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

package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.ToolVersionOutputParser
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class InspectionVersionParser : ToolVersionOutputParser {
    override fun parse(output: Collection<String>): Version {
        return output
            .map { tryParseServiceMessage(it) ?: it }
            .mapNotNull { VERSION_OUTPUT_LINE_REGEX.matchEntire(it) }
            .map {
                val versionStr = it.groupValues[1]
                Version.parseSimplified(versionStr.trim())
            }
            .firstOrNull { !it.isEmpty() } ?: Version.Empty
    }

    private fun tryParseServiceMessage(messageStr: String): String? {
        try {
            return ServiceMessage.parse(messageStr)?.attributes?.get("text")
        } catch (e: Exception) {
            LOG.warn("Failed to parse service message $messageStr", e)
        }

        return messageStr
    }

    companion object {
        private val LOG = Logger.getLogger(InspectionVersionParser::class.java)
        private val VERSION_OUTPUT_LINE_REGEX = Regex("^Version:[^\\S\\r\\n]+(.+)\$")
    }
}