package me.owdding.iconographic.config.categories.farming

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.iconographic.config.AutoTranslated

object FarmingConfig : CategoryKt("farming"), AutoTranslated {
    override val translationBase: String = "iconographic.config.farming"

    val toolLevelBar by autoBoolean(true)
}
