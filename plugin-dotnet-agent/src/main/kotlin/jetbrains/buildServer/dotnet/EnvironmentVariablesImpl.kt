package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.util.OSType
import java.io.File

class EnvironmentVariablesImpl(
        private val _environment: Environment,
        private val _sharedCompilation: SharedCompilation,
        private val _pathsService: PathsService)
    : EnvironmentVariables {
    override fun getVariables(toolVersion: Version): Sequence<CommandLineEnvironmentVariable> = sequence {
        yieldAll(defaultVariables)
        yield(CommandLineEnvironmentVariable("NUGET_PACKAGES", File(File(_pathsService.getPath(PathType.System), "dotnet"), ".nuget").absolutePath))

        if (_sharedCompilation.requireSuppressing(toolVersion)) {
            yield(useSharedCompilationEnvironmentVariable)
        }

        val home = if (_environment.os == OSType.WINDOWS) "USERPROFILE" else "HOME"
        if (_environment.tryGetVariable(home).isNullOrEmpty()) {
            yield(CommandLineEnvironmentVariable(home, System.getProperty("user.home")))
        }
    }

    companion object {
        internal val defaultVariables = sequenceOf(
                CommandLineEnvironmentVariable("COMPlus_EnableDiagnostics", "0"),
                CommandLineEnvironmentVariable("DOTNET_CLI_TELEMETRY_OPTOUT", "true"),
                CommandLineEnvironmentVariable("DOTNET_SKIP_FIRST_TIME_EXPERIENCE", "true"),
                CommandLineEnvironmentVariable("NUGET_XMLDOC_MODE", "skip"))

        internal val useSharedCompilationEnvironmentVariable = CommandLineEnvironmentVariable("UseSharedCompilation", "false")
    }
}