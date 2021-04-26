package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_CLT_PLUGINS
import java.io.ByteArrayOutputStream

class InspectionEnvironmentProvider(
        private val _parametersService: ParametersService,
        private val _pluginsListProvider: PackagesProvider,
        private val _xmlWriter: XmlWriter)
    : EnvironmentProvider {
    override fun getEnvironmentVariables() = sequence {
        _parametersService.tryGetParameter(ParameterType.Runner, RUNNER_SETTING_CLT_PLUGINS)?.let {
            var plugins = _pluginsListProvider.getPackages(it)
            if (!plugins.isEmpty) {
                ByteArrayOutputStream().use {
                    _xmlWriter.write(plugins, it)
                    yield(CommandLineEnvironmentVariable(PluginListEnvVar, it.toString(Charsets.UTF_8.name())))
                }
            }
        }
    }

    companion object {
        internal const val PluginListEnvVar = "JET_ADDITIONAL_DEPLOYED_PACKAGES"
    }
}