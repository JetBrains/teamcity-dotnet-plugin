package jetbrains.buildServer.inspect

import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.util.PropertiesUtil
import java.util.*

class InspectCodeRunTypePropertiesProcessor : PropertiesProcessor {
    override fun process(properties: Map<String, String>): Collection<InvalidProperty> {
        val result: MutableList<InvalidProperty> = Vector()
        val solutionPath = properties[InspectCodeConstants.RUNNER_SETTING_SOLUTION_PATH]
        if (PropertiesUtil.isEmptyOrNull(solutionPath)) {
            result.add(
                    InvalidProperty(
                            InspectCodeConstants.RUNNER_SETTING_SOLUTION_PATH,
                            "Solution path must be specified"))
        }
        return result
    }
}