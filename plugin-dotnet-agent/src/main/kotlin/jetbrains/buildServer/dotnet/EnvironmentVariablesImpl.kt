package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import kotlin.coroutines.experimental.buildSequence

class EnvironmentVariablesImpl : EnvironmentVariables {
    override val variables: Sequence<CommandLineEnvironmentVariable>
        get() = buildSequence {
            yield(CommandLineEnvironmentVariable("DOTNET_CLI_TELEMETRY_OPTOUT", "true"))
            yield(CommandLineEnvironmentVariable("DOTNET_SKIP_FIRST_TIME_EXPERIENCE", "true"))
            yield(CommandLineEnvironmentVariable("NUGET_XMLDOC_MODE", "skip"))
        }
}