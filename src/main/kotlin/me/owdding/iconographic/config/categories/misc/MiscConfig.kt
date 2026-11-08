package me.owdding.iconographic.config.categories.misc

import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.iconographic.config.AutoTranslated
import me.owdding.iconographic.features.ClickFeature

object MiscConfig : CategoryKt("misc"), AutoTranslated {
    override val translationBase: String = "iconographic.config.misc"
    override val name: TranslatableValue = Translated(translationBase)

    init { autoSeparator("misc") }
    val alignedItemAbility by autoBoolean(false)
    var clickIcons by autoEnum(ClickFeature.Style.REPLACE)
    val enchantedBookNames by autoBoolean(true)
    val skillLevelBar by autoBoolean(true)

    init { autoSeparator("enchantments") }
    var itemEnchants by autoBoolean(true)
    var romanEnchant by autoBoolean(true)
    var smallEnchantText by autoBoolean(true)
    var enchantNameBackground by autoBoolean(false)
    var centerEnchantments by autoBoolean(true)
    var stackingProgress by autoBoolean(true)
}
