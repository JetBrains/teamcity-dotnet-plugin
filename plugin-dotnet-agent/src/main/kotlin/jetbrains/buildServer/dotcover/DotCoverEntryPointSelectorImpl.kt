package jetbrains.buildServer.dotcover

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.OSType
import java.io.File

class DotCoverEntryPointSelectorImpl(
    private val _parametersService: ParametersService,
    private val _virtualContext: VirtualContext,
    private val _fileSystemService: FileSystemService,
    private val _loggerService: LoggerService,
) : DotCoverEntryPointSelector {
    override fun select(skipRequirementsValidation: Boolean): Result<File> =
        selectEntryPoint()
            .getOrThrow()   // tool is invalid
            .let { entryPoint -> when {
                skipRequirementsValidation || _virtualContext.isVirtual || isEntryPointValid(entryPoint) ->
                    Result.success(entryPoint.file)

                else -> {
                    val errorMessage =
                        "Code coverage cannot be collected: " +
                        "dotCover cannot be run because the required " +
                        "runtime is not detected on the agent: ${entryPoint.requirement!!.errorMessage}"
                    _loggerService.writeBuildProblem(
                        DOTCOVER_REQUIREMENTS_BUILD_PROBLEM,
                        BuildProblemData.TC_ERROR_MESSAGE_TYPE,
                        errorMessage
                    )
                    Result.failure(Error(errorMessage))
                }
            } }

    private fun selectEntryPoint(): Result<EntryPoint> {
        val homePath = dotCoverHomePath;
        if (homePath.isBlank()) {
            return Result.failure(ToolCannotBeFoundException("dotCover tool installation path is empty"))
        }

        val entryPointFileExe = EntryPointType.WindowsExecutable.getEntryPointFile(homePath)
        val entryPointFileDll = EntryPointType.UsingAgentDotnetRuntime.getEntryPointFile(homePath)
        val entryPointFileSh = EntryPointType.UsingBundledDotnetRuntime.getEntryPointFile(homePath)

        return when {
            // on Windows
            _virtualContext.targetOSType == OSType.WINDOWS -> when {
                _fileSystemService.isExists(entryPointFileExe) -> {
                    val requirement = when {
                        // cross-platform version on Windows requires .NET Framework 4.7.2
                        _fileSystemService.isExists(entryPointFileDll) -> MinVersionConfigParameterRequirement.DotnetFramework472

                        // Windows-only version using agent requirements mechanism – no build-time requirements validation needed
                        else -> null
                    }
                    Result.success(EntryPoint(entryPointFileExe, requirement))
                }

                else -> Result.failure(ToolCannotBeFoundException(
                    "dotCover has been run on Windows, however " +
                    "${EntryPointType.WindowsExecutable.entryPointFileName} " +
                    "wasn't found in the tool installation path:" + homePath
                ))
            }

            // on *nix-like OS
            else -> when {
                // deprecated cross-platform version
                _fileSystemService.isExists(entryPointFileSh) ->
                    Result.success(EntryPoint(entryPointFileSh))

                // cross-platform version on Linux/macOs requires .NET Core 3.1+
                _fileSystemService.isExists(entryPointFileDll) ->
                    Result.success(EntryPoint(entryPointFileDll, MinVersionConfigParameterRequirement.DotnetCore31))

                else -> Result.failure(ToolCannotBeFoundException(
                    "dotCover has been run on Linux or MacOS, however " +
                    "${EntryPointType.UsingAgentDotnetRuntime.entryPointFileName} or ${EntryPointType.UsingBundledDotnetRuntime.entryPointFileName} " +
                    "weren't found in the tool installation path:" + homePath
                ))
            }
        }
    }

    private data class EntryPoint(val file: File, val requirement: MinVersionConfigParameterRequirement? = null)

    private enum class EntryPointType(val entryPointFileName: String) {
        // dotCover.exe ... – simple run of Windows executable file
        WindowsExecutable("dotCover.exe"),

        // dotCover.sh ... – dotCover will select the proper bundled runtime in its own package
        UsingBundledDotnetRuntime("dotCover.sh"),

        // dotnet dotCover.dll ... – will use detected dotnet on agent
        UsingAgentDotnetRuntime("dotCover.dll");

        fun getEntryPointFile(basePath: String): File = File(basePath, this.entryPointFileName)
    }

    private enum class MinVersionConfigParameterRequirement(
        prefix: String,
        val minVersion: Version,
        suffix: String,
        val errorMessage: String
    ) {
        DotnetFramework472(
            DotnetConstants.CONFIG_PREFIX_DOTNET_FAMEWORK,
            Version.MinDotNetFrameworkVersionForDotCover,
            DotnetConstants.CONFIG_SUFFIX_PATH,
            "cross-platform dotCover requires a minimum of .NET Framework 4.7.2+ on Windows agent"
        ),

        DotnetCore31(
            DotnetConstants.CONFIG_PREFIX_CORE_RUNTIME,
            Version.MinDotNetSdkVersionForDotCover,
            DotnetConstants.CONFIG_SUFFIX_PATH,
            "cross-platform dotCover requires a minimum of .NET Core 3.1+ on Linux or macOS agent"
        );

        private val regexPattern = "^$prefix(.+)$suffix$".toRegex()

        fun isSatisfiedBy(parameter: String) =
            when (val extractedVersion = regexPattern.find(parameter)?.groupValues?.get(1)) {
                null -> false
                else -> when {
                    Version.isValid(extractedVersion) -> Version.parse(extractedVersion) >= minVersion
                    else -> false
                }
            }
    }

    private fun isEntryPointValid(entryPoint: EntryPoint) =
        entryPoint.requirement == null || buildConfigParamsNames.any { entryPoint.requirement.isSatisfiedBy(it) }

    private val dotCoverHomePath get() =
        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME)
            .let { when (it.isNullOrBlank()) {
                true -> ""
                false -> it
            }}

    private val buildConfigParamsNames get() = _parametersService.getParameterNames(ParameterType.Configuration)

    companion object {
        internal const val DOTCOVER_REQUIREMENTS_BUILD_PROBLEM = "dotCover_requirements_have_not_been_met"
    }
}