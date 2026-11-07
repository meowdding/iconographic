package me.owdding.iconographic.config.previews

import me.owdding.iconographic.config.categories.mining.MiningConfig
import me.owdding.iconographic.config.categories.visuals.VisualsConfig
import me.owdding.ktmodules.Module
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.builders.ItemBuilder
import tech.thatgravyboat.skyblockapi.utils.lazy.registryBoundLazy
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.obfuscated

@Module
object Silly1 : ConfigPreviewItem() {
    override val item: ItemStack? by registryBoundLazy {
        ItemBuilder(Items.DIAMOND_PICKAXE) {

            DataComponents.ITEM_MODEL(Identifier.parse("hypixel_skyblock:item/uncategorized/divans_drill"))
            DataComponents.TOOLTIP_STYLE(Identifier.parse("hypixel_skyblock:supreme"))
            customData {
                "rarity_upgrades"(1)
                "polarvoid"(5)
                "drill_fuel"(52704)
                "modifier"("heated")
                "compact_blocks"(4822278)
                "power_ability_scroll"("AMBER_POWER_SCROLL")
                "enchantments" {
                    "efficiency"(10)
                    "lapidary"(5)
                    "fortune"(4)
                    "smelting_touch"(1)
                    "compact"(10)
                    "paleontologist"(5)
                    "pristine"(5)
                    "telekinesis"(1)
                    "ultimate_flowstate"(3)
                    "experience"(4)
                }
                "uuid"("536142ef-37be-4faa-b8ab-f07c1cee9240")
                "divan_powder_coating"(1)
                "drill_part_upgrade_module"("goblin_omelette_blue_cheese")
                "gems" {
                    "JADE_1"("PERFECT")
                    "JADE_0"("PERFECT")
                    "MINING_0"("PERFECT")
                    "unlocked_slots"[
                        "JADE_0",
                        "MINING_0",
                        "JADE_1",
                    ]
                    "AMBER_0" {
                        "uuid"("7600113b-b53f-4642-ba10-c5c81b0773f0")
                        "quality"("PERFECT")
                    }
                    "AMBER_1"("PERFECT")
                    "MINING_0_gem"("TOPAZ")
                }
                "drill_part_engine"("amber_polished_drill_engine")
                "id"("DIVAN_DRILL")
                "drill_part_fuel_tank"("perfectly_cut_fuel_tank")
                "donated_museum"(1)
                "timestamp"(1648413300000)
            }
        }
    }

    override fun OptionsBuilder.globalSettings() {
        toggle(MiningConfig, MiningConfig::drillComponents)
        toggle(VisualsConfig, VisualsConfig::spinny)
    }

    override fun PreviewBuilder.buildPreview() {
        section(Text.of("Header")) {
            lore {
                add("Heated Divan's Drill", TextColor.AQUA)
                add("Breaking Power 10", TextColor.DARK_GRAY)
            }
        }

        section("Stats") {
            options {
                toggle(VisualsConfig, VisualsConfig::alignedStats)
                toggle(VisualsConfig, VisualsConfig::colorStatName)
            }

            lore {
                add("Damage: ") {
                    color = TextColor.GRAY
                    append("+75", TextColor.RED)
                }
                add("Mining Speed: ") {
                    color = TextColor.GRAY
                    append("+3,260 ", TextColor.GOLD)
                    append("[+500] ", TextColor.GOLD)
                    append("[+50] ", TextColor.BLUE)
                    append("(+70) ", TextColor.BLUE)
                    append("(+240)", TextColor.LIGHT_PURPLE)
                }
                add("Pristine: ") {
                    color = TextColor.GRAY
                    append("+4.7 ", TextColor.DARK_PURPLE)
                    append("(+2.2)", TextColor.LIGHT_PURPLE)
                }
                add("Mining Fortune: ") {
                    color = TextColor.GRAY
                    append("+445 ", TextColor.GOLD)
                    append("[+5] ", TextColor.BLUE)
                    append("(+25)", TextColor.LIGHT_PURPLE)
                }
                add("Gemstone Fortune: ") {
                    color = TextColor.GRAY
                    append("+50", TextColor.GOLD)
                }
                add("Mining Wisdom: ") {
                    color = TextColor.GRAY
                    append("+10", TextColor.DARK_AQUA)
                }
            }
        }
        section("Mrrp") {

            lore {
                add {
                    append("Gemstones: ", TextColor.GRAY)
                    append("[", TextColor.GOLD)
                    append("", TextColor.GOLD)
                    append("] ", TextColor.GOLD)
                    append("[", TextColor.GOLD)
                    append("", TextColor.GOLD)
                    append("] ", TextColor.GOLD)
                    append("[", TextColor.GOLD)
                    append("", TextColor.GREEN)
                    append("] ", TextColor.GOLD)
                    append("[", TextColor.GOLD)
                    append("", TextColor.GREEN)
                    append("] ", TextColor.GOLD)
                    append("[", TextColor.GOLD)
                    append("✦", TextColor.YELLOW)
                    append("]", TextColor.GOLD)
                }

                space()

                add("Flowstate III") {
                    color = TextColor.LIGHT_PURPLE
                    bold = true
                }

                add("Compact X", TextColor.BLUE)

                add("Efficiency X", TextColor.BLUE)

                add("Experience IV", TextColor.BLUE)

                add("Fortune IV", TextColor.BLUE)

                add("Lapidary V", TextColor.BLUE)

                add("Paleontologist V", TextColor.BLUE)

                add("Prismatic V", TextColor.BLUE)

                add("Smelting Touch I", TextColor.BLUE)

                space()

                add("Perfectly-Cut Fuel Tank", TextColor.GREEN)

                add("100,000 Max Fuel Capacity", TextColor.DARK_GREEN)

                add("-10% Pickaxe Ability Cooldown", TextColor.GREEN)

                space()

                add("Amber-Polished Drill Engine", TextColor.GREEN)

                add("+600 Mining Speed", TextColor.GOLD)

                add("+100 Mining Fortune", TextColor.GOLD)

                space()

                add("Blue Cheese Goblin Omelette Part", TextColor.GREEN)

                add {
                    append("Adds ", TextColor.GRAY)
                    append("+1 Level ", TextColor.GREEN)
                    append("to all of your unlocked ", TextColor.GRAY)
                    append("Heart of", TextColor.DARK_PURPLE)
                }

                add {
                    append("the Mountain ", TextColor.DARK_PURPLE)
                    append("perks.", TextColor.GRAY)
                }

                space()

                add {
                    append("Fuel: ", TextColor.GRAY)
                    append("52,704", TextColor.DARK_GREEN)
                    append("/100k", TextColor.DARK_GRAY)
                }

                space()

                add("Heated Bonus", TextColor.BLUE)

                add {
                    append("Grants ", TextColor.GRAY)
                    append("+1 Mining Fortune ", TextColor.GOLD)
                    append("and ", TextColor.GRAY)
                    append("+4", TextColor.GOLD)
                }

                add {
                    append("Mining Speed ", TextColor.GOLD)
                    append("per ", TextColor.GRAY)
                    append("1♨ Heat ", TextColor.RED)
                    append("you have.", TextColor.GRAY)
                }

                space()

                add {
                    append("* ") {
                        color = TextColor.DARK_GRAY
                        bold = true
                    }
                    append("Co-op Soulbound ", TextColor.DARK_GRAY)
                    append("*") {
                        color = TextColor.DARK_GRAY
                        bold = true
                    }
                }

                add {
                    append("a") {
                        color = TextColor.AQUA
                        bold = true
                        obfuscated = true
                    }
                    append(" ")
                    append("DIVINE DRILL ") {
                        color = TextColor.AQUA
                        bold = true
                    }
                    append("a") {
                        color = TextColor.AQUA
                        bold = true
                        obfuscated = true
                    }
                }


            }
        }
    }
}
