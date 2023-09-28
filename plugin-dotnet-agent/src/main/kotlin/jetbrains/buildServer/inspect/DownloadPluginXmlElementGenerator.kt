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

import jetbrains.buildServer.XmlElement
import jetbrains.buildServer.agent.runner.LoggerService

/**
 * Provides download specification, for example:
 * <Download Id="Plugin.Id" Version="1.2.0.0"></Download>
 */
class DownloadPluginXmlElementGenerator(
    private val _loggerService: LoggerService
) : PluginXmlElementGenerator {
    override val sourceId = "download"

    override fun generateXmlElement(strValue: String) =
        strValue.split("/").let { parts ->
            val result = XmlElement("Download")
            if (parts.size == 2) {
                result
                    .withAttribute("Id", parts[0])
                    .withAttribute("Version", parts[1])
            } else {
                _loggerService.writeWarning("Invalid R# CLT plugin descriptor for downloading: \"$strValue\", it will be ignored.")
            }

            result
        }
}