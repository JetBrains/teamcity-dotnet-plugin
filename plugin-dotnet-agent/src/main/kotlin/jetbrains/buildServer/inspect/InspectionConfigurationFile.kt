

package jetbrains.buildServer.inspect

import jetbrains.buildServer.XmlElement
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.inspect.InspectCodeConstants.CONFIG_PARAMETER_DISABLE_SOLUTION_WIDE_ANALYSIS
import jetbrains.buildServer.inspect.InspectCodeConstants.CONFIG_PARAMETER_SUPRESS_BUILD_IN_SETTINGS
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_CUSTOM_SETTINGS_PROFILE_PATH
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_PROJECT_FILTER
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_SOLUTION_PATH
import java.io.OutputStream

class InspectionConfigurationFile(
    private val _parametersService: ParametersService,
    private val _xmlWriter: XmlWriter
) : ConfigurationFile {

    override fun create(destinationStream: OutputStream, outputFile: Path, cachesHomeDirectory: Path?, debug: Boolean) {
        val includedProjects = _parametersService
            .tryGetParameter(ParameterType.Runner, RUNNER_SETTING_PROJECT_FILTER)
            ?.lines()
            ?.asSequence() ?: emptySequence<String>()
            .filter { !it.isNullOrBlank() }

        _xmlWriter.write(
            XmlElement(
                "InspectCodeOptions",
                XmlElement("Debug", if (debug) debug.toString() else null),
                XmlElement("IncludedProjects", includedProjects.map { XmlElement("IncludedProjects", it) }),
                XmlElement("OutputFile", if (!outputFile.path.isNullOrEmpty()) outputFile.path else null),
                XmlElement("SolutionFile", _parametersService.tryGetParameter(ParameterType.Runner, RUNNER_SETTING_SOLUTION_PATH)?.trim()),
                XmlElement("CachesHomeDirectory", if (!cachesHomeDirectory?.path.isNullOrEmpty()) cachesHomeDirectory?.path else null),
                XmlElement("CustomSettingsProfile", _parametersService.tryGetParameter(ParameterType.Runner, RUNNER_SETTING_CUSTOM_SETTINGS_PROFILE_PATH)),
                XmlElement("SupressBuildInSettings", _parametersService.tryGetParameter(ParameterType.Runner, CONFIG_PARAMETER_SUPRESS_BUILD_IN_SETTINGS)?.toBoolean()?.toString()),
                XmlElement(
                    "NoSolutionWideAnalysis",
                    _parametersService.tryGetParameter(ParameterType.Runner, CONFIG_PARAMETER_DISABLE_SOLUTION_WIDE_ANALYSIS)?.toBoolean()?.toString()
                )
            ),
            destinationStream
        )
    }
}