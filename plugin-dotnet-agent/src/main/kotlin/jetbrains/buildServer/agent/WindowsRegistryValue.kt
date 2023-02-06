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

import WindowsRegistryValueType

data class WindowsRegistryValue(
        val key: WindowsRegistryKey,
        val type: WindowsRegistryValueType,
        private val _value: Any) {

    val text: String get() =
        when(type) {
            WindowsRegistryValueType.Str -> _value as String
            WindowsRegistryValueType.Text -> _value as String
            WindowsRegistryValueType.ExpandText -> _value as String
            else -> ""
        }

    val number: Long get() =
        when(type) {
            WindowsRegistryValueType.Int -> (_value as Int).toLong()
            WindowsRegistryValueType.Long -> _value as Long
            else -> 0L
        }

    override fun toString(): String {
        return "$type $key = $_value"
    }
}