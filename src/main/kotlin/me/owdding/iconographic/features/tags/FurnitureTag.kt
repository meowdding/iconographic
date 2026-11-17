package me.owdding.iconographic.features.tags

import me.owdding.iconographic.TooltipLine
import me.owdding.iconographic.config.categories.tag.TagConfig
import me.owdding.iconographic.system.RegisterFeature
import me.owdding.iconographic.system.Result
import me.owdding.iconographic.system.TooltipFeatureWithContext
import me.owdding.iconographic.system.TooltipTag
import me.owdding.iconographic.utils.RemoteStrings
import me.owdding.iconographic.utils.chat.CatppuccinColors
import me.owdding.lib.utils.strings.StringGroup.Companion.resolve
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import java.util.concurrent.atomic.AtomicBoolean

@RegisterFeature
data object FurnitureTag : TooltipFeatureWithContext<AtomicBoolean>() {
    override val enabled: Boolean get() = TagConfig.furniture
    override fun createContext(): AtomicBoolean = AtomicBoolean(false)
    override val priority: Int = 2

    private val furnitureRegex by RemoteStrings.resolve().regex("(?i)Furniture")

    context(context: AtomicBoolean)
    override fun ItemStack.leftTags(): List<TooltipTag> {
        val furniture = context.get()
        if (!furniture) {
            return emptyList()
        }

        return listOf(TooltipTag.literal("Furniture", CatppuccinColors.Mocha.peach))
    }

    context(context: AtomicBoolean)
    override fun ItemStack.modifyEntries(list: MutableList<TooltipLine>, previousResult: Result?): Result = withComponentMerger(list) {
        if (!hasNext { it.stripped.lowercase().matches(furnitureRegex) }) return@withComponentMerger Result.modified
        addUntil { it.stripped.lowercase().matches(furnitureRegex) }
        if (!canRead()) return@withComponentMerger Result.unmodified
        read()
        skipSpace()

        context.set(true)

        Result.modified
    }
}
