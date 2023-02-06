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

package jetbrains.buildServer.script

import jetbrains.buildServer.dotnet.Tool

enum class ScriptType(
        val id: String,
        val description: String) {

    Custom("customScript", "Custom Script"),
    File("file", "Script File");

    companion object {
        fun tryParse(id: String): ScriptType? {
            return ScriptType.values().singleOrNull { it.id.equals(id, true) }
        }
    }
}