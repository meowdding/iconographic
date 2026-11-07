package me.owdding.iconographic.features

import me.owdding.iconographic.ComponentAlignment
import me.owdding.iconographic.ComponentLike
import me.owdding.iconographic.TooltipLine
import me.owdding.iconographic.config.categories.visuals.VisualsConfig
import me.owdding.iconographic.lines.SpacerLine
import me.owdding.iconographic.render.SeparatorRenderer
import me.owdding.iconographic.system.RegisterFeature
import me.owdding.iconographic.system.Result
import me.owdding.iconographic.system.TooltipFeature
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.match
import tech.thatgravyboat.skyblockapi.utils.regex.component.ComponentRegex
import tech.thatgravyboat.skyblockapi.utils.regex.component.findGroup
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.strikethrough
import java.util.*
import kotlin.jvm.optionals.getOrNull

@RegisterFeature
data object BetterSeparators : TooltipFeature() {
    override val enabled: Boolean get() = VisualsConfig.separators
    override val priority: Int = 10

    private val namedSeparatorComponentRegex = ComponentRegex("=====\\[ (?<name>.*) ]=====")
    private val namedSeparatorRegex = namedSeparatorComponentRegex.regex()

    override fun ItemStack.modifyEntries(list: MutableList<TooltipLine>, previousResult: Result?): Result = withComponentMerger(list) {
        if (!hasNext { it.isSeparator() || it.isNamedSeparator() }) return@withComponentMerger Result.unmodified

        addUntil { it.isSeparator() || it.isNamedSeparator() }

        val line = peek()
        when {
            line.isNamedSeparator() -> {
                val headerComponent = namedSeparatorComponentRegex.findGroup(line, "name")?.copy()?.withoutShadow() ?: return@withComponentMerger Result.unmodified

                read()
                originalMerger.add(SpacerLine(height = 3))
                originalMerger.add(ComponentLike(headerComponent, ComponentAlignment.Center, lines = true))
                originalMerger.add(SpacerLine(height = 3))
            }

            line.isSeparator() -> {
                read()
                originalMerger.add(SpacerLine(height = 3))
                originalMerger.add(SeparatorRenderer)
                originalMerger.add(SpacerLine(height = 3))
            }
        }

        Result.modified
    }

    private val isStrikethrough: FormattedText.StyledContentConsumer<Boolean> = { style, _ -> if (style.isStrikethrough) Optional.of(true) else Optional.empty() }

    private fun Component.isSeparator() = stripped.isBlank() && (this.strikethrough || visit(isStrikethrough, Style.EMPTY).getOrNull() == true)

    private fun Component.isNamedSeparator() = namedSeparatorRegex.match(stripped)
}
