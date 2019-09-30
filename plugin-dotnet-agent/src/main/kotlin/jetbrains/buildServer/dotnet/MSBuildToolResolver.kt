package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.util.OSType
import java.io.File

class MSBuildToolResolver(
        private val _environment: Environment,
        private val _parametersService: ParametersService,
        private val _dotnetToolResolver: ToolResolver)
    : ToolResolver {
    override val paltform: ToolPlatform
        get() = _currentTool?.platform ?: ToolPlatform.CrossPlatform

    override val executable: ToolPath
        get() =
            _currentTool?.let {
                when (it.platform) {
                    ToolPlatform.Windows -> {
                        val x86Tool = "MSBuildTools${it.version}.0_x86_Path"
                        val x64Tool = "MSBuildTools${it.version}.0_x64_Path"
                        when (it.bitness) {
                            ToolBitness.X64 -> {
                                return ToolPath(tryGetWindowsTool(x64Tool) ?: throw RunBuildException(ToolCannotBeFoundException(x64Tool)))
                            }
                            ToolBitness.X86 -> {
                                return ToolPath(tryGetWindowsTool(x86Tool) ?: throw RunBuildException(ToolCannotBeFoundException(x86Tool)))
                            }
                            else -> {
                                tryGetWindowsTool(x64Tool)?.let {
                                    return ToolPath(it)
                                }

                                return ToolPath(tryGetWindowsTool(x86Tool) ?: throw RunBuildException(ToolCannotBeFoundException(x64Tool)))
                            }
                        }
                    }
                    ToolPlatform.Mono -> {
                        val monoTool = MonoConstants.CONFIG_PATH
                        return ToolPath(tryGetMonoTool(monoTool) ?: throw RunBuildException(ToolCannotBeFoundException(monoTool)))
                    }
                    else -> {
                        return _dotnetToolResolver.executable
                    }
                }
            } ?: _dotnetToolResolver.executable


    override val isCommandRequired: Boolean
        get() =
            _currentTool?.let {
                return it.platform == ToolPlatform.CrossPlatform
            } ?: true

    private val _currentTool: Tool?
        get() =
            _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_MSBUILD_VERSION)?.let {
                return Tool.tryParse(it)
            }

    private fun tryGetWindowsTool(parameterName: String): Path? =
            _parametersService.tryGetParameter(ParameterType.Configuration, parameterName)?.let {
                return Path(File(it, MSBuildWindowsTooName).canonicalPath)
            }


    private fun tryGetMonoTool(parameterName: String): Path? =
            _parametersService.tryGetParameter(ParameterType.Configuration, parameterName)?.let {
                val baseDirectory = File(it).canonicalFile.parent
                return when (_environment.os) {
                    OSType.WINDOWS -> Path(File(baseDirectory, MSBuildMonoWindowsToolName).canonicalPath)
                    else -> Path(File(baseDirectory, MSBuildMonoToolName).canonicalPath)
                }
            }

    companion object {
        const val MSBuildWindowsTooName = "MSBuild.exe"
        const val MSBuildMonoWindowsToolName = "msbuild.bat"
        const val MSBuildMonoToolName = "msbuild"
    }
}