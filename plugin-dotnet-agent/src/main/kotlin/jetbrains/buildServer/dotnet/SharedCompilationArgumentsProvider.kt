package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import kotlin.coroutines.experimental.buildSequence

class SharedCompilationArgumentsProvider(
        private val _sharedCompilation: SharedCompilation): ArgumentsProvider {
    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        if (_sharedCompilation.requireSuppressing(context.currentSdk.version)) {
            yield(nodeReuseArgument)
        }
    }

    companion object {
        val nodeReuseArgument = CommandLineArgument("/nodeReuse:false")
    }
}