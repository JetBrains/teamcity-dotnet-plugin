/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import jetbrains.buildServer.util.OSType
import java.io.File

class ToolSearchServiceImpl(
        private val _fileSystem: FileSystemService,
        private val _environment: Environment): ToolSearchService {

    override fun find(toolName: String, paths: Sequence<Path>): Sequence<File> {
        val executableName = when(_environment.os) {
            OSType.UNIX, OSType.MAC -> toolName
            OSType.WINDOWS -> "$toolName.exe"
        }

        val pattern = Regex("^$executableName$")
        return paths
                .flatMap { _fileSystem.list(File(it.path)) }
                .filter { it.name.matches(pattern) }
                .distinct()
    }
}