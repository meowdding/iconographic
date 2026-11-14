package me.owdding.iconographic.features.tags

import me.owdding.iconographic.TooltipLine
import me.owdding.iconographic.config.categories.tag.TagConfig
import me.owdding.iconographic.system.RegisterFeature
import me.owdding.iconographic.system.Result
import me.owdding.iconographic.system.TooltipFeatureWithContext
import me.owdding.iconographic.system.TooltipTag
import me.owdding.lib.rendering.text.builtin.GradientTextShader
import me.owdding.lib.rendering.text.textShader
import me.owdding.lib.utils.GradientDirection
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.substring

@RegisterFeature
data object PetTags : TooltipFeatureWithContext<MutableList<TooltipTag>>() {
    override val enabled: Boolean get() = TagConfig.pet
    override fun createContext(): MutableList<TooltipTag> = mutableListOf()
    override val priority: Int = 2

    val petCategories = buildMap {
        fun pet(skill: String, color: Int, combine: Boolean) {
            put(
                skill,
                PetCategory(skill, color = color, combine = combine) {
                    it.substring("$skill Pet, ".length)
                },
            )
        }

        fun mount(skill: String, color: Int) {
            put(
                "$skill Mount",
                PetCategory(skill, "Mount", color = color) {
                    it.substring("$skill Mount, ".length)
                },
            )
        }

        put("All Skills", PetCategory(emptyList(), color = 0xAAAAAA))
        pet("Mining", 0xFF8C00, true)
        mount("Mining", 0xD97706)
        pet("Combat", 0xEF4444, true)
        mount("Combat", 0xDC2626)
        put(
            "Combat Morph",
            PetCategory("Combat", "Morph", color = 0xA72323) {
                it.substring("Combat Morph, ".length)
            },
        )
        pet("Foraging", 0x22C55E, true)
        pet("Enchanting", 0x7A33ED, true)
        pet("Taming", 0xEC4899, true)
        pet("Farming", 0xEAB308, true)
        put(
            "Gabagool Pet, feed to gain XP",
            PetCategory("Gabagool", color = 0xC084FC) {
                it.substring("Gabagool Pet, ".length)
            },
        )
        pet("Alchemy", 0x5190F4, true)
        pet("Fishing", 0x2F5ADB, true)
        pet("Fractured Soul", 0x9C56DC, false)
    }.mapKeys { (key) -> key.lowercase() }

    fun String.removeSkin() = if (this.endsWith("Skin")) this.substringBeforeLast(",") else this

    context(context: MutableList<TooltipTag>)
    override fun ItemStack.leftTags(): List<TooltipTag> = context

    context(context: MutableList<TooltipTag>)
    override fun ItemStack.modifyEntries(list: MutableList<TooltipLine>, previousResult: Result?): Result = withComponentMerger(list) {
        if (!hasNext { it.stripped.removeSkin().lowercase().split("/").all { c -> c.removeSuffix(" pet") in petCategories } }) return@withComponentMerger Result.modified
        addUntil {
            it.stripped.removeSkin().lowercase().split("/").all { c -> c.removeSuffix(" pet") in petCategories }
        }
        if (!canRead()) return@withComponentMerger Result.unmodified
        val line = read()
        val strippedLine = line.stripped.removeSkin().trim().lowercase()

        if (strippedLine.contains("/")) {
            skipSpace()
            val modifiedLine = strippedLine.removeSuffix("pet").trim()
            // TODO: maybe linemodifier support
            val categories = modifiedLine.split("/").map { petCategories[it.trim().removeSuffix(" pet")] ?: return@withComponentMerger Result.unmodified }
            val colors = categories.map { it.color }.toMutableList().apply { add(first()) }
            context.add(
                TooltipTag.gradientLiteral(
                    Text.of(modifiedLine) {
                        textShader = GradientTextShader(colors, speed = 3f)
                    },
                ) { colors },
            )
        } else {
            val category = petCategories[strippedLine.removeSuffix(" pet")] ?: return@withComponentMerger Result.unmodified
            if ((category.lineModifier != null)) {
                val modifiedLine = category.lineModifier(line)
                if (modifiedLine != Component.empty()) {
                    add(modifiedLine)
                }
            } else {
                skipSpace()
            }

            context.addAll(category.tags.map { TooltipTag.literal(it, color = category.color) })
        }
        Result.modified
    }

    data class PetCategory(
        val tags: List<String>,
        val color: Int = -1,
        val combine: Boolean = false,
        val lineModifier: ((Component) -> Component)? = null,
    ) {
        constructor(vararg tags: String, color: Int = -1, combine: Boolean = false, lineModifier: ((Component) -> Component)? = null) :
            this(tags.asList(), color, combine, lineModifier)
    }
}
