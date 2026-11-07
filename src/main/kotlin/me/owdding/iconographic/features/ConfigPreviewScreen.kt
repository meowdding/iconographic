package me.owdding.iconographic.features

import me.owdding.iconographic.ComponentLike
import me.owdding.iconographic.Iconographic.asTooltipComponents
import me.owdding.iconographic.Tooltip
import me.owdding.iconographic.TooltipInformation
import me.owdding.iconographic.TooltipLine
import me.owdding.iconographic.config.previews.ConfigPreviewItem
import me.owdding.iconographic.config.previews.ConfigPreviewSection
import me.owdding.iconographic.config.previews.Silly1
import me.owdding.iconographic.render.TooltipHeader
import me.owdding.iconographic.system.CustomTooltip
import me.owdding.iconographic.system.TooltipTag
import me.owdding.iconographic.utils.ExtraDisplays
import me.owdding.lib.builder.LayoutFactory
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity

object ConfigPreviewScreen : Screen(CommonComponents.EMPTY) {

    var previewItem: ConfigPreviewItem = Silly1

    override fun init() {
        val item = previewItem.item ?: return LayoutFactory.frame {
            string("You need to be in a world to use the preview config!")
        }.center().apply()

        val preview = previewItem.preview

        var displayItem: ItemStack? = null
        var name: Component? = null
        val leftTags: MutableList<TooltipTag> = mutableListOf()
        val rightTags: MutableList<TooltipTag> = mutableListOf()
        var topRightIcon: Identifier? = null
        var rarity: SkyBlockRarity? = null
        var isRarityUpgraded = false
        val futures = mutableListOf<(maxWidth: Int, maxSideWidth: Int, totalHeight: Int) -> Layout>()

        preview.sections.forEach {
            val [tooltip, future] = section(item, it)

            futures.add(future )

            displayItem = displayItem ?: tooltip.item
            name = name ?: tooltip.name
            leftTags.addAll(tooltip.leftTags)
            rightTags.addAll(tooltip.rightTags)
            topRightIcon = topRightIcon ?: tooltip.topRightIcon
            rarity = rarity ?: tooltip.rarity
            isRarityUpgraded = isRarityUpgraded || tooltip.isRarityUpgraded
        }

        val header = TooltipHeader(displayItem ?: item, name ?: item.hoverName, leftTags, rightTags, topRightIcon, rarity)

    }

    fun section(item: ItemStack, section: ConfigPreviewSection): Pair<Tooltip, (maxWidth: Int, maxSideWidth: Int, totalHeight: Int) -> Layout> {
        val info = TooltipInformation(null, section.lore.map(::ComponentLike))
        val tooltip = CustomTooltip.update(item, info)

        return tooltip to { maxWidth, maxSideWidth, totalHeight: Int ->
            LayoutFactory.horizontal {
                vertical {
                    spacer(width = width / 2)
                    section.options.forEach {
                        it.visualize()
                    }
                }

                spacer(5)

                info.entries.toMutableList().apply { addFirst(ComponentLike(CommonComponents.EMPTY)) }.asTooltipComponents(maxWidth, maxSideWidth).forEach {
                    display(ExtraDisplays.clientTooltipComponent(it, maxWidth, totalHeight))
                }
            }
        }
    }

    fun <Element : LayoutElement> Element.center() = apply { FrameLayout.centerInRectangle(this, 0, 0, width, height) }
    fun Layout.apply() = this.visitWidgets(::addRenderableWidget)
}
