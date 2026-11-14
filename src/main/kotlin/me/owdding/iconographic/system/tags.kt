package me.owdding.iconographic.system

import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.renderpearl.api.pipeline.RenderPipeline
import me.owdding.iconographic.Iconographic.id
import me.owdding.iconographic.font
import me.owdding.iconographic.utils.chat.ChatUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling
import net.minecraft.client.resources.model.sprite.SpriteId
import net.minecraft.data.AtlasIds
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import net.minecraft.util.ARGB
import org.joml.Matrix3x2f
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.drawGradient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.font
import kotlin.jvm.optionals.getOrNull

interface TooltipTag {
    fun extract(graphics: GuiGraphicsExtractor, x: Int, y: Int)
    val width: Int

    companion object {
        fun literal(component: Component, color: Int = -1) = TextTag.create(component, color)
        fun literal(text: String, color: Int = -1, init: MutableComponent.() -> Unit = {}) = TextTag.create(Text.of(text, init), color)
        fun identifier(id: Identifier, width: Int, height: Int = 13, color: Int = -1, background: Boolean = false) = IdentifierTag.create(id, width, height, color, background)
        fun literalGradient(component: Component, topLeftColor: Int, bottomRightColor: Int) = GradientTextTag.create(component, topLeftColor, -1, -1, topLeftColor)
        fun literalGradient(component: Component, topLeftColor: Int, topRightColor: Int, bottomRightColor: Int, bottomLeftColor: Int) = GradientTextTag.create(component, topLeftColor, topRightColor, bottomRightColor, bottomLeftColor)
    }
}

@ConsistentCopyVisibility
data class TextTag private constructor(val text: Component, val color: Int) : TooltipTag {
    companion object {
        fun create(component: Component, color: Int): TooltipTag = TextTag(
            component.copy().also {
                it.font = ChatUtils.mc5
            },
            ARGB.opaque(color),
        )
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
data class GradientTextTag private constructor(
    val text: Component,
    val topLeftColor: Int,
    val topRightColor: Int,
    val bottomRightColor: Int,
    val bottomLeftColor: Int,
) : TooltipTag {
    companion object {
        fun create(
            component: Component,
            topLeftColor: Int,
            topRightColor: Int,
            bottomRightColor: Int,
            bottomLeftColor: Int,
        ): TooltipTag = GradientTextTag(
            component.copy().also {
                it.font = ChatUtils.mc5
            },
            ARGB.opaque(topLeftColor), ARGB.opaque(topRightColor), ARGB.opaque(bottomRightColor), ARGB.opaque(bottomLeftColor)
        )
        fun create(
            text: Component,
            topLeftColor: Int,
            bottomRightColor: Int,
        ): TooltipTag = create(
            text,
            topLeftColor,
            getInterpolated(0.5f, topLeftColor, bottomRightColor),
            bottomRightColor,
            getInterpolated(0.5f, topLeftColor, bottomRightColor),
        )
    }

    inline val textWidth get() = font.width(text)
    override val width get() = textWidth + 10

    val height get() = 13

    override fun extract(graphics: GuiGraphicsExtractor, x: Int, y: Int) {
        graphics.gradientBlit(id("tag"), x, y, width - 1, height, topLeftColor, topRightColor, bottomRightColor, bottomLeftColor)
        graphics.text(font, text, x + 5, y + 2, -1)
    }
}

fun GuiGraphicsExtractor.gradientBlit(
    texture: Identifier,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    topLeftColor: Int,
    topRightColor: Int,
    bottomRightColor: Int,
    bottomLeftColor: Int,
) {
    val sprite = this.getSprite(SpriteId(McClient.self.atlasManager.getAtlasOrThrow(AtlasIds.GUI).location(), texture));
    val section: GuiMetadataSection = sprite.contents().getAdditionalMetadata(GuiMetadataSection.TYPE).getOrNull() ?: GuiMetadataSection.DEFAULT

    when (val scaling = section.scaling) {
        is GuiSpriteScaling.Stretch -> {
            gradientBlitInternal(sprite, x, y, width, height, topLeftColor, topRightColor, bottomRightColor, bottomLeftColor, sprite.u0, sprite.v0, sprite.u1, sprite.v1)
        }
        is GuiSpriteScaling.NineSlice -> {
            val border = scaling.border
            val borderLeft = border.left.coerceAtMost(scaling.width / 2)
            val borderRight = border.right.coerceAtMost(scaling.width / 2)
            val borderTop = border.top.coerceAtMost(scaling.height / 2)
            val borderBottom = border.bottom.coerceAtMost(scaling.height / 2)

            val u0 = sprite.u0
            val u1 = sprite.u1
            val v0 = sprite.v0
            val v1 = sprite.v1
            val uDiff = u1 - u0
            val vDiff = v1 - v0

            val leftUProgress = (borderLeft.toFloat() / scaling.width)
            val leftU = uDiff * leftUProgress
            val rightUProgress = 1 - (borderLeft.toFloat() / scaling.width)
            val rightU = uDiff * rightUProgress
            val topVProgress = borderTop.toFloat() / scaling.height
            val topV = vDiff * topVProgress
            val bottomVProgress = 1 - (borderBottom.toFloat() / scaling.height)
            val bottomV = vDiff * bottomVProgress

            when {
                width == scaling.width && height == scaling.height -> {
                    gradientBlitInternal(sprite, x, y, width, height, topLeftColor, topRightColor, bottomRightColor, bottomLeftColor, sprite.u0, sprite.v0, sprite.u1, sprite.v1)
                }
                height == scaling.height -> {
                    val topMiddleLeft = getInterpolated(leftUProgress, topLeftColor, topRightColor)
                    val bottomMiddleLeft = getInterpolated(leftUProgress, bottomLeftColor, bottomRightColor)
                    val topMiddleRight = getInterpolated(rightUProgress, topLeftColor, topRightColor)
                    val bottomMiddleRight = getInterpolated(rightUProgress, bottomLeftColor, bottomRightColor)
                    gradientBlitInternal(
                        sprite, x, y,
                        borderLeft, height,
                        topLeftColor, topMiddleLeft,
                        bottomMiddleLeft, bottomLeftColor,
                        u0, v0, u0 + leftU, v1
                    )
                    gradientBlitInternal(
                        sprite, x + borderLeft, y,
                        width - borderLeft - borderRight, height,
                        topMiddleLeft, topMiddleRight,
                        bottomMiddleRight, bottomMiddleLeft,
                        u0  + leftU, v0, u0 + rightU, v1
                    )
                    gradientBlitInternal(
                        sprite, x + width - borderRight, y,
                        borderRight, height,
                        topMiddleRight, topRightColor,
                        bottomRightColor, bottomMiddleRight,
                        u0 + rightU, v0, u1, v1
                    )
                }
                width == scaling.width -> {
                    val topMiddleLeft = getInterpolated(topVProgress, topLeftColor, bottomLeftColor)
                    val bottomMiddleLeft = getInterpolated(bottomVProgress, topLeftColor, bottomLeftColor)
                    val topMiddleRight = getInterpolated(topVProgress, topRightColor, bottomRightColor)
                    val bottomMiddleRight = getInterpolated(bottomVProgress, topRightColor, bottomRightColor)
                    gradientBlitInternal(
                        sprite, x, y,
                        width, borderTop,
                        topLeftColor, topRightColor,
                        topMiddleLeft, topMiddleRight,
                        u0, v0, u1, v0 + topV
                    )
                    gradientBlitInternal(
                        sprite, x, y + borderTop,
                        width, height - borderTop - borderBottom,
                        topMiddleLeft, topMiddleRight,
                        bottomMiddleRight, bottomMiddleLeft,
                        u0, v0 + topV, u1, v0 + bottomV
                    )
                    gradientBlitInternal(
                        sprite, x, y + height - borderBottom,
                        borderRight, height,
                        bottomMiddleLeft, bottomMiddleRight,
                        bottomRightColor, bottomLeftColor,
                        u0, v0 + topV, u1, v0 + bottomV
                    )
                }
                else -> {
                    val borderLeftTop = getInterpolated(topVProgress, topLeftColor, bottomLeftColor)
                    val borderLeftBottom = getInterpolated(bottomVProgress, topLeftColor, bottomLeftColor)

                    val borderTopLeft = getInterpolated(leftUProgress, topLeftColor, topRightColor)
                    val borderTopRight = getInterpolated(rightUProgress, topLeftColor, topRightColor)

                    val borderRightTop = getInterpolated(topVProgress, topRightColor, bottomRightColor)
                    val borderRightBottom = getInterpolated(bottomVProgress, topRightColor, bottomRightColor)

                    val borderBottomLeft = getInterpolated(leftUProgress, bottomLeftColor, bottomRightColor)
                    val borderBottomRight = getInterpolated(rightUProgress, bottomLeftColor, bottomRightColor)

                    val middleTopLeft = fourwayInterpolated(leftUProgress, topVProgress, topLeftColor, topRightColor, bottomLeftColor, bottomRightColor)
                    val middleTopRight = fourwayInterpolated(rightUProgress, topVProgress, topLeftColor, topRightColor, bottomLeftColor, bottomRightColor)
                    val middleBottomLeft = fourwayInterpolated(leftUProgress, bottomVProgress, topLeftColor, topRightColor, bottomLeftColor, bottomRightColor)
                    val middleBottomRight = fourwayInterpolated(rightUProgress, bottomVProgress, topLeftColor, topRightColor, bottomLeftColor, bottomRightColor)

                    // top left
                    gradientBlitInternal(
                        sprite, x, y, borderLeft, borderTop, topLeftColor, borderTopLeft, middleTopLeft, borderLeftTop,
                        sprite.u0, sprite.v0, sprite.u0 + leftU, sprite.v0 + topV
                    )
                    // middle left
                    gradientBlitInternal(
                        sprite, x, y + borderTop, borderLeft, height - borderBottom, borderLeftTop, middleTopLeft, borderLeftBottom, middleBottomLeft,
                        sprite.u0, sprite.v0 + topV, sprite.u0 + leftU, sprite.v0 + bottomV
                    )
                    // bottom left
                    gradientBlitInternal(
                        sprite, x, y + height - borderBottom, borderLeft,borderBottom, borderLeftBottom, middleBottomLeft, borderBottomLeft, bottomLeftColor,
                        sprite.u0, sprite.v0 + bottomV, sprite.u0 + leftU, sprite.v1
                    )


                    // top center
                    gradientBlitInternal(
                        sprite, x + borderLeft, y, width - borderLeft - borderRight, borderTop, borderTopLeft, borderTopRight, middleTopRight, middleBottomLeft,
                        sprite.u0 + leftU, sprite.v0, sprite.u0 + rightU, sprite.v0 + topV
                    )
                    // middle center
                    gradientBlitInternal(
                        sprite, x + borderLeft, y + borderTop, width - borderLeft - borderRight, height - borderTop - borderBottom, middleTopLeft, middleTopRight, middleBottomRight, middleBottomLeft,
                        sprite.u0 + leftU, sprite.v0 + topV, sprite.u0 + rightU, sprite.v0 + bottomV
                    )
                    // bottom center
                    gradientBlitInternal(
                        sprite, x + borderLeft, y + height - borderBottom, width - borderLeft - borderRight, borderBottom, middleTopLeft, middleTopRight, borderBottomRight, borderBottomLeft,
                        sprite.u0 + leftU, sprite.v0 + bottomV, sprite.u0 + rightU, sprite.v1
                    )

                    // top right
                    gradientBlitInternal(
                        sprite, x + width - borderRight, y, borderRight, borderTop, borderTopRight, topRightColor, borderRightTop, middleTopRight,
                        sprite.u0 + rightU, sprite.v0, sprite.u1, sprite.v0 + topV
                    )
                    // middle right
                    gradientBlitInternal(
                        sprite, x + width - borderRight, y + borderTop, borderRight, height - borderTop - borderBottom, middleTopRight, borderRightTop, borderRightBottom, middleBottomRight,
                        sprite.u0 + rightU, sprite.v0 + topV, sprite.u1, sprite.v0 + bottomV
                    )
                    // bottom right
                    gradientBlitInternal(
                        sprite, x + width - borderRight, y + height - borderBottom, borderRight, borderBottom, middleBottomRight, borderRightBottom, bottomRightColor, borderBottomRight,
                        sprite.u0 + rightU, sprite.v0 + bottomV, sprite.u1, sprite.v1
                    )
                }
            }
        }
    }
}

private fun GuiGraphicsExtractor.gradientBlitInternal(
    sprite: TextureAtlasSprite,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    topLeftColor: Int,
    topRightColor: Int,
    bottomRightColor: Int,
    bottomLeftColor: Int,
    u0: Float,
    v0: Float,
    u1: Float,
    v1: Float,
) {
    val texture = McClient.self.textureManager.getTexture(sprite.atlasLocation())
    this.guiRenderState.addGuiElement(GradientTextureState(
        Matrix3x2f(pose()),
        texture,
        x,y,x+width,y+height,
        u0, v0, u1, v1,
        topLeftColor, topRightColor, bottomRightColor, bottomLeftColor
    ))
}

private fun getInterpolated(progress: Float, first: Int, second: Int) = ARGB.linearLerp(progress, first, second)
private fun fourwayInterpolated(horizontal: Float, vertical: Float, topLeft: Int, topRight: Int, bottomLeft: Int, bottomRight: Int) =
    ARGB.linearLerp(vertical, ARGB.linearLerp(horizontal, topLeft, topRight), ARGB.linearLerp(horizontal, bottomLeft, bottomRight))

data class GradientTextureState(
    val pose: Matrix3x2f,
    val texture: AbstractTexture,
    val x0: Int, val y0: Int, val x1: Int, val y1: Int,
    val u0: Float, val v0: Float, val u1: Float, val v1: Float,
    val col1: Int, val col2: Int, val col3: Int, val col4: Int,
    val scissor: ScreenRectangle? = null,
    val bounds: ScreenRectangle = ScreenRectangle(x0, y0, x1 - x0, y1 - y0),
) : GuiElementRenderState {
    override fun buildVertices(vertexConsumer: VertexConsumer) {
        vertexConsumer.addVertexWith2DPose(this.pose, this.x0.toFloat(), this.y0.toFloat()).setUv(this.u0, this.v0).setColor(this.col1)
        vertexConsumer.addVertexWith2DPose(this.pose, this.x0.toFloat(), this.y1.toFloat()).setUv(this.u0, this.v1).setColor(this.col2)
        vertexConsumer.addVertexWith2DPose(this.pose, this.x1.toFloat(), this.y1.toFloat()).setUv(this.u1, this.v1).setColor(this.col3)
        vertexConsumer.addVertexWith2DPose(this.pose, this.x1.toFloat(), this.y0.toFloat()).setUv(this.u1, this.v0).setColor(this.col4)
    }

    override fun pipeline(): RenderPipeline = RenderPipelines.GUI_TEXTURED
    override fun textureSetup(): TextureSetup = TextureSetup.singleTexture(texture.textureView, texture.sampler)
    override fun bounds(): ScreenRectangle? = bounds
    override fun scissorArea(): ScreenRectangle? = scissor
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
