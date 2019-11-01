package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType

class SharedCompilationArgumentsProvider: ArgumentsProvider {
    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        if(context.toolVersion > Version.LastVersionWithoutSharedCompilation) {
            yield(nodeReuseArgument)
        }
    }

    companion object {
        val nodeReuseArgument = CommandLineArgument("/nodeReuse:false", CommandLineArgumentType.Infrastructural)
    }
}