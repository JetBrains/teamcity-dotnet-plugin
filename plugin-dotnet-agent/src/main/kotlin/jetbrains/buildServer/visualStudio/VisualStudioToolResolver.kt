package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_VISUAL_STUDIO_VERSION
import jetbrains.buildServer.dotnet.Tool
import java.io.File

class VisualStudioToolResolver(private val _parametersService: ParametersService)
    : ToolResolver {
   override val executableFile: File get() {
           val paramName = "VS${version}_Path"
           val path = _parametersService.tryGetParameter(ParameterType.Configuration, paramName) ?: throw RunBuildException("Can't find configuration parameter \"$paramName\"")
           return File(path, VSToolName)
        }

    private val version: Int get() =
        selectedVersion ?: availableVersions.sortedByDescending { it }.firstOrNull() ?: throw RunBuildException("Can't find any version of visual studio")

    private val selectedVersion: Int? get() =
        _parametersService.tryGetParameter(ParameterType.Runner, PARAM_VISUAL_STUDIO_VERSION)?.let {
            Tool.tryParse(it)?.let {
                it.version
            } ?: throw RunBuildException("Can't parse visual studio version from \"$PARAM_VISUAL_STUDIO_VERSION\" value \"$it\"")
        }

    private val availableVersions: Sequence<Int> get() =
        _parametersService.getParameterNames(ParameterType.Configuration)
                .map { _vsConfigNamePattern.find(it) }
                .filter { it != null }
                .map { it!!.groupValues[1].toIntOrNull() }
                .filter { it != null }
                .map { it as Int }

    companion object {
        private val _vsConfigNamePattern:Regex = Regex("VS(\\d+)_Path", option = RegexOption.IGNORE_CASE)
        const val VSToolName = "devenv.com"
    }
}