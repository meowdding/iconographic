package me.owdding.iconographic.config.categories.misc

import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.iconographic.config.AutoTranslated
import me.owdding.iconographic.config.GradientColorOption
import me.owdding.iconographic.config.GradientColorOptionWidget
import me.owdding.iconographic.features.ClickFeature
import me.owdding.iconographic.utils.chat.DisplayColor

object MiscConfig : CategoryKt("misc"), AutoTranslated {
    override val translationBase: String = "iconographic.config.misc"
    override val name: TranslatableValue = Translated(translationBase)

    init { autoSeparator("misc") }
    val alignedItemAbility by autoBoolean(false)
    val clickIcons by autoEnum(ClickFeature.Style.REPLACE)
    val enchantedBookNames by autoBoolean(true)
    val skillLevelBar by autoBoolean(true)

    init { autoSeparator("enchantments") }
    val itemEnchants by autoBoolean(true)
    val romanEnchant by autoBoolean(true)
    val smallEnchantText by autoBoolean(true)
    val enchantNameBackground by autoBoolean(false)
    val centerEnchantments by autoBoolean(true)
    val stackingProgress by autoBoolean(true)
    val ultimateEnchantmentColor by transform(
        autoString(GradientColorOptionWidget.State(mutableListOf(DisplayColor.LIGHT_PURPLE)).asJson) {
            this.renderer = GradientColorOption.ID
        },
        { it.asJson },
        { GradientColorOptionWidget.State.fromString(it) },
    )
    val normalEnchantmentColor by transform(
        autoString(GradientColorOptionWidget.State(mutableListOf(DisplayColor.BLUE)).asJson) {
            this.renderer = GradientColorOption.ID
        },
        { it.asJson },
        { GradientColorOptionWidget.State.fromString(it) },
    )

    val lockedEnchantmentLevelColor by transform(
        autoString(GradientColorOptionWidget.State(mutableListOf(DisplayColor.GRAY)).asJson) {
            this.renderer = GradientColorOption.ID
        },
        { it.asJson },
        { GradientColorOptionWidget.State.fromString(it) },
    )

    val normalEnchantmentLevelColor by transform(
        autoString(GradientColorOptionWidget.State(mutableListOf(DisplayColor.BLUE)).asJson) {
            this.renderer = GradientColorOption.ID
        },
        { it.asJson },
        { GradientColorOptionWidget.State.fromString(it) },
    )

    val maxEnchantmentLevelColor by transform(
        autoString(GradientColorOptionWidget.State(mutableListOf(DisplayColor.GOLD)).asJson) {
            this.renderer = GradientColorOption.ID
        },
        { it.asJson },
        { GradientColorOptionWidget.State.fromString(it) },
    )
}
