package me.owdding.iconographic.config.previews

import me.owdding.iconographic.config.categories.mining.MiningConfig
import me.owdding.iconographic.config.categories.visuals.VisualsConfig
import me.owdding.ktmodules.Module
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.builders.ItemBuilder
import tech.thatgravyboat.skyblockapi.utils.lazy.registryBoundLazy
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object Silly1 : ConfigPreviewItem() {
    override val item: ItemStack? by registryBoundLazy {
        ItemBuilder(Items.PRISMARINE_SHARD) {
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
        toggle(MiningConfig::drillComponents)
    }

    override fun PreviewBuilder.buildPreview() {
        section(Text.of("Header")) {
            options {
                toggle(VisualsConfig::spinny)
            }

            lore {
                add("Heated Divan's Drill", TextColor.AQUA)
                add("Breaking Power 10", TextColor.DARK_GRAY)
            }
        }

        section("Stats") {
            options {
                toggle(VisualsConfig::alignedStats)
                toggle(VisualsConfig::colorStatName)
            }

            lore {
                add("Damage: ") {
                    color = TextColor.GRAY
                    append("+75", TextColor.RED)
                }
                add("Mining Speed: ") {
                    color = TextColor.GRAY
                    append("+3,260", TextColor.GOLD)
                    append("[+500]", TextColor.GOLD)
                    append("[+50]", TextColor.BLUE)
                    append("(+70)", TextColor.BLUE)
                    append("(+240)", TextColor.LIGHT_PURPLE)
                }
                add("Pristine: ") {
                    color = TextColor.GRAY
                    append("+4.7", TextColor.DARK_PURPLE)
                    append("(+2.2)", TextColor.LIGHT_PURPLE)
                }
                add("Mining Fortune: ") {
                    color = TextColor.GRAY
                    append("+445", TextColor.GOLD)
                    append("[+5]", TextColor.BLUE)
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
    }
}
