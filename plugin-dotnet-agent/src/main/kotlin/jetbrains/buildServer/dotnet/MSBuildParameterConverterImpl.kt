package jetbrains.buildServer.dotnet

import jetbrains.buildServer.util.StringUtil

class MSBuildParameterConverterImpl : MSBuildParameterConverter {
    override fun convert(parameters: Sequence<MSBuildParameter>) = parameters
            .filter { parameter -> parameter.name.isNotBlank() && parameter.value.isNotBlank() }
            .map { "/p:${normalizeName(it.name)}=${normalizeValue(it.value)}" }

    override fun normalizeName(name: String) =
            String(
                name.mapIndexed { index: Int, c: Char ->
                    if ( if(index == 0) isValidInitialElementNameCharacter(c) else isValidSubsequentElementNameCharacter(c) ) c else '_'
                }.toCharArray()
            )

    override fun normalizeValue(value: String): String {
        val str = String(escapeSymbols(value.asSequence()).toList().toCharArray())
        if (str.isBlank() || str.contains(';')) {
            return StringUtil.doubleQuote(StringUtil.unquoteString(str))
        }

        return str
    }

    private fun isValidInitialElementNameCharacter(c: Char) =
        (c >= 'A' && c <= 'Z') ||
        (c >= 'a' && c <= 'z') ||
        (c == '_')

    private fun isValidSubsequentElementNameCharacter(c: Char) =
        (c >= 'A' && c <= 'Z') ||
        (c >= 'a' && c <= 'z') ||
        (c >= '0' && c <= '9') ||
        (c == '_') ||
        (c == '-')

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
}