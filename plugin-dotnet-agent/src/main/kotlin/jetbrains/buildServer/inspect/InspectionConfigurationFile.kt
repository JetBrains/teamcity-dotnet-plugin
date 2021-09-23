package jetbrains.buildServer.inspect

import jetbrains.buildServer.E
import jetbrains.buildServer.XmlDocumentService
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.build
import jetbrains.buildServer.inspect.InspectCodeConstants.CONFIG_PARAMETER_DISABLE_SOLUTION_WIDE_ANALYSIS
import jetbrains.buildServer.inspect.InspectCodeConstants.CONFIG_PARAMETER_SUPRESS_BUILD_IN_SETTINGS
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_CUSTOM_SETTINGS_PROFILE_PATH
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_PROJECT_FILTER
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_SOLUTION_PATH
import org.w3c.dom.Document
import java.io.OutputStream

class InspectionConfigurationFile(
        private val _parametersService: ParametersService,
        private val _xmlWriter: XmlWriter)
    : ConfigurationFile {

    override fun create(destinationStream: OutputStream, outputFile: Path, cachesHomeDirectory: Path?, debug: Boolean) {
        val includedProjects = _parametersService
                .tryGetParameter(ParameterType.Runner, RUNNER_SETTING_PROJECT_FILTER)
                ?.lines()
                ?.asSequence() ?: emptySequence<String>()
                .filter { !it.isNullOrBlank() }

        _xmlWriter.write(
                E("InspectCodeOptions",
                        E("Debug", if(debug) debug.toString() else null),
                        E("IncludedProjects", includedProjects.map { E("IncludedProjects", it) } ),
                        E("OutputFile", if(!outputFile.path.isNullOrEmpty()) outputFile.path else null),
                        E("SolutionFile", _parametersService.tryGetParameter(ParameterType.Runner, RUNNER_SETTING_SOLUTION_PATH)),
                        E("CachesHomeDirectory", if(!cachesHomeDirectory?.path.isNullOrEmpty()) cachesHomeDirectory?.path else null),
                        E("CustomSettingsProfile", _parametersService.tryGetParameter(ParameterType.Runner, RUNNER_SETTING_CUSTOM_SETTINGS_PROFILE_PATH)),
                        E("SupressBuildInSettings", _parametersService.tryGetParameter(ParameterType.Runner, CONFIG_PARAMETER_SUPRESS_BUILD_IN_SETTINGS)?.toBoolean()?.toString()),
                        E("NoSolutionWideAnalysis", _parametersService.tryGetParameter(ParameterType.Runner, CONFIG_PARAMETER_DISABLE_SOLUTION_WIDE_ANALYSIS)?.toBoolean()?.toString())
                ),
                destinationStream
        )
    }
}