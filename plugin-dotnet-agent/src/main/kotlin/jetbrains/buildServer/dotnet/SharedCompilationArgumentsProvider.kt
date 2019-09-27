package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType

class SharedCompilationArgumentsProvider(
        private val _sharedCompilation: SharedCompilation): ArgumentsProvider {
    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        if (_sharedCompilation.requireSuppressing(context.toolVersion)) {
            yield(nodeReuseArgument)
        }
    }

    companion object {
        val nodeReuseArgument = CommandLineArgument("/nodeReuse:false", CommandLineArgumentType.Infrastructural)
    }
}