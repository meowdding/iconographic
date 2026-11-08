package me.owdding.iconographic.config.previews

import com.mojang.blaze3d.platform.InputConstants
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import me.owdding.iconographic.config.AutoTranslated
import me.owdding.lib.builder.LayoutBuilder
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.*
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.builders.ItemBuilder
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.width
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.reflect.KMutableProperty0

@DslMarker
annotation class PreviewDsl

abstract class ConfigPreviewItem {

    abstract val item: ItemStack?
    val preview: ConfigPreview = buildPreview()

    inline fun ItemBuilder.customData(init: CompoundTagBuilder.() -> Unit) = this.set(DataComponents.CUSTOM_DATA, CustomData.of(CompoundTagBuilder(init)))

    context(builder: ItemBuilder)
    operator fun <Type : Any> DataComponentType<Type>.invoke(value: Type) = builder.set(this, value)

    fun buildPreview(): ConfigPreview {
        val settings = OptionsBuilder { this.globalSettings() }
        val sections = PreviewBuilder { this.buildPreview() }

        return ConfigPreview(settings, sections)
    }

    abstract fun OptionsBuilder.globalSettings()
    abstract fun PreviewBuilder.buildPreview()

    fun fullLore(): List<Component> = preview.sections.flatMap { it.lore }
}

@PreviewDsl
data class PreviewBuilder(val list: MutableList<ConfigPreviewSection> = mutableListOf()) {
    companion object {
        operator fun invoke(init: PreviewBuilder.() -> Unit): List<ConfigPreviewSection> = PreviewBuilder().apply(init).list
    }

    inline fun section(title: Component, init: SectionBuilder.() -> Unit) {
        list.add(SectionBuilder(title, init))
    }

    inline fun section(title: String, init: SectionBuilder.() -> Unit) {
        list.add(SectionBuilder(Text.of(title), init))
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun space() {
        list.add(SectionBuilder(CommonText.EMPTY) { lore { space() } })
    }

    inline fun loreSection(init: TooltipBuilder.() -> Unit) {
        list.add(SectionBuilder(CommonText.EMPTY) { lore(init) })
    }
}

@PreviewDsl
data class SectionBuilder(
    val title: Component,
    val lore: MutableList<Component> = mutableListOf(),
    val options: MutableList<ConfigOption> = mutableListOf(),
) {
    companion object {
        inline operator fun invoke(title: Component, init: SectionBuilder.() -> Unit) = SectionBuilder(title).apply(init).build()
    }


    inline fun lore(init: TooltipBuilder.() -> Unit) {
        this.lore.addAll(TooltipBuilder().apply(init).lines())
    }

    inline fun options(init: OptionsBuilder.() -> Unit) {
        this.options.addAll(OptionsBuilder(init))
    }

    fun build() = ConfigPreviewSection(title, lore, options)
}

@PreviewDsl
data class CompoundTagBuilder(val tag: CompoundTag) {
    companion object {
        inline operator fun invoke(init: CompoundTagBuilder.() -> Unit) = CompoundTagBuilder(CompoundTag()).apply(init).tag
    }

    operator fun String.invoke(value: Byte) = apply { tag.putByte(this, value) }
    operator fun String.invoke(value: Short) = apply { tag.putShort(this, value) }
    operator fun String.invoke(value: Int) = apply { tag.putInt(this, value) }
    operator fun String.invoke(value: Long) = apply { tag.putLong(this, value) }
    operator fun String.invoke(value: Float) = apply { tag.putFloat(this, value) }
    operator fun String.invoke(value: Double) = apply { tag.putDouble(this, value) }
    operator fun String.invoke(value: String) = apply { tag.putString(this, value) }
    operator fun String.invoke(value: ByteArray) = apply { tag.putByteArray(this, value) }
    operator fun String.invoke(value: IntArray) = apply { tag.putIntArray(this, value) }
    operator fun String.invoke(value: LongArray) = apply { tag.putLongArray(this, value) }
    operator fun String.invoke(value: Boolean) = apply { tag.putBoolean(this, value) }

    inline operator fun String.invoke(init: CompoundTagBuilder.() -> Unit) = apply {
        tag.put(this, CompoundTagBuilder(init))
    }

    operator fun String.get(vararg value: Byte) = apply { tag.put(this, ListTag().apply { value.map(ByteTag::valueOf).forEach(::add) }) }
    operator fun String.get(vararg value: Short) = apply { tag.put(this, ListTag().apply { value.map(ShortTag::valueOf).forEach(::add) }) }
    operator fun String.get(vararg value: Int) = apply { tag.put(this, ListTag().apply { value.map(IntTag::valueOf).forEach(::add) }) }
    operator fun String.get(vararg value: Long) = apply { tag.put(this, ListTag().apply { value.map(LongTag::valueOf).forEach(::add) }) }
    operator fun String.get(vararg value: Float) = apply { tag.put(this, ListTag().apply { value.map(FloatTag::valueOf).forEach(::add) }) }
    operator fun String.get(vararg value: Double) = apply { tag.put(this, ListTag().apply { value.map(DoubleTag::valueOf).forEach(::add) }) }
    operator fun String.get(vararg value: String) = apply { tag.put(this, ListTag().apply { value.map(StringTag::valueOf).forEach(::add) }) }
    operator fun String.get(vararg value: ByteArray) = apply { tag.put(this, ListTag().apply { value.map(::ByteArrayTag).forEach(::add) }) }
    operator fun String.get(vararg value: IntArray) = apply { tag.put(this, ListTag().apply { value.map(::IntArrayTag).forEach(::add) }) }
    operator fun String.get(vararg value: LongArray) = apply { tag.put(this, ListTag().apply { value.map(::LongArrayTag).forEach(::add) }) }
    operator fun String.get(vararg value: Boolean) = apply { tag.put(this, ListTag().apply { value.map(ByteTag::valueOf).forEach(::add) }) }

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun String.get(vararg value: CompoundTagBuilder.() -> Unit) = apply {
        tag.put(
            this,
            ListTag().apply {
                addAll(value.map(CompoundTagBuilder::invoke))
            },
        )
    }
}

data class ConfigPreview(
    val globalConfigs: List<ConfigOption>,
    val sections: List<ConfigPreviewSection>,
)

data class ConfigPreviewSection(
    val title: Component,
    val lore: List<Component> = listOf(),
    val options: List<ConfigOption> = listOf(),
)

@PreviewDsl
data class OptionsBuilder(val options: MutableList<ConfigOption> = mutableListOf()) {
    companion object {
        inline operator fun invoke(init: OptionsBuilder.() -> Unit): List<ConfigOption> = OptionsBuilder().apply(init).options
    }


    fun <Owner : AutoTranslated> toggle(owner: Owner, property: KMutableProperty0<Boolean>) {
        val name = owner.createKey(property.name)
        options.add(
            BooleanOption(
                property::set,
                property::get,
                Component.translatable(name),
                Component.translatable("$name.desc"),
            ),
        )
    }

    inline fun <Owner : AutoTranslated, reified T : Enum<T>> enum(owner: Owner, property: KMutableProperty0<T>) {
        val name = owner.createKey(property.name)
        options.add(
            EnumOption(
                property.get(),
                property::set,
                property::get,
                Component.translatable(name),
                Component.translatable("$name.desc"),
            ),
        )
    }
}

data class BooleanOption(
    val setter: (Boolean) -> Unit,
    val getter: () -> Boolean,
    val name: Component,
    val description: Component,
) : ConfigOption {
    context(layout: LayoutBuilder)
    override fun visualize() {
        layout.widget(
            Widgets.button {
                it.withTexture(null)
                it.withSize(10, McFont.height)
                it.withRenderer(
                    WidgetRenderers.text(
                        Text.of {
                            append(name)
                            append(": ")
                            color = TextColor.GOLD
                            append {
                                color = if (getter()) TextColor.GREEN else TextColor.RED
                                append(if (getter()) "On" else "Off")
                            }
                        }.also { title -> it.width = title.width },
                    ),
                )
                it.withCallback {
                    setter(!getter())
                    McScreen.refreshScreen()
                }
                it.withTooltip(
                    Text.of {
                        color = TextColor.GRAY
                        append(description)
                    },
                )
            },
        )

    }
}

data class EnumOption<T : Enum<T>>(
    val enum: T,
    val enumEntries: List<T>,
    val setter: (T) -> Unit,
    val getter: () -> T,
    val name: Component,
    val description: Component,
) : ConfigOption {
    companion object {
        inline operator fun <reified T : Enum<T>> invoke(
            enum: T,
            noinline setter: (T) -> Unit,
            noinline getter: () -> T,
            name: Component,
            description: Component,
        ): EnumOption<T> = EnumOption(enum, T::class.java.enumConstants.toList(), setter, getter, name, description)
    }

    context(layout: LayoutBuilder)
    override fun visualize() {
        layout.widget(
            Widgets.button {
                it.withTexture(null)
                it.withSize(10, McFont.height)
                it.withRenderer(
                    WidgetRenderers.text(
                        Text.of {
                            append(name)
                            append(": ")
                            color = TextColor.GOLD
                            append {
                                color = TextColor.YELLOW
                                append(getter().toComponent())
                            }
                        }.also { title -> it.width = title.width },
                    ),
                )
                it.withCallback(InputConstants.MOUSE_BUTTON_LEFT) {
                    setter(enumEntries[(enumEntries.size + getter().ordinal + 1) % enumEntries.size])
                    McScreen.refreshScreen()
                }
                it.withCallback(InputConstants.MOUSE_BUTTON_RIGHT) {
                    setter(enumEntries[(enumEntries.size + getter().ordinal - 1) % enumEntries.size])
                    McScreen.refreshScreen()
                }
                it.withTooltip(
                    Text.of {
                        color = TextColor.GRAY
                        append(description)
                        append("\n\n")
                        append("Options", TextColor.GREEN)
                        enumEntries.forEach {
                            append {
                                color = if (getter() == it) TextColor.GOLD else TextColor.YELLOW
                                append("\n ")
                                if (getter() == it) {
                                    append("> ")
                                }
                                append(it.toComponent())
                            }
                        }
                    },
                )
            },
        )

    }

    private fun <T: Enum<T>> T.toComponent(): Component = when (this) {
        is Translatable -> Component.translatable(this.translationKey)
        else -> Component.literal(this.name.toTitleCase())
    }
}

// todo: better idk someone is hater number 1
fun McScreen.refreshScreen() {
    self?.let { it.resize(it.width, it.height) }
}

interface ConfigOption {

    context(layout: LayoutBuilder)
    fun visualize()
}
