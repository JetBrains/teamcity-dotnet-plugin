package jetbrains.buildServer.dotcover

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotcover.tool.DotCoverAgentTool
import jetbrains.buildServer.dotcover.tool.DotCoverToolType
import jetbrains.buildServer.util.OSType
import java.io.File

class DotCoverEntryPointSelectorImpl(
    private val _tool: DotCoverAgentTool,
    private val _virtualContext: VirtualContext,
    private val _loggerService: LoggerService,
) : DotCoverEntryPointSelector {
    override fun select(): Result<File> =
        selectEntryPoint()
            .fold(
                onSuccess = { Result.success(it) },
                onFailure = { when (it) {
                    is UnsatisfiedRequirementError -> {
                        val errorMessage = "Unable to collect code coverage: ${it.message}"
                        _loggerService.writeBuildProblem(
                            DOTCOVER_REQUIREMENTS_BUILD_PROBLEM,
                            BuildProblemData.TC_ERROR_MESSAGE_TYPE,
                            errorMessage
                        )
                        Result.failure(Error(errorMessage))
                    }

                    else -> Result.failure(it)
                }}
            )

    private fun selectEntryPoint(): Result<File> {
        val homePath = _tool.dotCoverHomePath
        if (homePath.isBlank()) {
            return Result.failure(ToolCannotBeFoundException("dotCover tool installation path is empty"))
        }

        return when {
            // on Windows
            _virtualContext.targetOSType == OSType.WINDOWS -> when (_tool.type) {
                // for the deprecated version on Windows we use .exe since the tool version can't manage run with bundled runtime .NET runtime properly
                DotCoverToolType.DeprecatedCrossPlatform -> Result.success(_tool.dotCoverExeFile)
                DotCoverToolType.WindowsOnly -> Result.success(_tool.dotCoverExeFile)
                DotCoverToolType.CrossPlatform -> when {
                    // we don't need to check requirements when the build run inside a container
                    _virtualContext.isVirtual -> Result.success(_tool.dotCoverExeFile)
                    _tool.canUseDotNetRuntime -> Result.success(_tool.dotCoverDllFile)
                    _tool.canUseDotNetFrameworkRuntime -> Result.success(_tool.dotCoverExeFile)
                    else -> Result.failure(UnsatisfiedRequirementError(
                        "Windows agents must have $crossPlatformToolRequirementsText " +
                        "installed to run the \"dotCover Cross-Platform\" tool"
                    ))
                }
                DotCoverToolType.Unknown -> Result.failure(ToolCannotBeFoundException(
                    "dotCover has been run on Windows, however " +
                    "${_tool.dotCoverDllFile.name} or ${_tool.dotCoverExeFile.name} " +
                    "wasn't found in the tool installation path: $homePath"
                ))
            }
            // on *nix-like OS
            else -> when (_tool.type) {
                DotCoverToolType.DeprecatedCrossPlatform -> Result.success(_tool.dotCoverShFile)
                DotCoverToolType.CrossPlatform -> when {
                    // we don't need to check requirements when the build running inside a container
                    _virtualContext.isVirtual -> Result.success(_tool.dotCoverDllFile)
                    _tool.canUseDotNetRuntime -> Result.success(_tool.dotCoverDllFile)
                    else -> Result.failure(UnsatisfiedRequirementError(
                        "Linux or macOS agents must have $crossPlatformToolRequirementsText " +
                        "installed to run the \"dotCover Cross-Platform\" tool"
                    ))
                }
                DotCoverToolType.WindowsOnly -> Result.failure(UnsatisfiedRequirementError(
                    "dotCover has been run on Linux or macOS agent, however it's using a tool designed for Windows"
                ))
                DotCoverToolType.Unknown -> Result.failure(ToolCannotBeFoundException(
                    "dotCover has been run on Linux or macOS agent, however " +
                    "${_tool.dotCoverDllFile.name} or ${_tool.dotCoverShFile.name} " +
                    "weren't found in the tool installation path: $homePath"
                ))
            }
        }
    }

    private class UnsatisfiedRequirementError(message: String) : Error(message)

    private val crossPlatformToolRequirementsText get() =
        _tool.getCrossPlatformVersionMinRequirement(_virtualContext.targetOSType).joinToString(" or ")

    companion object {
        internal const val DOTCOVER_REQUIREMENTS_BUILD_PROBLEM = "dotCover_requirements_have_not_been_met"
    }
}