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
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import java.io.File

/**
 * Provides file specification, for example:
 * <File Path="C:\temp\file.nupkg"></File>
 */
class FilePluginXmlElementGenerator(
    private val _fileSystemService: FileSystemService,
    private val _loggerService: LoggerService
) : PluginXmlElementGenerator {
    override val sourceId = "file"

    override fun generateXmlElement(strValue: String) =
        File(strValue).let { file ->
            val result = XmlElement("File")
            if (_fileSystemService.isExists(file) && _fileSystemService.isFile(file)) {
                result.withAttribute("Path", file.canonicalFile.absolutePath)
            } else {
                _loggerService.writeWarning("Invalid R# CLT plugin file descriptor, file $file does not exist or is not a file, it will be ignored.")
            }

            result
        }
}