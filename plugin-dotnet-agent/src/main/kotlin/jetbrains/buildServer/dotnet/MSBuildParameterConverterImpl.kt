package jetbrains.buildServer.dotnet

import jetbrains.buildServer.util.StringUtil

class MSBuildParameterConverterImpl : MSBuildParameterConverter {
    override fun convert(source: MSBuildParameter): String = "/p:${toString(normalizeName(source.name))}=${normalizeValue(source.value)}"

    private fun normalizeValue(value: String): String {
        val str = toString(escapeSymbols(value.asSequence()))
        if (str.isBlank() || str.contains(';')) {
            return StringUtil.doubleQuote(StringUtil.unquoteString(str))
        }

        return str
    }

    private fun normalizeName(name: String): Sequence<Char> = sequence {
        for (char in name.asSequence()) {
            if (char.isLetterOrDigit() || char == '_') {
                yield(char)
            } else {
                yield('_')
            }
        }
    }

    private fun escapeSymbols(chars: Sequence<Char>): Sequence<Char> = sequence {
        for (char in chars) {
            if (char.isLetterOrDigit() || char == ';' || char == '%') {
                yield(char)
            } else {
                yield('%')
                for (c in String.format("%02X", char.toByte())) {
                    yield(c)
                }
            }
        }
    }

    private fun toString(chars: Sequence<Char>): String = String(chars.toList().toCharArray())
}