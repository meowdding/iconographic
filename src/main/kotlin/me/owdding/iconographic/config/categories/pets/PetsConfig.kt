package me.owdding.iconographic.config.categories.pets

import com.teamresourceful.resourcefulconfig.api.types.info.Translatable
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import me.owdding.iconographic.config.AutoTranslated
import me.owdding.lib.extensions.shorten
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString

object PetsConfig : CategoryKt("pets"), AutoTranslated {
    override val translationBase: String = "iconographic.config.pets"
    override val name: TranslatableValue = Translated(translationBase)

    init { autoSeparator("main") }
    var petAbilities by autoBoolean(true)
    var petHeldItem by autoBoolean(true)
    var petLevel by autoBoolean(true)
    var experienceFormat by autoEnum(PetDisplayType.INTEGER)
    var maxExperienceFormat by autoEnum(PetDisplayType.SHORTENED)
    var petFavourite by autoBoolean(true)
}

enum class PetDisplayType : Translatable {
    SHORTENED {
        override fun format(number: Number): String = number.shorten()
    },
    DECIMALS {
        override fun format(number: Number): String = number.toFormattedString()
    },
    INTEGER {
        override fun format(number: Number): String = number.toInt().toFormattedString()
    },
    ;

    override fun getTranslationKey(): String = PetsConfig.translationBase + ".display_type.${this.name.lowercase()}"


    operator fun invoke(number: Number): String = format(number)

    abstract fun format(number: Number): String
}
