package jetbrains.buildServer.inspect

import jetbrains.buildServer.parameters.ReferencesResolverUtil
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.util.PropertiesUtil
import java.util.*

class DupFinderRunTypePropertiesProcessor : PropertiesProcessor {
    override fun process(properties: Map<String, String>): Collection<InvalidProperty> {
        val result: MutableList<InvalidProperty> = Vector()
        val files = properties[DupFinderConstants.SETTINGS_INCLUDE_FILES]
        if (PropertiesUtil.isEmptyOrNull(files)) {
            result.add(InvalidProperty(DupFinderConstants.SETTINGS_INCLUDE_FILES, "Input files must be specified"))
        }
        val discardCostValue = properties[DupFinderConstants.SETTINGS_DISCARD_COST]
        if (!PropertiesUtil.isEmptyOrNull(discardCostValue)) {
            if (!ReferencesResolverUtil.isReference(discardCostValue!!)) {
                val value = PropertiesUtil.parseInt(discardCostValue)
                if (value == null || value <= 0) {
                    result.add(InvalidProperty(DupFinderConstants.SETTINGS_DISCARD_COST, "Duplicate complexity must be a positive number or parameter reference. "))
                }
            }
        }
        return result
    }
}