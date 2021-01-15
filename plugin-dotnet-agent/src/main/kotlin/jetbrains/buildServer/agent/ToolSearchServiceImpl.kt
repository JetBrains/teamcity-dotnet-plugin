/*
 * Copyright 2000-2021 JetBrains s.r.o.
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

import jetbrains.buildServer.FileSystemService
import java.io.File

class ToolSearchServiceImpl(
        private val _fileSystem: FileSystemService): ToolSearchService {

    override fun find(toolName: String, paths: Sequence<Path>): Sequence<File> {
        val pattern = Regex("^$toolName(\\.(exe))?$")
        return paths
                .flatMap { _fileSystem.list(File(it.path)) }
                .filter { it.name.matches(pattern) }
                .distinct()
    }
}