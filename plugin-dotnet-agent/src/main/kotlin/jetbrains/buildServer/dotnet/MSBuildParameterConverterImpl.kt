package jetbrains.buildServer.dotnet

import jetbrains.buildServer.util.StringUtil

class MSBuildParameterConverterImpl : MSBuildParameterConverter {
    override fun convert(parameters: Sequence<MSBuildParameter>, isCommandLineParameters: Boolean) = parameters
            .filter { parameter -> parameter.name.isNotBlank() && parameter.value.isNotBlank() }
            .map { "/p:${normalizeName(it.name)}=${normalizeValue(it.value, isCommandLineParameters)}" }

    fun normalizeName(name: String) =
            String(
                name.mapIndexed { index: Int, c: Char ->
                    if ( if(index == 0) isValidInitialElementNameCharacter(c) else isValidSubsequentElementNameCharacter(c) ) c else '_'
                }.toCharArray()
            )

    fun normalizeValue(value: String, isCommandLineParameter: Boolean): String {
        val str = String(escapeSymbols(value.asSequence(), isCommandLineParameter).toList().toCharArray())
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

    private fun escapeSymbols(chars: Sequence<Char>, isCommandLineParameter: Boolean): Sequence<Char> = sequence {
        for (char in chars) {
            if (char.isLetterOrDigit() || (char == ';' && !isCommandLineParameter) || char == '%') {
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