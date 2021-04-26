package jetbrains.buildServer.inspect

import jetbrains.buildServer.inspect.CltConstants.RUNNER_SETTING_CLT_PLATFORM
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_SOLUTION_PATH
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.util.PropertiesUtil
import java.util.*

class InspectCodeRunTypePropertiesProcessor(
        private val _toolVersionProvider: ToolVersionProvider)
    : PropertiesProcessor {
    override fun process(properties: Map<String, String>): Collection<InvalidProperty> {
        val result: MutableList<InvalidProperty> = Vector()
        val solutionPath = properties[RUNNER_SETTING_SOLUTION_PATH]
        if (PropertiesUtil.isEmptyOrNull(solutionPath)) {
            result.add(InvalidProperty(RUNNER_SETTING_SOLUTION_PATH,"Solution path must be specified"))
        }

        val platform = properties[RUNNER_SETTING_CLT_PLATFORM]?.let {
            IspectionToolPlatform.tryParse(it)
        }

        if(platform == IspectionToolPlatform.CrossPlatform && _toolVersionProvider.getVersion(properties) < RequirementsResolverImpl.CrossPlatformVersion) {
            result.add(InvalidProperty(RUNNER_SETTING_CLT_PLATFORM,"To support cross-platform inspections, use ReSharper version ${RequirementsResolverImpl.CrossPlatformVersion} or later."))
        }

        return result
    }
}