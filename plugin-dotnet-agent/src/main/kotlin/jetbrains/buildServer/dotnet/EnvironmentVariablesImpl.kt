package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.util.OSType
import java.io.File

class EnvironmentVariablesImpl(
        private val _environment: Environment,
        private val _pathsService: PathsService,
        private val _virtualContext: VirtualContext)
    : EnvironmentVariables {
    override fun getVariables(sdkVersion: Version): Sequence<CommandLineEnvironmentVariable> = sequence {
        yieldAll(defaultVariables)
        yield(CommandLineEnvironmentVariable("NUGET_PACKAGES", _virtualContext.resolvePath(File(File(_pathsService.getPath(PathType.System), "dotnet"), ".nuget").canonicalPath)))

        val home = if (_environment.os == OSType.WINDOWS) "USERPROFILE" else "HOME"
        if (_environment.tryGetVariable(home).isNullOrEmpty()) {
            yield(CommandLineEnvironmentVariable(home, System.getProperty("user.home")))
        }

        if (_virtualContext.isVirtual && _virtualContext.targetOSType != OSType.WINDOWS) {
            yieldAll(tempDirVariables)
        }
    }

    companion object {
        internal val defaultVariables = sequenceOf(
                CommandLineEnvironmentVariable("UseSharedCompilation", "false"),
                CommandLineEnvironmentVariable("COMPlus_EnableDiagnostics", "0"),
                CommandLineEnvironmentVariable("DOTNET_CLI_TELEMETRY_OPTOUT", "true"),
                CommandLineEnvironmentVariable("DOTNET_SKIP_FIRST_TIME_EXPERIENCE", "true"),
                CommandLineEnvironmentVariable("NUGET_XMLDOC_MODE", "skip"))

        internal val tempDirVariables = sequenceOf(
                CommandLineEnvironmentVariable("TEMP", ""),
                CommandLineEnvironmentVariable("TMP", ""),
                CommandLineEnvironmentVariable("TMPDIR", ""))
    }
}