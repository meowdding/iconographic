package me.owdding.iconographic.utils.debug

import com.google.gson.JsonElement
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import me.owdding.ktmodules.Module
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.Style
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toDataOrThrow
import java.util.*

@Module
object TextToCodeThingy {

    private val CODEC = Codec.either(ComponentSerialization.CODEC.listOf(), ComponentSerialization.CODEC.xmap(::listOf) { it[0] }).xmap(Either<*, *>::unwrap, Either<List<Component>, *>::left)

    @Subscription
    context(event: RegisterIconCommandEvent)
    private fun command() = event.registerDevWithCallback("clipboardToCode") {
        val components = McClient.clipboard.readJson<JsonElement>().toDataOrThrow(CODEC)

        McClient.clipboard = buildString {
            components.forEach {
                appendLine(it.asCode())
            }
        }.also { println(it) }
    }

    fun Component.asCode(): String = buildString {
        val component = this@asCode
        val collector = mutableListOf<Pair<Style, String>>()
        component.visit(
            { style, text ->
                if (text.isEmpty()) return@visit Optional.empty()
                collector.add(style to text)
                Optional.empty<Unit>()
            },
            Style.EMPTY,
        )

        if (collector.isEmpty()) {
            appendLine("space()")
            return@buildString
        }
        if (collector.size == 1) {
            collector.first().let { [style, text] -> text.emitProperties(style) }
            return@buildString
        }

        appendLine("add {")
        collector.forEach { [style, text] -> text.emitProperties(style, false) }
        appendLine("}")
    }

    fun StringBuilder.indent(amount: Int = 1) = append("  ".repeat(amount))

    context(builder: StringBuilder)
    fun String.emitProperties(style: Style, single: Boolean = true) {

        builder.indent().append(if (single) "add(" else "append(").append('"').append(this).append('"')

        if (
            style.color == null
            && style.shadowColor == null
            && !style.isBold
            && !style.isItalic
            && !style.isUnderlined
            && !style.isStrikethrough
            && !style.isObfuscated
        ) {
            builder.appendLine(")")
            return
        }

        if (style.color != null && (
                style.shadowColor == null
                    && !style.isBold
                    && !style.isItalic
                    && !style.isUnderlined
                    && !style.isStrikethrough
                    && !style.isObfuscated
                //&& style.font == null
                )
        ) {
            builder.append(", ")
            val color = style.color.serialize()

            if (color.startsWith("#")) {
                builder.append("${style.color.value}")
            } else {
                builder.append("TextColor.${color.uppercase()}")
            }
            builder.appendLine(")")
            return
        }

        builder.appendLine(") {")

        if (style.color != null) {
            val color = style.color.serialize()

            if (color.startsWith("#")) {
                builder.indent(2).appendLine("color = ${style.color.value}")
            } else {
                builder.indent(2).appendLine("color = TextColor.${color.uppercase()}")
            }
        }
        if (style.shadowColor != null) builder.indent(2).appendLine("shadowColor = ${style.shadowColor}")
        if (style.isBold) builder.indent(2).appendLine("bold = ${style.isBold}")
        if (style.isItalic) builder.indent(2).appendLine("italic = ${style.isItalic}")
        if (style.isUnderlined) builder.indent(2).appendLine("underlined = ${style.isUnderlined}")
        if (style.isStrikethrough) builder.indent(2).appendLine("strikethrough = ${style.isStrikethrough}")
        if (style.isObfuscated) builder.indent(2).appendLine("obfuscated = ${style.isObfuscated}")
        //if (style.font != null) indent(2).appendLine("font = ${style.font}")

        builder.indent().appendLine("}")
    }

}
