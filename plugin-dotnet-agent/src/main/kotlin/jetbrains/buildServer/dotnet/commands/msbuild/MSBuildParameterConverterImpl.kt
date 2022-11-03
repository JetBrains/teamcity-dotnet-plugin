package jetbrains.buildServer.dotnet.commands.msbuild

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.StringUtil

class MSBuildParameterConverterImpl(
    private val _parameterService: ParametersService,
) : MSBuildParameterConverter {
    private val hasParametersEscapingBeenEnabled get() =
        _parameterService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_MSBUILD_PARAMETERS_ESCAPE)
            ?.let { it.trim().equals("true", ignoreCase = true) }
            ?: false

    override fun convert(parameters: Sequence<MSBuildParameter>): Sequence<String> {
        val allParamsEscapingEnabled = hasParametersEscapingBeenEnabled
        return parameters
            .filter { parameter -> parameter.name.isNotBlank() && parameter.value.isNotBlank() }
            .map { parameter ->
                val normalizedName = normalizeName(parameter.name)
                val normalizedValue = normalizeValue(parameter.value) { shouledBeEscaped(parameter.type, it, allParamsEscapingEnabled) }
                "-p:${normalizedName}=${normalizedValue}"
            }
    }

    fun normalizeName(name: String) =
        String(
            name.mapIndexed { index: Int, c: Char ->
                if ( if(index == 0) isValidInitialElementNameCharacter(c) else isValidSubsequentElementNameCharacter(c) ) c else '_'
            }.toCharArray()
        )

    fun normalizeValue(value: String, shouledBeEscaped: (Char) -> Boolean): String {
        val str = String(escapeSymbols(value.asSequence(), shouledBeEscaped).toList().toCharArray())
        if (str.isBlank() || SpecialCharacters.any { str.contains(it)}) {
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

    private fun escapeSymbols(chars: Sequence<Char>, shouledBeEscaped: (Char) -> Boolean): Sequence<Char> = sequence {
        var lastIsSlash = false
        for (char in chars) {
            lastIsSlash = false
            when {
                char == '\\' -> {
                    yield('\\')
                    lastIsSlash = true
                }

                shouledBeEscaped(char) -> yieldAll(escape(char))
                else -> yield(char)
            }
        }

        // https://youtrack.jetbrains.com/issue/TW-72915
        if(lastIsSlash) {
            yield('\\')
        }
    }

    fun escape(char: Char) = sequence {
        yield('%')
        for (c in String.format("%02X", char.code.toByte())) {
            yield(c)
        }
    }

    companion object {
        private val SpecialCharacters = hashSetOf(' ', '%', ';')

        internal fun shouledBeEscaped(parameterType: MSBuildParameterType, char: Char, allParamsEscapingEnabled: Boolean) = when {
            // invisible
            char.isISOControl() -> true
            // predefined parameters
            allParamsEscapingEnabled || parameterType == MSBuildParameterType.Predefined -> when {
                char.isLetterOrDigit() -> false
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
            char == '"' -> true
            else -> false
        }
    }
}