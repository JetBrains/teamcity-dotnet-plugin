package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument

class ResponseFileAlternative(
        private val _responseFileFactory: ResponseFileFactory)
    : ArgumentsAlternative {

    override fun select(description: String, arguments: Collection<CommandLineArgument>, alternativeArguments: Sequence<CommandLineArgument>, alternativeParameters: Sequence<MSBuildParameter>, verbosity: Verbosity?) =
            sequence {
                if (arguments.fold(0, { length, arg -> length + arg.value.length } ) <= MaxArgSize) {
                    yieldAll(arguments)
                }
                else {
                    val respponseFile = _responseFileFactory.createResponeFile(description, alternativeArguments, alternativeParameters.asSequence(), verbosity)
                    yield(CommandLineArgument("@${respponseFile.path}"))
                }
            }

    companion object {
        internal const val MaxArgSize = 1024
    }
}