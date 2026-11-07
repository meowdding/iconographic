package me.owdding.iconographic.config.previews

import me.owdding.lib.builder.LayoutBuilder
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.*
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import tech.thatgravyboat.skyblockapi.utils.builders.ItemBuilder
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.text.Text
import kotlin.reflect.KMutableProperty0

@DslMarker
annotation class PreviewDsl

abstract class ConfigPreviewItem {

    abstract val item: ItemStack?
    val preview: ConfigPreview = buildPreview()

    inline fun ItemBuilder.customData(init: CompoundTagBuilder.() -> Unit) = this.set(DataComponents.CUSTOM_DATA, CustomData.of(CompoundTagBuilder(init)))

    fun buildPreview(): ConfigPreview {
        val settings = OptionsBuilder { this.globalSettings() }
        val sections = PreviewBuilder { this.buildPreview() }

        return ConfigPreview(settings, sections)
    }

    abstract fun OptionsBuilder.globalSettings()
    abstract fun PreviewBuilder.buildPreview()
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
    val lore: List<Component>,
    val options: List<ConfigOption>,
)

@PreviewDsl
data class OptionsBuilder(val options: MutableList<ConfigOption> = mutableListOf()) {
    companion object {
        inline operator fun invoke(init: OptionsBuilder.() -> Unit): List<ConfigOption> = OptionsBuilder().apply(init).options
    }

    fun toggle(property: KMutableProperty0<Boolean>) {
        print(property)
    }
}

interface Thingy {
    fun set(value: Boolean)
    fun get(): Boolean
}

data class BooleanOption(
    val setter: (Boolean) -> Unit,
    val getter: (Boolean) -> Unit,
    val name: Component,
    val description: Component,
) : ConfigOption {
    context(layout: LayoutBuilder)
    override fun visualize() {
        TODO("Not yet implemented")
    }
}


interface ConfigOption {

    context(layout: LayoutBuilder)
    fun visualize()
}
