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

package jetbrains.buildServer.dotnet.discovery.mstest

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.PEReader
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceFactory
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.Platform
import java.io.File

class MSTestConsoleInstanceFactory(
        private val _fileSystemService: FileSystemService,
        private val _peReader: PEReader)
    : ToolInstanceFactory {

    override fun tryCreate(path: File, baseVersion: Version, platform: Platform): ToolInstance? {
        if (!_fileSystemService.isExists(path) || !_fileSystemService.isDirectory(path)) {
            LOG.debug("Cannot find \"$path\".")
            return null
        }
        else {
            val mstestFile = File(path, "MSTest.exe")
            if (!_fileSystemService.isExists(mstestFile) || !_fileSystemService.isFile(mstestFile)) {
                LOG.debug("Cannot find \"$mstestFile\".")
                return null
            }
            else {
                var detailedVersion = _peReader.tryGetVersion(mstestFile)
                if (detailedVersion == Version.Empty) {
                    LOG.warn("Cannot get a product version from \"$mstestFile\".")
                    return null
                }

                return ToolInstance(ToolInstanceType.MSTest, mstestFile, detailedVersion, Version(detailedVersion.major, detailedVersion.minor), platform)
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(MSTestConsoleInstanceFactory::class.java)
    }
}