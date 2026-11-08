package me.owdding.iconographic.config.previews

import com.mojang.authlib.properties.Property
import me.owdding.iconographic.config.categories.misc.MiscConfig
import me.owdding.iconographic.config.categories.pets.PetsConfig
import me.owdding.iconographic.config.categories.tag.TagConfig
import me.owdding.iconographic.config.categories.visuals.VisualsConfig
import me.owdding.ktmodules.Module
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import tech.thatgravyboat.skyblockapi.platform.GameProfile
import tech.thatgravyboat.skyblockapi.platform.toResolvableProfile
import tech.thatgravyboat.skyblockapi.utils.builders.ItemBuilder
import tech.thatgravyboat.skyblockapi.utils.lazy.registryBoundLazy
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.strikethrough

@Module
object PetPreview : ConfigPreviewItem() {
    private const val TEXTURE =
        "ewogICJ0aW1lc3RhbXAiIDogMTczMzM5MDYyOTQ2NiwKICAicHJvZmlsZUlkIiA6ICJjN2Y2Nzk3ZmE4ZGM0NTdiYTkxNTU0NWIwMGU3M2UzMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJlZGd5c3BlbmNlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y5NGM3MTE3ZmVkZTk2NDE2MGIwZjllNTk1MGVkZDFhYjRhZjAzMGY4NjVlNTYyNTI0NGE3NTlkNWZhMmY2OWYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="

    override val item: ItemStack? by registryBoundLazy {
        ItemBuilder(Items.PLAYER_HEAD) {
            DataComponents.TOOLTIP_STYLE(Identifier.parse("hypixel_skyblock:legendary"))
            DataComponents.LORE(ItemLore(this@PetPreview.fullLore().drop(1)))
            DataComponents.CUSTOM_NAME(this@PetPreview.fullLore().first())
            DataComponents.PROFILE(
                GameProfile {
                    put("textures", Property("textures", TEXTURE))
                }.toResolvableProfile(),
            )
            customData {
                "id"("PET")
                "uuid"("bcb12cfd-5e05-41ec-8589-ac08a18add85")
                "petInfo"("{\"type\":\"GOLDEN_DRAGON\",\"active\":true,\"exp\":1.3193008421268465E8,\"tier\":\"LEGENDARY\",\"hideInfo\":false,\"heldItem\":\"DWARF_TURTLE_SHELMET\",\"candyUsed\":1,\"skin\":\"GOLDEN_DRAGON_SWAP_PLUSHIE\",\"uuid\":\"bcb12cfd-5e05-41ec-8589-ac08a18add85\",\"uniqueId\":\"400c145d-6cf9-4353-a244-f069b098d9fc\",\"hideRightClick\":false,\"noMove\":false,\"extraData\":{\"favorite_plushie_gdragon\":12.0},\"petSoulbound\":false}")
            }
        }
    }

    override fun OptionsBuilder.globalSettings() {
        toggle(VisualsConfig, VisualsConfig::spinny)
        toggle(TagConfig, TagConfig::rarity)
        toggle(TagConfig, TagConfig::category)
        toggle(TagConfig, TagConfig::pet)
        toggle(TagConfig, TagConfig::petCandy)
        toggle(PetsConfig, PetsConfig::petFavourite)
    }

    override fun PreviewBuilder.buildPreview() {
        section("Header") {
            lore {
                add {
                    append("⭐ ", TextColor.YELLOW)
                    append("[Lvl 156] ", TextColor.GRAY)
                    append("Golden Dragon", TextColor.GOLD)
                    append(" ✦", TextColor.DARK_RED)
                }
                add("Combat Pet, Super Plushie Skin", TextColor.DARK_GRAY)
            }
        }

        space()

        statsSection {
            add {
                append("True Defense: ", TextColor.GRAY)
                append("+10", TextColor.WHITE)
            }

            add {
                append("Strength: ", TextColor.GRAY)
                append("+159", TextColor.RED)
            }

            add {
                append("Attack Speed: ", TextColor.GRAY)
                append("+39%", TextColor.YELLOW)
            }
        }

        space()

        section("Pet Ability") {
            options {
                toggle(PetsConfig, PetsConfig::petAbilities)
            }
            lore {
                add("Gold's Power", TextColor.GOLD)

                add {
                    append("Increase the potency of ", TextColor.GRAY)
                    append("Midas'", TextColor.GOLD)
                }

                add {
                    append("Sword", TextColor.GOLD)
                    append(" and ", TextColor.GRAY)
                    append("Midas Staff's ", TextColor.GOLD)
                    append("Greed Ability", TextColor.DARK_PURPLE)
                }

                add {
                    append("by ", TextColor.GRAY)
                    append("7.8%", TextColor.GREEN)
                    append(".", TextColor.GRAY)
                }

                space()

                add("Shining Scales", TextColor.GOLD)

                add {
                    append("Grants ", TextColor.GRAY)
                    append("+15 Strength ", TextColor.RED)
                    append("to your pet", TextColor.GRAY)
                }

                add {
                    append("for each digit in your ", TextColor.GRAY)
                    append("Gold Collection", TextColor.GOLD)
                    append(".", TextColor.GRAY)
                }

                add("(Max 100M collection)", TextColor.DARK_GRAY)

                space()

                add("Dragon's Greed", TextColor.GOLD)

                add {
                    append("Grants ", TextColor.GRAY)
                    append("+0.39%", TextColor.RED)
                    append(" Strength ", TextColor.RED)
                    append("per ", TextColor.GRAY)
                    append("5", TextColor.AQUA)
                    append("", TextColor.AQUA)
                }

                add {
                    append("Magic Find", TextColor.AQUA)
                    append(". ", TextColor.GRAY)
                    append("(Max +3.9%)", TextColor.DARK_GRAY)
                }

                space()

                add("Legendary Treasure", TextColor.GOLD)

                add {
                    append("Gain ", TextColor.GRAY)
                    append("0.195% ", TextColor.RED)
                    append("damage for every million", TextColor.GRAY)
                }

                add {
                    append("coins in your bank. ", TextColor.GRAY)
                    append("(Max 195%)", TextColor.DARK_GRAY)
                }
            }
        }

        space()

        section("Held Item") {
            options {
                toggle(PetsConfig, PetsConfig::petHeldItem)
            }
            lore {
                add {
                    append("Held Item: ", TextColor.GOLD)
                    append("Dwarf Turtle Shelmet", TextColor.DARK_PURPLE)
                }

                add {
                    append("Grants ", TextColor.GRAY)
                    append("+10 True Defense ", TextColor.WHITE)
                    append("and", TextColor.GRAY)
                }

                add("makes the pet's owner immune to", TextColor.GRAY)

                add("knockback.", TextColor.GRAY)
            }
        }

        space()

        section("Pet Candy") {
            lore {
                add("(1/10) Pet Candy Used", TextColor.GREEN)
                space()
            }
        }

        section("Pet Level") {
            options {
                toggle(PetsConfig, PetsConfig::petLevel)
                enum(PetsConfig, PetsConfig::experienceFormat)
                enum(PetsConfig, PetsConfig::maxExperienceFormat)
            }
            lore {
                add {
                    append("Progress to Level 157: ", TextColor.GRAY)
                    append("48.9%", TextColor.YELLOW)
                }

                add {
                    append("             ") {
                        color = TextColor.DARK_GREEN
                        bold = true
                        strikethrough = true
                    }
                    append("            ") {
                        color = TextColor.WHITE
                        bold = true
                        strikethrough = true
                    }
                    append(" ")
                    append("921,654.2", TextColor.YELLOW)
                    append("/", TextColor.GOLD)
                    append("1.9M", TextColor.YELLOW)
                }
            }
        }

        space()

        section("Click Icons") {
            options {
                enum(MiscConfig, MiscConfig::clickIcons)
            }
            lore {
                add("Click to despawn!", TextColor.RED)
                add("Shift Left-click to toggle as favorite!", TextColor.YELLOW)
                add("Right-click to convert to an item!", TextColor.YELLOW)
            }
        }
    }
}
