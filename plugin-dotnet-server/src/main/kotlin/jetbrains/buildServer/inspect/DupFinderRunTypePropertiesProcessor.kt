package jetbrains.buildServer.inspect

import jetbrains.buildServer.parameters.ReferencesResolverUtil
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.util.PropertiesUtil
import java.util.*

class DupFinderRunTypePropertiesProcessor(
        private val _toolVersionProvider: ToolVersionProvider)
    : PropertiesProcessor {
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

        val platform = properties[CltConstants.RUNNER_SETTING_CLT_PLATFORM]?.let {
            IspectionToolPlatform.tryParse(it)
        }

        if(platform == IspectionToolPlatform.CrossPlatform && _toolVersionProvider.getVersion(properties) < RequirementsResolverImpl.CrossPlatformVersion) {
            result.add(InvalidProperty(CltConstants.RUNNER_SETTING_CLT_PLATFORM,"To support cross-platform duplicates finder, use ReSharper version ${RequirementsResolverImpl.CrossPlatformVersion} or later."))
        }

        return result
    }
}