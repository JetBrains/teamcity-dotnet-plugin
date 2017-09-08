package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import java.io.File

class MSBuildToolResolver(
        private val _parametersService: ParametersService,
        private val _dotnetToolResolver: ToolResolver)
    : ToolResolver {
    override val executableFile: File
        get() {
            CurrentTool?.let {
                when (it.platform) {
                    ToolPlatform.Windows -> {
                        val x86Tool = "MSBuildTools${it.version}.0_x86_Path"
                        val x64Tool = "MSBuildTools${it.version}.0_x64_Path"
                        when (it.bitness) {
                            ToolBitness.x64 -> {
                                return tryGetTool(x64Tool) ?: throw RunBuildException(ToolCannotBeFoundException(x64Tool))
                            }
                            ToolBitness.x86 -> {
                                return tryGetTool(x86Tool) ?: throw RunBuildException(ToolCannotBeFoundException(x86Tool))
                            }
                            else -> {
                                tryGetTool(x64Tool)?.let {
                                    return it
                                }

                                return tryGetTool(x86Tool) ?: throw RunBuildException(ToolCannotBeFoundException(x64Tool))
                            }
                        }
                    }
                    else -> {
                    }
                }
            }

            return _dotnetToolResolver.executableFile
        }

    override val isCommandRequired: Boolean
        get() {
            CurrentTool?.let {
                return it.platform == ToolPlatform.Any
            }

            return true
        }

    private val CurrentTool: Tool? get() {
        _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_MSBUILD_VERSION)?.let {
            return Tool.tryParse(it)
        }

        return null
    }

    private fun tryGetTool(parameterName: String): File? {
        _parametersService.tryGetParameter(ParameterType.Configuration, parameterName)?.let {
            return File(it, MSBuildTooName).absoluteFile
        }

        return null
    }

    companion object {
        const val MSBuildTooName:String = "MSBuild.exe"
    }
}