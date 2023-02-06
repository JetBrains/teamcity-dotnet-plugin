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

import jetbrains.buildServer.E
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.util.FileUtil
import java.io.File

/**
 * Provides folder specification, for example:
 * <Folder Path="C:\temp"></Folder>
 */
class FolderPluginSource(
        private val _fileSystemService: FileSystemService)
    : PluginSource {
    override val id = "folder"

    override fun getPlugin(specification: String) =
        File(specification).let {
            directory ->
            val result = E("Folder")
            if (_fileSystemService.isExists(directory) && _fileSystemService.isDirectory(directory)) {
                result.a("Path", directory.canonicalFile.absolutePath)
            }

            result
        }
}