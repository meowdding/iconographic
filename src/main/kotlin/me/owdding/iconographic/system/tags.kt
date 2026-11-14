package me.owdding.iconographic.system

import me.owdding.iconographic.Iconographic.id
import me.owdding.iconographic.font
import me.owdding.iconographic.utils.chat.ChatUtils
import me.owdding.lib.rendering.GuiRenderTypes
import me.owdding.lib.utils.GradientDirection
import me.owdding.lib.utils.GradientProvider
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import net.minecraft.util.ARGB
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.font

interface TooltipTag {
    fun extract(graphics: GuiGraphicsExtractor, x: Int, y: Int)
    val width: Int

    companion object {
        fun literal(component: Component, color: Int = -1) = TextTag.create(component, color)
        fun literal(text: String, color: Int = -1, init: MutableComponent.() -> Unit = {}) = TextTag.create(Text.of(text, init), color)
        fun gradientLiteral(component: Component, color: GradientProvider) = GradientTextTag.create(component, color)
        fun gradientLiteral(text: String, color: GradientProvider, init: MutableComponent.() -> Unit = {}) = GradientTextTag.create(Text.of(text, init), color)
        fun identifier(id: Identifier, width: Int, height: Int = 13, color: Int = -1, background: Boolean = false) = IdentifierTag.create(id, width, height, color, background)
    }
}

@ConsistentCopyVisibility
data class TextTag private constructor(val text: Component, val color: Int) : TooltipTag {
    companion object {
        fun create(component: Component, color: Int): TooltipTag = TextTag(component.copy().also {
            it.font = ChatUtils.mc5
        }, ARGB.opaque(color))
    }

    inline val textWidth get() = font.width(text)
    override val width get() = textWidth + 10

    val height get() = 13

    override fun extract(graphics: GuiGraphicsExtractor, x: Int, y: Int) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, id("tag"), x, y, width - 1, height, color)
        graphics.text(font, text, x + 5, y + 2, color)
    }
}

@ConsistentCopyVisibility
data class GradientTextTag private constructor(val text: Component, val gradient: GradientProvider) : TooltipTag {
    companion object {
        fun create(component: Component, color: GradientProvider): TooltipTag = GradientTextTag(component.copy().also {
            it.font = ChatUtils.mc5
        }, color)
    }

    inline val textWidth get() = font.width(text)
    override val width get() = textWidth + 10

    val height get() = 13

    override fun extract(graphics: GuiGraphicsExtractor, x: Int, y: Int) {
        // TODO: remove direction setting with 4.3.8 after the default direction has been changed to right
        graphics.blitSprite(GuiRenderTypes.guiTexturedGradient(colors = gradient, speed = 3f, direction = GradientDirection.RIGHT), id("tag"), x, y, width - 1, height, -1)
        graphics.text(font, text, x + 5, y + 2, -1)
    }
}

@ConsistentCopyVisibility
data class IdentifierTag private constructor(val identifier: Identifier, val iconWidth: Int, val iconHeight: Int, val color: Int, val background: Boolean) : TooltipTag {
    companion object {
        fun create(identifier: Identifier, width: Int, height: Int, color: Int, background: Boolean = false): TooltipTag = IdentifierTag(identifier, width, height, ARGB.opaque(color), background)
    }

    override val width get() = iconWidth + if (background) 10 else 1

    val height get() = 13

    override fun extract(graphics: GuiGraphicsExtractor, x: Int, y: Int) {
        if (background) {
            // This could be broken
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, id("tag"), x, y, width - 1, height, color)
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, x + 5, y + 2, iconWidth, 10, color)
        } else {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, x, y + (13 - height) / 2, iconWidth, iconHeight, color)
        }
    }
}
