package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.Environment
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
    override val executableFile: File get() =
        _currentTool?.let {
            when (it.platform) {
                ToolPlatform.Windows -> {
                    val x86Tool = "MSBuildTools${it.version}.0_x86_Path"
                    val x64Tool = "MSBuildTools${it.version}.0_x64_Path"
                    when (it.bitness) {
                        ToolBitness.x64 -> {
                            return tryGetWindowsTool(x64Tool) ?: throw RunBuildException(ToolCannotBeFoundException(x64Tool))
                        }
                        ToolBitness.x86 -> {
                            return tryGetWindowsTool(x86Tool) ?: throw RunBuildException(ToolCannotBeFoundException(x86Tool))
                        }
                        else -> {
                            tryGetWindowsTool(x64Tool)?.let {
                                return it
                            }

                            return tryGetWindowsTool(x86Tool) ?: throw RunBuildException(ToolCannotBeFoundException(x64Tool))
                        }
                    }
                }
                ToolPlatform.Mono -> {
                    val monoTool = MonoConstants.CONFIG_PATH
                    return tryGetMonoTool(monoTool) ?: throw RunBuildException(ToolCannotBeFoundException(monoTool))
                }
                else -> {
                    return _dotnetToolResolver.executableFile
                }
            }
        } ?: _dotnetToolResolver.executableFile

    override val isCommandRequired: Boolean get() =
        _currentTool?.let {
            return it.platform == ToolPlatform.DotnetCore
        } ?: true

    private val _currentTool: Tool? get() =
        _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_MSBUILD_VERSION)?.let {
            return Tool.tryParse(it)
        }

    private fun tryGetWindowsTool(parameterName: String): File? =
        _parametersService.tryGetParameter(ParameterType.Configuration, parameterName)?.let {
            return File(it, MSBuildWindowsTooName).absoluteFile
        }


    private fun tryGetMonoTool(parameterName: String): File? =
        _parametersService.tryGetParameter(ParameterType.Configuration, parameterName)?.let {
            val baseDirectory = File(it).absoluteFile.parent
            return when(_environment.OS) {
                OSType.WINDOWS -> File(baseDirectory, MSBuildMonoWindowsToolName).absoluteFile
                else -> File(baseDirectory, MSBuildMonoToolName).absoluteFile
            }
        }

    companion object {
        const val MSBuildWindowsTooName = "MSBuild.exe"
        const val MSBuildMonoWindowsToolName = "msbuild.bat"
        const val MSBuildMonoToolName = "msbuild"
    }
}