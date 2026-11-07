package me.owdding.iconographic.config.categories.visuals

import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.iconographic.config.AutoTranslated
import me.owdding.iconographic.features.stats.ExtraStatDisplay

object VisualsConfig : CategoryKt("visuals"), AutoTranslated {
    override val translationBase: String = "iconographic.config.visuals"
    override val name: TranslatableValue = Translated(translationBase)

    init { autoSeparator("general") }
    var spinny by autoBoolean(false)
    var vanillaBackground by autoBoolean(true)
    var forceDefaultVanillaBackground by autoBoolean(false)
    var skyBlockColor by autoBoolean(true)
    var replaceNoRarityColor by autoBoolean(false)
    var customNoRarityColor by autoColor(0xFFFFFF)



    init { autoSeparator("text_formatting") }
    var separators by autoBoolean(true)
    var shinyHolographic by autoBoolean(true)
    var alignedStats by autoBoolean(true)
    var colorStatName by autoBoolean(false)
    var extraStatsDisplay by autoEnum(ExtraStatDisplay.NORMAL)
}
