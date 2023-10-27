package jetbrains.buildServer.dotcover.tool

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.OSType
import java.io.File

class DotCoverAgentTool(
    private val _parametersService: ParametersService,
    private val _fileSystemService: FileSystemService,
) {
    val dotCoverExeFile get() = EntryPointType.WindowsExecutable.getEntryPointFile(dotCoverHomePath)

    val dotCoverDllFile get() = EntryPointType.UsingAgentDotnetRuntime.getEntryPointFile(dotCoverHomePath)

    val dotCoverShFile get() = EntryPointType.UsingBundledDotnetRuntime.getEntryPointFile(dotCoverHomePath)

    val dotCoverHomePath get() =
        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME)
            .let { when (it.isNullOrBlank()) {
                true -> ""
                false -> it
            }}

    val type get() = when {
        // Windows-only version using agent requirements mechanism – no build-time requirements checking needed
        _fileSystemService.isExists(dotCoverExeFile) && !_fileSystemService.isExists(dotCoverDllFile) ->
            DotCoverToolType.WindowsOnly

        // cross-platform version using agent runtime
        _fileSystemService.isExists(dotCoverDllFile) && !_fileSystemService.isExists(dotCoverShFile) ->
            DotCoverToolType.CrossPlatform

        // cross-platform version using bundled runtime
        _fileSystemService.isExists(dotCoverShFile) ->
            DotCoverToolType.DeprecatedCrossPlatform

        else -> DotCoverToolType.Unknown
    }

    val canUseDotNetRuntime get() =
        satisfiedRequirements.contains(MinRequirement.DotnetCore31) && _fileSystemService.isExists(dotCoverDllFile)

    val canUseDotNetFrameworkRuntime get() =
        satisfiedRequirements.contains(MinRequirement.DotnetFramework472) && _fileSystemService.isExists(dotCoverExeFile)

    fun getCrossPlatformVersionMinRequirement(os: OSType) = when (os) {
        OSType.WINDOWS -> sequenceOf(
            MinRequirement.DotnetCore31.requirementName,
            MinRequirement.DotnetFramework472.requirementName
        )

        else -> sequenceOf(MinRequirement.DotnetCore31.requirementName)
    }

    private val satisfiedRequirements : List<MinRequirement> get() {
        val buildParametersNames = _parametersService.getParameterNames(ParameterType.Configuration)
        return MinRequirement.values().filter { req -> buildParametersNames.any { req.isSatisfiedBy(it) } }
    }

    private enum class EntryPointType(private val entryPointFileName: String) {
        // dotCover.exe ... – simple run of Windows executable file
        WindowsExecutable("dotCover.exe"),

        // dotCover.sh ... – dotCover will select the proper bundled runtime in its own package
        UsingBundledDotnetRuntime("dotCover.sh"),

        // dotnet dotCover.dll ... – will use detected dotnet on agent
        UsingAgentDotnetRuntime("dotCover.dll");

        fun getEntryPointFile(basePath: String): File = File(basePath, this.entryPointFileName)
    }

    private enum class MinRequirement(
        prefix: String,
        private val minVersion: Version,
        suffix: String,
        val requirementName: String
    ) {
        DotnetFramework472(
            DotnetConstants.CONFIG_PREFIX_DOTNET_FAMEWORK,
            Version.MinDotNetFrameworkVersionForDotCover,
            DotnetConstants.CONFIG_SUFFIX_PATH,
            ".NET Framework 4.7.2+"
        ),

        DotnetCore31(
            DotnetConstants.CONFIG_PREFIX_CORE_RUNTIME,
            Version.MinDotNetSdkVersionForDotCover,
            DotnetConstants.CONFIG_SUFFIX_PATH,
            ".NET Core 3.1+"
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
}

