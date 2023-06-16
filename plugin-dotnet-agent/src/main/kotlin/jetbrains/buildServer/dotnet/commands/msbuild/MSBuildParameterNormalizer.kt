package jetbrains.buildServer.dotnet.commands.msbuild

import jetbrains.buildServer.util.StringUtil

object MSBuildParameterNormalizer {
    private val SpecialCharacters = hashSetOf(' ', '%', ';')

    fun normalizeName(name: String) =
        String(
            name.mapIndexed { index: Int, c: Char ->
                if (if (index == 0) isValidInitialElementNameCharacter(c) else isValidSubsequentElementNameCharacter(c)) c else '_'
            }.toCharArray()
        )

    fun normalizeValue(value: String, fullEscaping: Boolean) =
        String(
            escapeSymbols(value.asSequence()) { char -> shouldBeEscaped(char, fullEscaping) }
                .toList()
                .toCharArray()
        )

    fun normalizeAndQuoteValue(value: String, fullEscaping: Boolean, quoteOnOneTrailingBackslash: Boolean): String {
        val str = normalizeValue(value, fullEscaping)
        if (isDoubleQuoteRequired(str, quoteOnOneTrailingBackslash)) {
            return StringUtil.doubleQuote(StringUtil.unquoteString(str))
        }

        return str
    }

    private fun isDoubleQuoteRequired(str: String, quoteOneTrailingBackslash: Boolean): Boolean {
        val endsWithOneBackslash = countTrailingBackslashes(str) == 2 // one backslash is escaping
        return str.isBlank()
                || SpecialCharacters.any { str.contains(it) }
                || (quoteOneTrailingBackslash && endsWithOneBackslash)
    }

    private fun countTrailingBackslashes(str: String): Int {
        var backslashesCount = 0
        val chars = str.toCharArray()
        for (i in (0..chars.lastIndex).reversed()) {
            if (chars[i] != '\\') {
                break
            }
            backslashesCount++
        }
        return backslashesCount
    }

    private fun isValidInitialElementNameCharacter(c: Char) =
        (c in 'A'..'Z') ||
                (c in 'a'..'z') ||
                (c == '_')

    private fun isValidSubsequentElementNameCharacter(c: Char) =
        (c in 'A'..'Z') ||
                (c in 'a'..'z') ||
                (c in '0'..'9') ||
                (c == '_') ||
                (c == '-')

    private fun escapeSymbols(chars: Sequence<Char>, shouldBeEscaped: (Char) -> Boolean): Sequence<Char> = sequence {
        var lastIsSlash = false
        for (char in chars) {
            lastIsSlash = false
            when {
                char == '\\' -> {
                    yield('\\')
                    lastIsSlash = true
                }

                shouldBeEscaped(char) -> yieldAll(escape(char))
                else -> yield(char)
            }
        }

        // https://youtrack.jetbrains.com/issue/TW-72915
        if (lastIsSlash) {
            yield('\\')
        }
    }

    private fun escape(char: Char) = sequence {
        yield('%')
        for (c in String.format("%02X", char.code.toByte())) {
            yield(c)
        }
    }

    private fun shouldBeEscaped(char: Char, fullEscaping: Boolean) =
        when {
            char == '"' -> true
            char.isISOControl() -> true // invisible
            char.isLetterOrDigit() -> false

            fullEscaping -> when {
                char == ' ' -> false
                char == '\\' -> false
                char == '/' -> false
                char == '.' -> false
                char == '-' -> false
                char == '_' -> false
                char == '%' -> false
                char == ':' -> false
                char == ')' -> false
                char == '(' -> false
                else -> true
            }

            else -> false
        }
}