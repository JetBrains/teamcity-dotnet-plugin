package jetbrains.buildServer.dotnet

import kotlin.coroutines.experimental.buildSequence

class MSBuildParameterConverterImpl : MSBuildParameterConverter {
    override fun convert(source: MSBuildParameter): String = "/p:${toString(normalizeName(source.name))}=${toString(normalizeValue(source.value))}"

    private fun normalizeValue(value: String): Sequence<Char> = buildSequence {
        yield('\"')
        for (char in value.asSequence()) {
            if (SpecialSymbols.contains(char)) {
                yield('\\')
            }

            yield(char)
        }

        yield('\"')
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

    private fun toString(chars: Sequence<Char>): String = String(chars.toList().toCharArray())

    companion object {
        private val SpecialSymbols = hashSetOf('\\', '\"')
    }
}