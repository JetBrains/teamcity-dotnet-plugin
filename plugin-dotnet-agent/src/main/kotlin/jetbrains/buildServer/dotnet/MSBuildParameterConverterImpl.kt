package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.ArgumentsService
import kotlin.coroutines.experimental.buildSequence

class MSBuildParameterConverterImpl(): MSBuildParameterConverter {
    override fun convert(source: MSBuildParameter): String = "/p:${toString(normalizeName(source.name))}=${normalizeValue(source.value)}"

    private fun normalizeValue(value: String): String {
        if (value.isNullOrEmpty()) {
            return "\"\"";
        }

        return toString(escape(value))
    }

    private fun normalizeName(name: String): Sequence<Char> = buildSequence {
        for (char in name.asSequence()) {
            if (char.isLetterOrDigit() || char == '_') {
                yield(char)
            }
            else {
                yield('_')
            }
        }
    }

    private fun escape(name: String): Sequence<Char> = buildSequence {
        for (char in name.asSequence()) {
            if(char.isLetterOrDigit()) {
                yield(char)
            }
            else {
                yield('%')
                for (c in String.format("%02X", char.toByte())) {
                    yield(c)
                }
            }
        }
    }

    private fun toString(chars: Sequence<Char>): String = String(chars.toList().toCharArray())
}