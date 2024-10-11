package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.impl.operationModes.AgentOperationModeHolder
import jetbrains.buildServer.agent.impl.operationModes.BaseExecutorMode
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.common.MSBuildEnvironmentVariables.USE_SHARED_COMPILATION_ENV_VAR
import jetbrains.buildServer.dotnet.logging.LoggerResolver
import jetbrains.buildServer.util.OSType

class DotnetEnvironmentVariables(
    private val _environment: Environment,
    private val _parametersService: ParametersService,
    private val _pathsService: PathsService,
    private val _additionalEnvironmentVariables: List<EnvironmentVariables>,
    private val _loggerResolver: LoggerResolver,
    private val agentOperationModeHolder: AgentOperationModeHolder
) : EnvironmentVariables {
    override fun getVariables(sdkVersion: Version): Sequence<CommandLineEnvironmentVariable> = sequence {
        yieldAll(sequenceOf(
            // we don't want to send extra telemetry from an agent to 3rd-party organizations
            "DOTNET_CLI_TELEMETRY_OPTOUT" to "true",

            // disable redundant welcome message of the .NET CLI on an agent
            "DOTNET_SKIP_FIRST_TIME_EXPERIENCE" to "true",

            // ignore redundant XML files extraction during .nupkg unpacking
            "NUGET_XMLDOC_MODE" to "skip",

            // pass paths to the TeamCity's MSBuild and VSTest loggers into the build process
            "TEAMCITY_MSBUILD_LOGGER" to _loggerResolver.resolve(ToolType.MSBuild).canonicalPath,
            "TEAMCITY_VSTEST_LOGGER" to _loggerResolver.resolve(ToolType.VSTest).canonicalPath,

            // disable shared compilation by default; enable if the user-defined variable is present
            USE_SHARED_COMPILATION_ENV_VAR to useSharedCompilation.toString(),
        ))

        // set user home directory from Java system properties if it's not defined by user
        if (_environment.tryGetVariable(userHomeEnvVar).isNullOrBlank()) {
            yield(userHomeEnvVar to System.getProperty("user.home"))
        }

        // disable debugger pipes creation in temp directory: https://youtrack.jetbrains.com/issue/TW-60571
        // applies till .NET 8 version only since it raises an issue with dotCover: https://youtrack.jetbrains.com/issue/TW-85181
        if (sdkVersion < Dotnet8Version) {
            yield("COMPlus_EnableDiagnostics" to "0")
        }

        // set service messages backup file path if allowed
        if (allowMessageGuard) {
            yield("TEAMCITY_SERVICE_MESSAGES_PATH" to _pathsService.getPath(PathType.AgentTemp).canonicalPath)
        }

        // if the build is being executed by an executor, it is not expected that the user will have the same permissions to access directories
        // https://youtrack.jetbrains.com/issue/TW-90039
        if (_environment.tryGetVariable(dotNetCliHome).isNullOrBlank() &&
            agentOperationModeHolder.operationMode is BaseExecutorMode){
            yield(dotNetCliHome to "/tmp/DOTNET_CLI_HOME")
        }
    }
        .map { CommandLineEnvironmentVariable(it.first, it.second) }
        .let { it + _additionalEnvironmentVariables.flatMap { it.getVariables(sdkVersion) }}

    private val useSharedCompilation get() =
        _parametersService.tryGetParameter(ParameterType.Environment, USE_SHARED_COMPILATION_ENV_VAR)
            ?.trim().toBoolean()

    private val allowMessageGuard get() =
        _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_MESSAGES_GUARD)
            ?.trim().toBoolean()

    private val userHomeEnvVar get() = when {
        _environment.os == OSType.WINDOWS -> "USERPROFILE"
        else -> "HOME"
    }

    companion object {
        private val Dotnet8Version = Version(8,0,0)
        const val dotNetCliHome = "DOTNET_CLI_HOME"
    }
}
