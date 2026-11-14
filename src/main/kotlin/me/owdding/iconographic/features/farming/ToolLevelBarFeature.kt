package me.owdding.iconographic.features.farming

import me.owdding.iconographic.system.TooltipFeature
import me.owdding.iconographic.system.TooltipFeatureWithContext

data object ToolLevelBarFeature : TooltipFeature() {
    override val enabled: Boolean = true
    override val priority: Int = 0

}
