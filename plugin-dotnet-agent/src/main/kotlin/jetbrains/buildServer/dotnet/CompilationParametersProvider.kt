package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.util.OSType
import kotlin.coroutines.experimental.buildSequence

/**
 * Prevents the case when VBCSCompiler service remains in memory after `dotnet build` for Linux and consumes 100% of 1 CPU core and a lot of memory
 * https://youtrack.jetbrains.com/issue/TW-55268
 * https://github.com/dotnet/roslyn/issues/27566
 */

class CompilationParametersProvider(
        private val _environment: Environment,
        private val _dotnetCliToolInfo: DotnetCliToolInfo)
    : MSBuildParametersProvider {
    override val parameters: Sequence<MSBuildParameter>
        get() = buildSequence {
            if (_dotnetCliToolInfo.Version <= MultiAdapterPathVersion) {
                return@buildSequence
            }

            when(_environment.OS) {
                OSType.UNIX, OSType.MAC -> yield(MSBuildParameter("UseSharedCompilation", "false"))
                else -> return@buildSequence
            }
        }

    companion object {
        val MultiAdapterPathVersion: Version = Version(2, 1, 105)
    }
}