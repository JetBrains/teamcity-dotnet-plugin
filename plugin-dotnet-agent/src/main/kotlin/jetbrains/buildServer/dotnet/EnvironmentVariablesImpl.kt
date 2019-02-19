package jetbrains.buildServer.dotnet

import com.intellij.openapi.util.SystemInfo
import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.runner.TargetRegistry
import jetbrains.buildServer.util.OSType

class EnvironmentVariablesImpl(
        private val _environment: Environment,
        private val _sharedCompilation: SharedCompilation)
    : EnvironmentVariables {
    override fun getVariables(sdkVersion: Version): Sequence<CommandLineEnvironmentVariable> = sequence {
        yieldAll(defaultVariables)

        if (_sharedCompilation.requireSuppressing(sdkVersion)) {
            yield(useSharedCompilationEnvironmentVariable)
        }

        val home = if (_environment.os == OSType.WINDOWS) "USERPROFILE" else "HOME"
        if (_environment.tryGetVariable(home).isNullOrEmpty()) {
            yield(CommandLineEnvironmentVariable(home, System.getProperty("user.home")))
        }
    }

    companion object {
        internal val defaultVariables = sequenceOf(
                CommandLineEnvironmentVariable("DOTNET_CLI_TELEMETRY_OPTOUT", "true"),
                CommandLineEnvironmentVariable("DOTNET_SKIP_FIRST_TIME_EXPERIENCE", "true"),
                CommandLineEnvironmentVariable("NUGET_XMLDOC_MODE", "skip"))

        internal val useSharedCompilationEnvironmentVariable = CommandLineEnvironmentVariable("UseSharedCompilation", "false")
    }
}