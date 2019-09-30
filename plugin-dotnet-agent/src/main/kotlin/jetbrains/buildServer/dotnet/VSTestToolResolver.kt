package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.io.File

class VSTestToolResolver(
        private val _parametersService: ParametersService,
        private val _dotnetToolResolver: ToolResolver)
    : ToolResolver {
    override val paltform: ToolPlatform
        get() = _currentTool?.platform ?: ToolPlatform.CrossPlatform

    override val executable: ToolPath
        get() {
            _currentTool?.let {
                when (it.platform) {
                    ToolPlatform.Windows -> {
                        val vstestTool = "teamcity.dotnet.vstest.${it.version}.0"
                        return ToolPath(tryGetTool(vstestTool) ?: throw RunBuildException(ToolCannotBeFoundException(vstestTool)))
                    }
                    else -> {
                    }
                }
            }

            return _dotnetToolResolver.executable
        }

    override val isCommandRequired: Boolean
        get() {
            _currentTool?.let {
                return it.platform == ToolPlatform.CrossPlatform
            }

            return true
        }

    private val _currentTool: Tool?
        get() {
            _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VSTEST_VERSION)?.let {
                return Tool.tryParse(it)
            }

            return null
        }

    private fun tryGetTool(parameterName: String): File? {
        _parametersService.tryGetParameter(ParameterType.Configuration, parameterName)?.let {
            return File(it).absoluteFile
        }

        return null
    }
}