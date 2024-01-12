

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument

class DotnetCommonArgumentsProviderImpl(
    private val _useRspFile: Boolean,
    private val _responseFileArgumentsProvider: ArgumentsProvider,
    private val _customArgumentsProvider: ArgumentsProvider,
    private val _argumentsProviders: List<ArgumentsProvider>
) : DotnetCommonArgumentsProvider {
    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
        if (_useRspFile) {
            yieldAll(_responseFileArgumentsProvider.getArguments(context))
        } else {
            for (argumentsProvider in _argumentsProviders) {
                yieldAll(argumentsProvider.getArguments(context))
            }
        }

        yieldAll(_customArgumentsProvider.getArguments(context))
    }
}