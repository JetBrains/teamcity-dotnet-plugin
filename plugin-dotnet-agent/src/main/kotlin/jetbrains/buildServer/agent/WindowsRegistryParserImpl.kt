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

package jetbrains.buildServer.agent

class WindowsRegistryParserImpl : WindowsRegistryParser {
    override fun tryParseKey(key: WindowsRegistryKey, text: String): WindowsRegistryKey? {
        val trimmed = text.trimStart()
        if (trimmed.length < key.regKey.length + 2) {
            return null
        }

        if (!trimmed.lowercase().startsWith(key.regKey.lowercase())) {
            return null
        }

        return key + trimmed.substring(key.regKey.length + 1 .. trimmed.length - 1).split("\\")
    }

    override fun tryParseValue(key: WindowsRegistryKey, text: String): WindowsRegistryValue? =
        RegItemRegex.find(text)?.let { match ->
            val subKey = match.groupValues[2].let { name ->
                key + name
            }

            val type = match.groupValues[4].let { typeStr ->
                WindowsRegistryValueType.tryParse(typeStr)
            } ?: WindowsRegistryValueType.Text

            val value = match.groupValues[6].let { text ->
                when (type) {
                    WindowsRegistryValueType.Str -> text
                    WindowsRegistryValueType.Bin -> arrayOf<Byte>(0)
                    WindowsRegistryValueType.Int -> text.replace("0x", "").toIntOrNull(16) ?: 0
                    WindowsRegistryValueType.Long -> text.replace("0x", "").toLongOrNull(16)?: 0L
                    WindowsRegistryValueType.Text -> text.split("\\0").joinToString("\n")
                    WindowsRegistryValueType.ExpandText -> text
                }
            }

            WindowsRegistryValue(subKey, type, value)
        }

    companion object {
        private val RegItemRegex = Regex("^(\\s{4}|\\s*\\t)(.+)(\\s{4}|\\t)(REG_SZ|REG_BINARY|REG_DWORD|REG_QWORD|REG_MULTI_SZ|REG_EXPAND_SZ)(\\s{4}|\\t)(.*)\$")
    }
}