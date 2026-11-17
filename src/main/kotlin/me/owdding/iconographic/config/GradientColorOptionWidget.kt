package me.owdding.iconographic.config

import com.google.gson.JsonElement
import com.mojang.blaze3d.platform.InputConstants
import com.mojang.serialization.Codec
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigElementRenderer
import com.teamresourceful.resourcefulconfig.api.client.options.ResourcefulConfigOptionUI
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfigElement
import com.teamresourceful.resourcefulconfig.api.types.elements.ResourcefulConfigEntryElement
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigValueEntry
import com.teamresourceful.resourcefulconfig.client.UIConstants
import com.teamresourceful.resourcefulconfig.client.components.ModSprites
import com.teamresourceful.resourcefulconfig.client.components.base.BaseWidget
import com.teamresourceful.resourcefulconfig.client.components.base.SpriteButton
import com.teamresourceful.resourcefulconfig.client.components.options.types.NumberOptionWidget
import com.teamresourceful.resourcefulconfig.client.components.options.types.color.HsbColor
import com.teamresourceful.resourcefulconfig.client.components.options.types.color.HsbState
import com.teamresourceful.resourcefulconfig.client.components.options.types.color.HueSelector
import com.teamresourceful.resourcefulconfig.client.components.options.types.color.SaturationBrightnessSelector
import com.teamresourceful.resourcefulconfig.client.screens.base.CloseableScreen
import com.teamresourceful.resourcefulconfig.client.screens.base.OverlayScreen
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.utils.ListenableState
import me.owdding.iconographic.Iconographic
import me.owdding.iconographic.config.categories.visuals.VisualsConfig
import me.owdding.iconographic.generated.IconographicCodecs
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.rendering.GuiRenderTypes.animatedGradient
import me.owdding.lib.rendering.text.PrideShader
import me.owdding.lib.rendering.text.builtin.GradientTextShader
import me.owdding.lib.rendering.text.textShader
import me.owdding.lib.utils.GradientDirection
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.ARGB
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedName
import tech.thatgravyboat.skyblockapi.utils.extentions.toIntValue
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.SkyBlockColor
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.regex.Pattern
import kotlin.math.ceil

class GradientColorOption(val element: ResourcefulConfigElement) : ResourcefulConfigElementRenderer {
    val entry get() = (element as ResourcefulConfigEntryElement).entry() as ResourcefulConfigValueEntry

    override fun title(): MutableComponent = entry.options().title().toComponent()
    override fun description(): MutableComponent = entry.options().comment().toComponent()

    override fun widgets(): List<AbstractWidget> = listOf(
        GradientColorOptionWidget(
            { GradientColorOptionWidget.OptionState.fromString(entry.string) },
            { entry.string = it.asJson },
        ),
        SpriteButton.builder(12, 12)
            .padding(2)
            .sprite(ModSprites.RESET)
            .tooltip(UIConstants.RESET)
            .onPress { entry.reset() }
            .build(),
    )

    companion object {
        val ID = Iconographic.id("gradient_color_option")
    }
}

class GradientColorOptionWidget(private val getter: Supplier<OptionState>, private val setter: Consumer<OptionState>) : BaseWidget(SIZE, SIZE) {
    @GenerateCodec
    data class OptionState(
        val colors: MutableList<Int> = mutableListOf(),
        var direction: GradientDirection = GradientDirection.RIGHT,
        var speed: Int = 1,
    ) {
        val asJson get() = this.toJson(CODEC).toString()
        val actualColors
            get() = when (colors.size) {
                0, 1 -> colors
                else -> if (colors.first() != colors.last()) colors.toMutableList().apply { add(colors.first()) } else colors
            }

        companion object {
            val CODEC: Codec<OptionState> = IconographicCodecs.OptionStateCodec.codec()

            fun fromString(string: String) = string.readJson<JsonElement>().toData(CODEC) ?: OptionState()
        }
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ModSprites.BUTTON, x, y, this.width, this.height)
        val (actualColors, direction, speed) = this.getter.get()
        graphics.animatedGradient(x + 1, y + 1, this.width - 2, this.height - 2, speed.toFloat(), direction) { actualColors }
        this.applyCursor(graphics)
    }

    override fun onClick(event: MouseButtonEvent, bl: Boolean) {
        McClient.setScreen(PresetsOverlay(this))
    }

    private class PresetsOverlay(private val widget: GradientColorOptionWidget) : OverlayScreen(McScreen.self!!), CloseableScreen {
        private val data get() = widget.getter.get()

        private var currentColor: Int = 0
        private val colorState: HsbState = HsbState(HsbColor.fromRgb(data.colors.getOrElse(0) { TextColor.WHITE })) { color: HsbColor ->
            widget.setter.accept(
                data.apply {
                    if (colors.isEmpty()) {
                        colors.add(color.toRgba())
                        currentColor = 0
                    } else if (currentColor == colors.size) {
                        colors.add(color.toRgba())
                    } else if (currentColor < colors.size) {
                        colors[currentColor] = color.toRgba()
                    }
                },
            )
        }
        private val direction: ListenableState<GradientDirection> = ListenableState.of(data.direction).apply {
            registerListener { widget.setter.accept(data.apply { this.direction = it }) }
        }
        private val speed: ListenableState<Int> = ListenableState.of(data.speed).apply {
            registerListener { widget.setter.accept(data.apply { this.speed = it }) }
        }
        private val presetType: ListenableState<PresetType> = ListenableState.of(if (VisualsConfig.skyBlockColor) PresetType.SKYBLOCK_COLORS else PresetType.MC_COLORS)

        private var x = 0
        private var y = 0
        private var width = 0
        private var height = 0

        override fun init() {
            val layout = LayoutFactory.horizontal(SPACING) {
                vertical(SPACING * 2) {
                    data.colors.forEachIndexed { index, _ ->
                        widget(
                            Button().apply {
                                withTexture(null)
                                withSize(12, 12)
                                withRenderer { graphics, ctx, _ ->
                                    val color = data.colors[index]
                                    graphics.fill(ctx.x, ctx.y, ctx.x + 12, ctx.y + 12, ARGB.opaque(color))
                                }
                                withCallback {
                                    currentColor = index
                                    colorState.set(HsbColor.fromRgb(data.colors.getOrElse(currentColor) { TextColor.WHITE }))
                                    rebuildWidgets()
                                }
                            },
                        )
                    }

                    widget(
                        Button().apply {
                            withTexture(null)
                            withSize(12, 12)
                            withRenderer { graphics, ctx, _ ->
                                graphics.centeredText(McFont.self, "+", ctx.x + 6, ctx.y, -1)
                            }
                            withCallback {
                                currentColor = data.colors.size
                                colorState.set(HsbColor.fromRgb(TextColor.WHITE))
                                rebuildWidgets()
                            }
                        },
                    )
                }

                vertical(SPACING) {
                    widget(SaturationBrightnessSelector(100, 50, this@PresetsOverlay.colorState))
                    widget(HueSelector(100, 10, this@PresetsOverlay.colorState))

                    horizontal {
                        widget(
                            Button().apply {
                                withTexture(null)
                                withSize(12, 16)
                                withTooltip(Text.of("Delete Color"))
                                withRenderer { graphics, ctx, _ ->
                                    graphics.centeredText(McFont.self, "x", ctx.x + 6, ctx.y + 2, ARGB.opaque(TextColor.RED))
                                }
                                withCallback {
                                    if (data.colors.isNotEmpty()) {
                                        widget.setter.accept(
                                            data.apply {
                                                colors.removeAt(currentColor.coerceAtMost(colors.size - 1))
                                                currentColor = currentColor.coerceAtMost(data.colors.size - 1).coerceAtLeast(0)
                                            },
                                        )
                                        rebuildWidgets()
                                    }
                                }
                            },
                        )

                        widget(
                            ResourcefulConfigOptionUI.dropdown(
                                Component.empty(),
                                GradientDirection.entries,
                                this@PresetsOverlay.direction,
                                this@PresetsOverlay.direction,
                            ),
                        )
                    }

                    widget(
                        NumberOptionWidget(
                            this@PresetsOverlay.speed,
                            { s -> this@PresetsOverlay.speed.set(s); true },
                            String::toIntValue,
                            POSITIVE_INTEGER_FILTER,
                        ),
                    )

                    widget(
                        object : AbstractWidget(0, 0, 100, 16, CommonText.EMPTY) {
                            override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
                                graphics.animatedGradient(x, y, this.width, this.height, speed.get().toFloat(), direction.get()) {
                                    data.actualColors
                                }
                            }

                            override fun updateWidgetNarration(output: NarrationElementOutput) {}
                        },
                    )

                    widget(
                        ResourcefulConfigOptionUI.dropdown(
                            Component.empty(),
                            PresetType.entries,
                            { this@PresetsOverlay.presetType.get() },
                            { newValue ->
                                this@PresetsOverlay.presetType.set(newValue)
                                rebuildWidgets()
                            },
                        ).apply { width = 100 },
                    )

                    widget(
                        PresetGridWidget({ presetType.get().colors }) { presetColors, isShift ->
                            widget.setter.accept(
                                data.apply {
                                    if (isShift) {
                                        this.colors.addAll(presetColors)
                                    } else {
                                        this.colors.clear()
                                        this.colors.addAll(presetColors)
                                    }
                                    currentColor = 0
                                },
                            )
                            rebuildWidgets()
                        }
                    )
                }
            }

            val windowWidth = this.rectangle.width()
            val windowHeight = this.rectangle.height()

            val targetX = this.widget.x.coerceAtMost(windowWidth - layout.width - PADDING * 2)
            val targetY = if (this.widget.y + this.widget.getHeight() + SPACING + layout.height + PADDING * 2 > windowHeight) {
                this.widget.y - layout.height - PADDING - 2 - SPACING
            } else {
                this.widget.y + this.widget.getHeight() + SPACING
            }

            layout.setPosition(targetX, targetY + PADDING)
            layout.visitWidgets { widget -> this.addRenderableWidget(widget) }

            this.x = targetX - PADDING
            this.y = targetY
            this.width = layout.width + PADDING * 2
            this.height = layout.height + PADDING * 2
        }

        override fun extractBackground(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
            super.extractBackground(graphics, mouseX, mouseY, partialTicks)
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ModSprites.ACCENT, this.x, this.y, this.width, this.height)
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ModSprites.BUTTON, this.x + 1, this.y + 1, this.width - 2, this.height - 2)
        }

        override fun onClosed(replacement: Screen?) {}

        override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
            return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height
        }

        override fun mouseClicked(event: MouseButtonEvent, bl: Boolean): Boolean {
            if (event.button() != InputConstants.MOUSE_BUTTON_LEFT || this.isMouseOver(event.x(), event.y())) {
                return super.mouseClicked(event, bl)
            }
            this.onClose()
            return false
        }
    }

    private class PresetGridWidget(
        private val presetsSupplier: Supplier<List<Pair<List<Int>, String>>>,
        private val onSelect: (List<Int>, Boolean) -> Unit
    ) : BaseWidget(100, 30) {

        init {
            val presets = presetsSupplier.get()
            val rows = ceil(presets.size / 8.0).toInt().coerceAtLeast(1)
            val size = (100 - 18) / 8
            this.height = 6 + rows * size + (rows - 1) * 2
        }

        override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ModSprites.ACCENT, x, y, width, height)
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ModSprites.BUTTON, x + 1, y + 1, width - 2, height - 2)

            val presets = presetsSupplier.get()
            val size = (width - 18) / 8

            presets.forEachIndexed { index, [presetColors, name] ->
                val j = index % 8
                val k = index / 8
                val cellX = x + 3 + (j * size) + (2 * j)
                val cellY = y + 3 + (k * size) + (2 * k)

                val previewColors = when (presetColors.size) {
                    0, 1 -> presetColors
                    else -> if (presetColors.first() != presetColors.last()) presetColors.toMutableList().apply { add(presetColors.first()) } else presetColors
                }

                graphics.animatedGradient(cellX, cellY, size, size, 5.0f, GradientDirection.RIGHT) { previewColors }
                graphics.outline(cellX, cellY, size, size, 0xFFDDDDDD.toInt())

                if (mouseX >= cellX && mouseX <= cellX + size && mouseY >= cellY && mouseY <= cellY + size) {
                    graphics.outline(cellX, cellY, size, size, 0xFF000000.toInt())

                    val tooltip = listOf(
                        Text.of(name) {
                            textShader = GradientTextShader { presetColors }
                        },
                        Text.of("Press shift to append.", TextColor.GRAY),
                    )
                    graphics.setComponentTooltipForNextFrame(McFont.self, tooltip, mouseX, mouseY)
                    this.applyCursor(graphics)
                }
            }
        }

        override fun mouseClicked(event: MouseButtonEvent, bl: Boolean): Boolean {
            if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                val presets = presetsSupplier.get()
                val size = (width - 18) / 8
                val mouseX = event.x().toInt()
                val mouseY = event.y().toInt()

                presets.forEachIndexed { index, [presetColors, _] ->
                    val j = index % 8
                    val k = index / 8
                    val cellX = x + 3 + (j * size) + (2 * j)
                    val cellY = y + 3 + (k * size) + (2 * k)

                    if (mouseX >= cellX && mouseX <= cellX + size && mouseY >= cellY && mouseY <= cellY + size) {
                        onSelect(presetColors, McScreen.isShiftDown)
                        return true
                    }
                }
            }
            return super.mouseClicked(event, bl)
        }

        override fun updateWidgetNarration(output: NarrationElementOutput) {}
    }

    enum class PresetType(val colors: List<Pair<List<Int>, String>>) {
        MC_COLORS(
            TextColor.BLACK to "Black",
            TextColor.DARK_BLUE to "Dark Blue",
            TextColor.DARK_GREEN to "Dark Green",
            TextColor.DARK_AQUA to "Dark Aqua",
            TextColor.DARK_RED to "Dark Red",
            TextColor.DARK_PURPLE to "Dark Purple",
            TextColor.GOLD to "Gold",
            TextColor.GRAY to "Gray",
            TextColor.DARK_GRAY to "Dark Gray",
            TextColor.BLUE to "Blue",
            TextColor.GREEN to "Green",
            TextColor.AQUA to "Aqua",
            TextColor.RED to "Red",
            TextColor.LIGHT_PURPLE to "Light Purple",
            TextColor.YELLOW to "Yellow",
            TextColor.WHITE to "White",
        ),
        SKYBLOCK_COLORS(
            SkyBlockColor.BLACK to "Black",
            SkyBlockColor.DARK_BLUE to "Dark Blue",
            SkyBlockColor.DARK_GREEN to "Dark Green",
            SkyBlockColor.DARK_AQUA to "Dark Aqua",
            SkyBlockColor.DARK_RED to "Dark Red",
            SkyBlockColor.DARK_PURPLE to "Dark Purple",
            SkyBlockColor.GOLD to "Gold",
            SkyBlockColor.GRAY to "Gray",
            SkyBlockColor.DARK_GRAY to "Dark Gray",
            SkyBlockColor.BLUE to "Blue",
            SkyBlockColor.GREEN to "Green",
            SkyBlockColor.AQUA to "Aqua",
            SkyBlockColor.RED to "Red",
            SkyBlockColor.LIGHT_PURPLE to "Light Purple",
            SkyBlockColor.YELLOW to "Yellow",
            SkyBlockColor.WHITE to "White",
        ),
        GRADIENTS(PrideShader.entries.map { it.getColors() to it.toFormattedName() }),
        ;

        constructor(vararg colors: Pair<Int, String>) : this(colors.map { [a, b] -> listOf(a) to b })

        private fun toFormattedName(): String {
            return name.lowercase().split("_").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
        }

        private val formatted = toFormattedName()
        override fun toString(): String = formatted
    }

    companion object {
        private const val SIZE = 16
        private const val PADDING = 4
        private const val SPACING = 2

        val POSITIVE_INTEGER_FILTER: Pattern = Pattern.compile("[0-9]*")
    }
}
