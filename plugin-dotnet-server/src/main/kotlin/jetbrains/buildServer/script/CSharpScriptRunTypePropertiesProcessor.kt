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

import jetbrains.buildServer.ToolVersionProvider
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor

class CSharpScriptRunTypePropertiesProcessor(
        private val _toolVersionProvider: ToolVersionProvider)
    : PropertiesProcessor {
    override fun process(properties: Map<String, String>) =
        validate(properties).toCollection(mutableListOf())

    private fun validate(properties: Map<String, String>) = sequence {
        if (properties[ScriptConstants.CLT_PATH].isNullOrBlank()) {
            yield(InvalidProperty(ScriptConstants.CLT_PATH, "The path to ${ScriptConstants.RUNNER_DESCRIPTION} must be specified"))
        }

        val scriptType = properties[ScriptConstants.SCRIPT_TYPE]?.let { ScriptType.tryParse(it) }
        if (scriptType == null) {
            yield(InvalidProperty(ScriptConstants.SCRIPT_TYPE, "Script type is not specified"))
        }

        when(scriptType) {
            ScriptType.Custom ->
                if(properties[ScriptConstants.SCRIPT_CONTENT].isNullOrBlank()) {
                    yield(InvalidProperty(ScriptConstants.SCRIPT_CONTENT, "Custom script content is not provided"))
                }

            ScriptType.File ->
                if(properties[ScriptConstants.SCRIPT_FILE].isNullOrBlank()) {
                    yield(InvalidProperty(ScriptConstants.SCRIPT_FILE, "Script file path is not specified"))
                }

            else -> { }
        }
    }
}