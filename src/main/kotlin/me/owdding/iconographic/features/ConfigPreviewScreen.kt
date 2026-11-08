package me.owdding.iconographic.features

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.compound.LayoutWidget
import me.owdding.iconographic.ComponentLike
import me.owdding.iconographic.Iconographic.asTooltipComponents
import me.owdding.iconographic.SideTooltipLine
import me.owdding.iconographic.Tooltip
import me.owdding.iconographic.TooltipInformation
import me.owdding.iconographic.TooltipLine
import me.owdding.iconographic.config.Config
import me.owdding.iconographic.config.previews.ConfigPreviewItem
import me.owdding.iconographic.config.previews.ConfigPreviewSection
import me.owdding.iconographic.config.previews.DrillPreview
import me.owdding.iconographic.config.previews.PetPreview
import me.owdding.iconographic.render.TooltipHeader
import me.owdding.iconographic.system.CustomTooltip
import me.owdding.iconographic.system.TooltipTag
import me.owdding.iconographic.utils.ExtraDisplays
import me.owdding.iconographic.utils.debug.debugToggle
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.VerticalLayoutBuilder
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.layouts.PaddedWidget
import me.owdding.lib.layouts.withPadding
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LayoutSettings
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.TriState
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity

object ConfigPreviewScreen : Screen(CommonComponents.EMPTY) {

    val alignBoxes by debugToggle("preview/align_and_boxes")

    var previewItem: ConfigPreviewItem = PetPreview

    override fun isPauseScreen(): Boolean = false

    override fun onClose() {
        Config.save()
        super.onClose()
    }

    override fun init() {
        val item = previewItem.item ?: return LayoutFactory.frame {
            string("You need to be in a world to use the preview config!")
        }.center().apply()

        val preview = previewItem.preview

        var displayItem: ItemStack? = null
        var name: Component? = null
        val leftTags: MutableSet<TooltipTag> = mutableSetOf()
        val rightTags: MutableSet<TooltipTag> = mutableSetOf()
        var topRightIcon: Identifier? = null
        var rarity: SkyBlockRarity? = null
        var isRarityUpgraded = false
        val futures = mutableListOf<(widgets: PreviewWidgets) -> Unit>()

        var maxWidth = 0
        var maxSideWidth = 0
        var height = 0
        preview.sections.forEachIndexed { index, section ->
            val [tooltip, future] = section(item, section, index)

            futures.add(future)

            maxWidth = maxOf(maxWidth, tooltip.entries.maxOfOrNull { it.getWidth(font) } ?: 0)
            maxSideWidth = maxOf(maxSideWidth, tooltip.entries.filterIsInstance<SideTooltipLine>().maxOfOrNull { it.getSideWidth(font) } ?: 0)
            height += tooltip.entries.sumOf { it.getHeight(font) }

            displayItem = displayItem ?: tooltip.item
            name = name ?: tooltip.name
            leftTags.addAll(tooltip.leftTags)
            rightTags.addAll(tooltip.rightTags)
            topRightIcon = topRightIcon ?: tooltip.topRightIcon
            rarity = rarity ?: tooltip.rarity
            isRarityUpgraded = isRarityUpgraded || tooltip.isRarityUpgraded
        }

        val header = TooltipHeader(displayItem ?: item, name ?: item.hoverName, leftTags.toList(), rightTags.toList(), topRightIcon, rarity)

        height += header.getHeight(font)
        maxWidth = maxOf(maxWidth, header.getWidth(font))

        val tooltipStyle = displayItem?.get(DataComponents.TOOLTIP_STYLE)
        val widgets = PreviewWidgets(
            maxWidth = maxWidth,
            maxSideWidth = maxSideWidth,
            totalHeight = height,
        )

        widgets.optionAndLine(
            LayoutFactory.vertical {
                preview.globalConfigs.forEach {
                    it.visualize()
                }
            },
            LayoutFactory.vertical {
                widgets.asTooltipComponentDisplays(listOf(header)).forEach(::display)
            },
        )
        futures.forEach { section ->
            section(widgets)
        }

        val options = widgets.options.build(2)
        val lines = widgets.lines.build(2)

        LayoutFactory.horizontal {
            widget(options.withPadding(12))
            spacer(5)
            // TODO: this doesnt support custom background texture, should just use TooltipRenderUtil.extractBackground()
            widget(lines.withPadding(12).withTexturedBackground(TooltipRenderUtil.getFrameSprite(tooltipStyle)).withTexturedBackground(TooltipRenderUtil.getBackgroundSprite(tooltipStyle)))
        }.center().let {
            val diff = this@ConfigPreviewScreen.width - it.width - 20
            PaddedWidget(it, 0, diff / 2, 0, diff / 2)
        }.asScrollable(this@ConfigPreviewScreen.width, this@ConfigPreviewScreen.height).apply()
    }

    fun LayoutElement.withTexturedBackground(location: Identifier, width: Int = this.width, height: Int = this.height): LayoutElement = LayoutFactory.frame {
        val element = this@withTexturedBackground
        widget(Displays.sprite(location, width, height).asWidget()) {
            alignHorizontallyCenter()
            alignVerticallyMiddle()
        }
        widget(element) {
            alignHorizontallyCenter()
            alignVerticallyMiddle()
        }
    }

    data class PreviewWidgets(
        val options: LayoutBuilder = VerticalLayoutBuilder(),
        val lines: LayoutBuilder = VerticalLayoutBuilder(),
        val maxWidth: Int,
        val maxSideWidth: Int,
        val totalHeight: Int,
    ) {
        fun optionAndLine(options: LayoutElement, lines: LayoutElement) {
            val height = maxOf(options.height, lines.height)
            option(options, height)
            line(lines, height)
        }

        fun option(widget: LayoutElement, height: Int) = options.widget(
            LayoutFactory.frame(height = height) {
                spacer(height = height)
                if (alignBoxes) {
                    display(Displays.outline({ 0xFFFFFFFFu }, Displays.empty(widget.width, height)))
                }
                widget(widget)
            },
        )

        fun line(widget: LayoutElement, height: Int) = lines.widget(
            LayoutFactory.frame(height = height) {
                spacer(height = height)
                if (alignBoxes) {
                    widget(
                        object : AbstractWidget(0, 0, 0, height, CommonComponents.EMPTY) {
                            override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
                                graphics.horizontalLine(0, 100000, y, -1)
                            }

                            override fun updateWidgetNarration(output: NarrationElementOutput) = Unit
                        },
                    )
                    display(Displays.outline({ 0xFFFFFFFFu }, Displays.empty(widget.width, height)))
                }
                widget(widget)
            },
        )

        fun asTooltipComponentDisplays(list: List<TooltipLine>) = asTooltipComponents(list).map(::asDisplay)
        fun asTooltipComponents(list: List<TooltipLine>) = list.asTooltipComponents(maxWidth, maxSideWidth)
        fun asDisplay(line: ClientTooltipComponent) = ExtraDisplays.clientTooltipComponent(line, maxWidth, totalHeight)
    }

    fun section(item: ItemStack, section: ConfigPreviewSection, index: Int): Pair<Tooltip, (widgets: PreviewWidgets) -> Unit> {
        val info = TooltipInformation(null, section.lore.toMutableList().apply { if (index > 0) addFirst(CommonComponents.EMPTY) }.map(::ComponentLike))
        val tooltip = CustomTooltip.update(item, info)

        return tooltip to { widgets ->
            if (section.options.isEmpty() && tooltip.entries.isEmpty()) return@to

            widgets.optionAndLine(
                LayoutFactory.vertical {
                    section.options.forEach {
                        it.visualize()
                    }
                },
                LayoutFactory.vertical {
                    widgets.asTooltipComponentDisplays(tooltip.entries).forEach(::display)
                },
            )
        }
    }

    fun <Element : LayoutElement> Element.center() = apply { FrameLayout.centerInRectangle(this, 0, 0, this@ConfigPreviewScreen.width, this@ConfigPreviewScreen.height) }
    fun LayoutElement.apply() = this.visitWidgets(::addRenderableWidget)


    fun AbstractWidget.asScrollable(
        width: Int,
        height: Int,
        init: LayoutWidget<FrameLayout>.() -> Unit = {},
        alwaysShowScrollBar: Boolean = false,
    ): LayoutWidget<FrameLayout> {
        val scrollable = Widgets.frame { frame ->
            frame.withScrollableY(alwaysShowScrollBar.takeIf { it }?.let { TriState.TRUE } ?: TriState.DEFAULT).withSize(width, this.height.coerceAtMost(height)).withAutoFocus(false)
                .withContents { contents ->
                    contents.setMinWidth(width - 10)
                    contents.addChild(this, LayoutSettings.defaults().alignHorizontallyCenter())
                }.withAutoFocus(false).init()
        }

        return scrollable
    }

    fun Layout.asScrollable(width: Int, height: Int, init: LayoutWidget<FrameLayout>.() -> Unit = {}, alwaysShowScrollBar: Boolean = false): Layout {
        this.arrangeElements()
        val widget = LayoutWidget(this).apply {
            visible = true
            withAutoFocus(false)
        }.withStretchToContentSize()

        return LayoutFactory.frame(width, height) {
            widget(widget.asScrollable(width, height, init, alwaysShowScrollBar))
        }
    }
}
