@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.runner.TargetRegistry
import jetbrains.buildServer.util.OSType
import kotlin.coroutines.experimental.buildSequence

class EnvironmentVariablesImpl(
        private val _environment: Environment,
        private val _dotnetCliToolInfo: DotnetCliToolInfo,
        private val _targetRegistry: TargetRegistry)
    : EnvironmentVariables {
    override val variables: Sequence<CommandLineEnvironmentVariable>
        get() = buildSequence {
            yieldAll(defaultVariables)

            // Prevents the case when VBCSCompiler service remains in memory after `dotnet build` for Linux and consumes 100% of 1 CPU core and a lot of memory
            // https://youtrack.jetbrains.com/issue/TW-55268
            // https://github.com/dotnet/roslyn/issues/27566
            if (_dotnetCliToolInfo.version > LastNotHangingVBCSCompilerVersion) {
                when (_environment.os) {
                    OSType.UNIX, OSType.MAC -> yield(useSharedCompilationEnvironmentVariable)
                    else -> {
                        // dotCover is waiting for finishing of all spawned processes including a build's infrastructure processes
                        // https://github.com/JetBrains/teamcity-dotnet-plugin/issues/132
                        if (_targetRegistry.activeTargets.contains(TargetType.CodeCoverageProfiler)) {
                            yield(useSharedCompilationEnvironmentVariable)
                        }
                    }
                }
            }
        }

    companion object {
        private val LastNotHangingVBCSCompilerVersion: Version = Version(2, 1, 105)
        internal val defaultVariables = sequenceOf(
                CommandLineEnvironmentVariable("DOTNET_CLI_TELEMETRY_OPTOUT", "true"),
                CommandLineEnvironmentVariable("DOTNET_SKIP_FIRST_TIME_EXPERIENCE", "true"),
                CommandLineEnvironmentVariable("NUGET_XMLDOC_MODE", "skip"))

        internal val useSharedCompilationEnvironmentVariable = CommandLineEnvironmentVariable("UseSharedCompilation", "false")
    }
}