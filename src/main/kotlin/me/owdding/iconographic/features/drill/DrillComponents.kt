package me.owdding.iconographic.features.drill

import me.owdding.iconographic.ComponentLike
import me.owdding.iconographic.ExtractableTooltipLine
import me.owdding.iconographic.Iconographic.id
import me.owdding.iconographic.TooltipLine
import me.owdding.iconographic.config.categories.mining.MiningConfig
import me.owdding.iconographic.font
import me.owdding.iconographic.lines.SpacerLine
import me.owdding.iconographic.system.RegisterFeature
import me.owdding.iconographic.system.Result
import me.owdding.iconographic.system.TooltipFeature
import me.owdding.iconographic.utils.ListMerger
import me.owdding.iconographic.utils.chat.DisplayColor
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.util.ARGB
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockCategory
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.repo.apis.SkyBlockItemsRepo
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.isBlank
import kotlin.math.max

@RegisterFeature
data object DrillComponents : TooltipFeature() {
    override val enabled: Boolean get() = MiningConfig.drillComponents
    override val priority: Int = 20

    override fun ItemStack.modifyEntries(list: MutableList<TooltipLine>, previousResult: Result?): Result = withComponentMerger(list) {
        if (getData(DataTypes.CATEGORY) != SkyBlockCategory.DRILL) return@withComponentMerger Result.unmodified

        val firstLines = buildList {
            DataTypes.FUEL_TANK()?.let { add(SkyBlockItemsRepo.getLazyItemStack(it)?.getDisplayName()?.stripped ?: return@let) }
            DataTypes.ENGINE()?.let { add(SkyBlockItemsRepo.getLazyItemStack(it)?.getDisplayName()?.stripped ?: return@let) }
            DataTypes.UPGRADE_MODULE()?.let { add(SkyBlockItemsRepo.getLazyItemStack(it)?.getDisplayName()?.stripped ?: return@let) }
            add("Fuel Tank: Not Installed")
            add("Drill Engine: Not Installed")
            add("Upgrade Module: Not Installed")
        }

        addUntil { it.stripped in firstLines }
        if (!canRead()) return@withComponentMerger Result.unmodified

        val upgradeComp = parseComponent(ComponentType.UPGRADE, DataTypes.UPGRADE_MODULE()) ?: return@withComponentMerger Result.unmodified
        val engineComp = parseComponent(ComponentType.ENGINE, DataTypes.ENGINE()) ?: return@withComponentMerger Result.unmodified
        val tankComp = parseComponent(ComponentType.TANK, DataTypes.FUEL_TANK()) ?: return@withComponentMerger Result.unmodified

        // Remove Mechanic line
        if (DataTypes.FUEL_TANK() == null && DataTypes.ENGINE() == null && DataTypes.UPGRADE_MODULE() == null) {
            read()
            if (peek().stripped.startsWith("Apply Drill Parts")) {
                read()
                read()
                read()
            }
        }

        originalMerger.add(upgradeComp)
        originalMerger.add(SpacerLine(height = 4))
        originalMerger.add(engineComp)
        originalMerger.add(SpacerLine(height = 4))
        originalMerger.add(tankComp)

        Result.modified
    }

    private fun ListMerger<Component>.parseComponent(
        type: ComponentType,
        skyblockId: String?,
    ): DrillComponentLine? {
        if (!canRead()) return null
        if (peek().isBlank()) read()
        val title = read()
        val isInstalled = !title.stripped.trim().endsWith("Not Installed")
        return DrillComponentLine(
            type, ComponentLike(title),
            buildList {
                while (canRead() && !peek().isBlank()) {
                    add(ComponentLike(read()))
                }
            },
            isInstalled, skyblockId?.let { SkyBlockId.item(it) },
        )
    }

    data class DrillComponentLine(
        val type: ComponentType,
        val nameLine: ComponentLike,
        val statLines: List<ComponentLike>,
        val isInstalled: Boolean,
        val skyblockId: SkyBlockId?,
    ) : ExtractableTooltipLine {

        private val textXOffset = 32

        private val itemStack: ItemStack? by lazy {
            if (!isInstalled || skyblockId == null) return@lazy null
            skyblockId.toItem().takeUnless { it.isEmpty }
        }

        override fun extract(graphics: GuiGraphicsExtractor, totalWidth: Int, x: Int, y: Int) {
            val totalHeight = getHeight(font)

            val bgSize = 26
            val iconSize = 16
            val bgY = y + (totalHeight - bgSize) / 2
            val iconY = y + (totalHeight - iconSize) / 2

            val iconX = x + 5

            val textX = x + textXOffset

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, id("drill/background"), x, bgY, bgSize, bgSize, -1)

            val stack = itemStack
            if (stack != null) {
                graphics.item(stack, iconX, iconY)
            } else {
                val drawColor = if (isInstalled) type.color else ARGB.opaque(DisplayColor.GRAY)
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, type.id, iconX, iconY, iconSize, iconSize, drawColor)
            }

            graphics.text(font, nameLine.charSequence, textX, y + 4, -1)

            var currentY = y + 16
            for (stat in statLines) {
                graphics.text(font, stat.charSequence, textX, currentY, -1)
                currentY += font.lineHeight + 1
            }
        }

        override fun getWidth(font: Font): Int {
            val nameWidth = nameLine.width + textXOffset
            val statsWidth = statLines.maxOfOrNull { it.width + textXOffset } ?: 0
            return max(nameWidth, max(statsWidth, 100))
        }

        override fun getHeight(font: Font): Int = (18 + (statLines.size * (font.lineHeight + 1))).coerceAtLeast(26)
    }

    enum class ComponentType(location: String, val color: Int) {
        TANK("tank", ARGB.opaque(DisplayColor.RED)),
        ENGINE("engine", ARGB.opaque(DisplayColor.GOLD)),
        UPGRADE("upgrade", ARGB.opaque(DisplayColor.LIGHT_PURPLE));

        val id = id("drill/$location")
    }
}
