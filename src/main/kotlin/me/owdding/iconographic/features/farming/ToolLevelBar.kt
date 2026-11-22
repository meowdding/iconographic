package me.owdding.iconographic.features.farming

import me.owdding.iconographic.ExtractableTooltipLine
import me.owdding.iconographic.Iconographic.id
import me.owdding.iconographic.TooltipLine
import me.owdding.iconographic.config.categories.farming.FarmingConfig
import me.owdding.iconographic.font
import me.owdding.iconographic.system.RegisterFeature
import me.owdding.iconographic.system.Result
import me.owdding.iconographic.system.TooltipFeature
import me.owdding.iconographic.utils.RemoteStrings
import me.owdding.iconographic.utils.chat.ChatUtils
import me.owdding.iconographic.utils.chat.DisplayColor
import me.owdding.lib.extensions.shorten
import me.owdding.lib.utils.strings.StringGroup.Companion.resolve
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import net.minecraft.util.ARGB
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.utils.extentions.parseFormattedDouble
import tech.thatgravyboat.skyblockapi.utils.extentions.scissor
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.width
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.font
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.shadowColor
import kotlin.math.max
import kotlin.math.roundToInt

@RegisterFeature
data object ToolLevelBar : TooltipFeature() {
    val thingy by RemoteStrings.resolve().regex(".*(\\(.*?/.*?\\))")
    private val YELLOW get() = ARGB.opaque(DisplayColor.YELLOW)

    override val enabled: Boolean get() = FarmingConfig.toolLevelBar
    override val priority: Int = 0

    override fun ItemStack.applies(): Boolean = DataTypes.TOOL_EXP() != null && DataTypes.TOOL_LEVEL() != null

    override fun ItemStack.modifyEntries(list: MutableList<TooltipLine>, previousResult: Result?): Result = withComponentMerger(list) {
        addUntil { it.stripped.startsWith("Level ") }
        val levelLine = read().stripped
        val barLine = read().stripped
        val requireUpgrade: Boolean
        val requireOverclock: Boolean

        val xpSection = levelLine.replace(thingy, "$1").substringAfter('(').removeSuffix(")")
        val split = xpSection.split("/")
        val [experience, required] = split.map { it.parseFormattedDouble() }

        val process = barLine.trim().removeSuffix("%").toFloatOrNull()?.let { it / 100f } ?: (experience.toFloat() / required.toFloat())

        val max: Boolean = levelLine.contains("(MAX!)")
        if (!max) {
            requireUpgrade = canRead() && peek().stripped.trim() == "UPGRADE REQUIRED!"
            requireOverclock = canRead() && peek().stripped.trim() == "OVERCLOCK REQUIRED!"

            if (requireOverclock || requireUpgrade) read()
        } else {
            requireUpgrade = false
            requireOverclock = false
        }

        originalMerger.add(FarmingToolLevelLine(DataTypes.TOOL_LEVEL() ?: 1, experience, required, process, requireOverclock, requireUpgrade, max, DataTypes.TOOL_OVERCLOCKS() ?: 0))
        Result.modified
    }

    enum class OverClockSlots(val x: Int, val y: Int, val isLeft: Boolean) {
        ONE(5, 8, true),
        TWO(12, 0, true),
        THREE(20, 0, false),
        FOUR(13, 8, false),
        FIVE(9, 8, false),
        SIX(16, 0, false),
        SEVEN(8, 0, true),
        EIGHT(1, 8, true),
        NINE(1, 4, true),
        TEN(9, 4, false),
        ;

        companion object {
            fun getFilledSlots(overclocks: Int) = entries.take(overclocks)
        }
    }

    data class FarmingToolLevelLine(
        val level: Int,
        val experience: Double,
        val needed: Double,
        val progress: Float,
        val requiresOverclock: Boolean,
        val requiresUpgrade: Boolean,
        val isMax: Boolean,
        val overclockAmount: Int,
    ) : ExtractableTooltipLine {

        val titleComponent = Text.of {
            this.font = ChatUtils.mc5
            this.shadowColor = null
            append("Level $level")
        }

        val experienceComponent = Text.of {
            this.font = ChatUtils.mc5
            this.shadowColor = null
            this.color = -1
            append("${experience.toFormattedString()}/${needed.shorten()}")
        }

        val extraText = when {
            requiresUpgrade -> Text.of("Requires Upgrade!") {
                color = DisplayColor.RED
                this.font = ChatUtils.mc5
                this.shadowColor = null
            }
            requiresOverclock -> Text.of("Requires Overclock!") {
                color = DisplayColor.DARK_AQUA
                this.font = ChatUtils.mc5
                this.shadowColor = null
            }
            else -> null
        }

        override fun extract(graphics: GuiGraphicsExtractor, totalWidth: Int, x: Int, y: Int) {

            val titleWidth = titleComponent.width
            val left = x + (totalWidth - titleWidth) / 2 - 22
            val titleTriangleWidth = titleWidth + 46
            graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                id("farming/triangle_up"),
                left,
                y,
                titleTriangleWidth,
                14,
                ARGB.opaque(-1),
            )
            graphics.centeredText(font, titleComponent, x + totalWidth / 2, y + 2, -1)

            OverClockSlots.getFilledSlots(overclockAmount).forEach {
                val x: Int
                val id: Identifier

                if (it.isLeft) {
                    x = left + it.x
                    id = id("farming/overclock_inlay_left")
                } else {
                    x = left + titleTriangleWidth - it.x
                    id = id("farming/overclock_inlay_right")
                }

                graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    id,
                    x,
                    y + it.y,
                    6,
                    5,
                    -1
                )
            }

            graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                id("triangle_down"),
                x + 5,
                y + 15,
                totalWidth - 10,
                14,
                ARGB.opaque(-1),
            )

            val progress = if (experience <= 0) 0f else (experience / needed).coerceIn(.0, 1.0).toFloat()

            graphics.scissor((x + 11)..(x + 11 + ((totalWidth - 23) * progress).roundToInt()), (y + 18)..(y + 25)) {
                graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    when {
                        isMax -> id("progress_bar/max")
                        requiresOverclock -> id("progress_bar/cyan")
                        requiresUpgrade -> id("progress_bar/red")
                        else -> id("progress_bar")
                    },
                    x + 11,
                    y + 18,
                    totalWidth - 23,
                    7,
                    -1,
                )
            }
            graphics.centeredText(font, experienceComponent, x + totalWidth / 2, y + 17, -1)

            if (extraText != null) {
                val extraWidth = extraText.width
                graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    id("triangle_down"),
                    x + (totalWidth - extraWidth) / 2 - 22,
                    y + 30,
                    extraWidth + 46,
                    14,
                    ARGB.opaque(-1),
                )
                graphics.centeredText(font, extraText, x + totalWidth / 2, y + 32, -1)
            }
        }

        override fun getWidth(font: Font): Int = max(max(font.width(titleComponent) + 46, font.width(experienceComponent) + 40), extraText?.let { font.width(it) + 63 } ?: 0)

        override fun getHeight(font: Font): Int = 30 + if (extraText != null) 15 else 0
    }
}
