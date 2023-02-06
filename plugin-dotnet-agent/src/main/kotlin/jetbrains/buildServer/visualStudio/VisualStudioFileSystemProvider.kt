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

package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.agent.Logger
import org.springframework.cache.annotation.Cacheable
import java.io.File

class VisualStudioFileSystemProvider(
        private val _packagesLocators: List<VisualStudioPackagesLocator>,
        private val _fileSystemService: FileSystemService,
        private val _visualStudioInstancesParser: VisualStudioInstanceParser)
    : ToolInstanceProvider {
    @Cacheable("ListOfVisualStuioFromFileSystem", sync = true)
    override fun getInstances(): Collection<ToolInstance> =
            _packagesLocators
                    // C:\ProgramData\Microsoft\VisualStudio\Packages
                    .mapNotNull { it.tryGetPackagesPath() }
                    // C:\ProgramData\Microsoft\VisualStudio\Packages\_Instances
                    .map { File(it, "_Instances") }
                    .filter { _fileSystemService.isExists(it) }
                    // dir C:\ProgramData\Microsoft\VisualStudio\Packages\_Instances
                    .filter {
                        LOG.debug("Goes through \"$it\".")
                        _fileSystemService.isDirectory(it)
                    }
                    .flatMap { _fileSystemService.list(it).asIterable() }
                    // dir C:\ProgramData\Microsoft\VisualStudio\Packages\_Instances\*
                    .filter {
                        LOG.debug("Goes through \"$it\".")
                        _fileSystemService.isDirectory(it)
                    }
                    .flatMap { _fileSystemService.list(it).asIterable() }
                    .asSequence()
                    // dir C:\ProgramData\Microsoft\VisualStudio\Packages\_Instances\*\state.json
                    .filter {
                        LOG.debug("Goes through \"$it\".")
                        _fileSystemService.isFile(it)
                    }
                    .filter { "state.json".equals(it.name, true) }
                    // parse state.json
                    .mapNotNull {
                        var instance: ToolInstance? = null
                        LOG.debug("Parsing \"$it\".")
                        try {
                            _fileSystemService.read(it) {
                                instance = _visualStudioInstancesParser.tryParse(it)
                            }
                        } catch (error: Exception) {
                            LOG.error("Error while parsing \"$it\".", error)
                        }

                        LOG.debug("Found $instance");
                        instance
                    }
                   .toList()

    companion object {
        private val LOG = Logger.getLogger(VisualStudioFileSystemProvider::class.java)
    }
}