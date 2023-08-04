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

import jetbrains.buildServer.DocElement
import jetbrains.buildServer.agent.FileSystemService
import java.io.File

/**
 * Provides file specification, for example:
 * <File Path="C:\temp\file.nupkg"></File>
 */
class FilePluginSource(
    private val _fileSystemService: FileSystemService
) : PluginSource {
    override val id = "file"

    override fun getPlugin(specification: String) =
        File(specification).let { file ->
            val result = DocElement("File")
            if (_fileSystemService.isExists(file) && _fileSystemService.isFile(file)) {
                result.a("Path", file.canonicalFile.absolutePath)
            }

            result
        }
}