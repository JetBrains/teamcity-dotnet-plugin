package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.util.OSType
import kotlin.coroutines.experimental.buildSequence

class EnvironmentVariablesImpl(
        private val _environment: Environment,
        private val _dotnetCliToolInfo: DotnetCliToolInfo)
    : EnvironmentVariables {
    override val variables: Sequence<CommandLineEnvironmentVariable>
        get() = buildSequence {
           yieldAll(DefaultVariables)

            // Prevents the case when VBCSCompiler service remains in memory after `dotnet build` for Linux and consumes 100% of 1 CPU core and a lot of memory
            // https://youtrack.jetbrains.com/issue/TW-55268
            // https://github.com/dotnet/roslyn/issues/27566
            if (_dotnetCliToolInfo.Version > LastNotHangingVBCSCompilerVersion) {
                when(_environment.OS) {
                    OSType.UNIX, OSType.MAC -> yield(CommandLineEnvironmentVariable("UseSharedCompilation", "false"))
                    else -> { }
                }
            }
        }

    companion object {
        private val LastNotHangingVBCSCompilerVersion: Version = Version(2, 1, 105)
        val DefaultVariables = sequenceOf(
                CommandLineEnvironmentVariable("DOTNET_CLI_TELEMETRY_OPTOUT", "true"),
                CommandLineEnvironmentVariable("DOTNET_SKIP_FIRST_TIME_EXPERIENCE", "true"),
                CommandLineEnvironmentVariable("NUGET_XMLDOC_MODE", "skip"))
    }
}