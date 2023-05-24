/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet.commands.msbuild

import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.OSType
import jetbrains.buildServer.util.StringUtil

class MSBuildParameterConverterImpl(
    private val _parameterService: ParametersService,
    private val _environment: Environment
) : MSBuildParameterConverter {
    private val hasParametersEscapingBeenEnabled get() =
        _parameterService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_MSBUILD_PARAMETERS_ESCAPE)
            ?.let { it.trim().equals("true", ignoreCase = true) }
            ?: false
    private val hasTrailingBackslashQuotBeenDisabled get() =
        _parameterService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_MSBUILD_DISABLE_TRAILING_BACKSLASH_QUOTATION)
            ?.let { it.trim().equals("true", ignoreCase = true) }
            ?: false

    override fun convert(parameters: Sequence<MSBuildParameter>): Sequence<String> {
        val allParamsEscapingEnabled = hasParametersEscapingBeenEnabled
        return parameters
            .filter { parameter -> parameter.name.isNotBlank() && parameter.value.isNotBlank() }
            .map { parameter ->
                val normalizedName = normalizeName(parameter.name)
                val normalizedValue = normalizeValue(parameter.value) { shouldBeEscaped(parameter.type, it, allParamsEscapingEnabled) }
                "-p:${normalizedName}=${normalizedValue}"
            }
    }

    fun normalizeName(name: String) =
        String(
            name.mapIndexed { index: Int, c: Char ->
                if ( if(index == 0) isValidInitialElementNameCharacter(c) else isValidSubsequentElementNameCharacter(c) ) c else '_'
            }.toCharArray()
        )

    fun normalizeValue(value: String, shouldBeEscaped: (Char) -> Boolean): String {
        val str = String(escapeSymbols(value.asSequence(), shouldBeEscaped).toList().toCharArray())
        if (isDoubleQuoteRequired(str)) {
            return StringUtil.doubleQuote(StringUtil.unquoteString(str))
        }

        return str
    }

    private fun isDoubleQuoteRequired(str: String): Boolean {
        val isEndWithOneBackslash = countTrailingBackslashes(str) == 2 // one backslash is escaping
        return str.isBlank()
                || SpecialCharacters.any { str.contains(it) }
                || (!hasTrailingBackslashQuotBeenDisabled && isEndWithOneBackslash && _environment.os == OSType.WINDOWS)
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
        (c >= 'A' && c <= 'Z') ||
        (c >= 'a' && c <= 'z') ||
        (c == '_')

    private fun isValidSubsequentElementNameCharacter(c: Char) =
        (c >= 'A' && c <= 'Z') ||
        (c >= 'a' && c <= 'z') ||
        (c >= '0' && c <= '9') ||
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

    fun escape(char: Char) = sequence {
        yield('%')
        for (c in String.format("%02X", char.code.toByte())) {
            yield(c)
        }
    }

    companion object {
        private val SpecialCharacters = hashSetOf(' ', '%', ';')

        internal fun shouldBeEscaped(parameterType: MSBuildParameterType, char: Char, allParamsEscapingEnabled: Boolean) = when {
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