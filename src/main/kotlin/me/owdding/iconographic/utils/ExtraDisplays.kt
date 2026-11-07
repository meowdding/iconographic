package me.owdding.iconographic.utils

import me.owdding.lib.displays.Display
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import tech.thatgravyboat.skyblockapi.helpers.McFont

object ExtraDisplays {

    fun clientTooltipComponent(component: ClientTooltipComponent, maxWidth: Int, totalHeight: Int) : Display = object : Display {
        override fun getWidth(): Int = component.getWidth(McFont.self)

        override fun getHeight(): Int = component.getHeight(McFont.self)
        override fun extract(graphics: GuiGraphicsExtractor) {
            component.extractText(graphics, McFont.self, 0, 0)
            component.extractImage(McFont.self, 0, 0, maxWidth, totalHeight, graphics)
        }
    }

}
