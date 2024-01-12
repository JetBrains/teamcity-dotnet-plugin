

package jetbrains.buildServer.dotnet.commands.responseFile

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameter

class ResponseFileArgumentsProvider(
    private val _responseFileFactory: ResponseFileFactory,
    private val _argumentsProviders: List<ArgumentsProvider>)
    : ArgumentsProvider {
    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
        val args = _argumentsProviders.flatMap { it.getArguments(context).toList() }

        if (args.isEmpty()) {
            return@sequence
        }

        val responseFile = _responseFileFactory.createResponeFile(
                "",
                args.asSequence(),
                emptySequence<MSBuildParameter>(),
                context.verbosityLevel)

        yield(CommandLineArgument("@${responseFile.path}", CommandLineArgumentType.Infrastructural))
    }
}