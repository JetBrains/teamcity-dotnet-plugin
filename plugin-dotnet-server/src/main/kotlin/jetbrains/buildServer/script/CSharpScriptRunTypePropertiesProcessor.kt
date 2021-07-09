package jetbrains.buildServer.script

import jetbrains.buildServer.inspect.CltConstants.RUNNER_SETTING_CLT_PLATFORM
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_SOLUTION_PATH
import jetbrains.buildServer.inspect.ToolVersionProvider
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.util.PropertiesUtil
import java.util.*

class CSharpScriptRunTypePropertiesProcessor(
        private val _toolVersionProvider: ToolVersionProvider)
    : PropertiesProcessor {
    override fun process(properties: Map<String, String>): Collection<InvalidProperty> {
        val result: MutableList<InvalidProperty> = Vector()
        return result
    }
}