

package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

class PluginDescriptorsProviderImpl(
    private val _parametersService: ParametersService,
    private val _loggerService: LoggerService
) : PluginDescriptorsProvider {

    override fun getPluginDescriptors(): List<PluginDescriptor> {
        return getPluginLines()
            .mapNotNull { line ->
                PluginDescriptorType.values().forEach {
                    val pluginDescriptor = tryCreateDescriptorOfType(line, it)
                    if (pluginDescriptor != null) return@mapNotNull pluginDescriptor
                }

                _loggerService.writeWarning("Invalid R# CLT plugin descriptor: \"$line\", it will be ignored.")
                return@mapNotNull null
            }
    }

    private fun getPluginLines(): List<String> {
        val plugins = _parametersService.tryGetParameter(ParameterType.Runner, InspectCodeConstants.RUNNER_SETTING_CLT_PLUGINS)
        if (!plugins.isNullOrBlank()) {
            return plugins.lines()
                .filter { it.isNotBlank() }
                .map { it.trim() }
                .toList()
        }

        return emptyList()
    }

    private fun tryCreateDescriptorOfType(value: String, type: PluginDescriptorType): PluginDescriptor? {
        val typeMatch = type.regex.matchEntire(value)
        return if (typeMatch != null) PluginDescriptor(type, value) else null
    }
}