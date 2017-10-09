package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class SystemParametersArgumentsProvider(
        private val _parametersService: ParametersService)
    : ArgumentsProvider {
    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            for (paramName in _parametersService.getParameterNames(ParameterType.System)) {
                _parametersService.tryGetParameter(ParameterType.System, paramName)?.let {
                    yield(CommandLineArgument("/p:${normalizePropertyName(paramName)}=$it"))
                }
            }
        }

    companion object {
        internal fun normalizePropertyName(paramName: String): String =
                String(normalizeCharSequence(paramName.asSequence()).toList().toCharArray())

        private fun normalizeCharSequence(charSequence: Sequence<Char>): Sequence<Char> = buildSequence {
            for (char in charSequence) {
                if (char.isLetterOrDigit() || char == '_') {
                    yield(char)
                }
                else {
                    yield('_')
                }
            }
        }
    }
}