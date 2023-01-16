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

package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.OSType
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

class DotCoverEnvironmentVariables(
    private val _environment: Environment,
    private val _virtualContext: VirtualContext,
    private val _fileSystemService: FileSystemService,
    private val _pathsService: PathsService,
) : EnvironmentVariables {
    override fun getVariables(): Sequence<CommandLineEnvironmentVariable> = sequence {
        if (_virtualContext.targetOSType == OSType.UNIX) {
            yieldAll(linuxDefaultVariables)
        }

        // dotCover doesn't work with long temp dir paths on Unix-like systems
        // because tries to create named pipes with short struct. 60 symbols only
        if (_virtualContext.targetOSType != OSType.WINDOWS) {
            if (_virtualContext.isVirtual && _environment.os == OSType.WINDOWS) {
                LOG.debug("Override temp environment variables by empty values")
                yieldAll(getTempDirVariables())
            } else {
                val tempPath = _pathsService.getPath(PathType.BuildTemp).path
                if (tempPath.length <= 60) {
                    LOG.debug("Do not override temp environment variables")
                }
                else {
                    // try to find default /tmp
                    if (_fileSystemService.isExists(defaultTemp) && _fileSystemService.isDirectory(defaultTemp)) {
                        LOG.debug("Override temp environment variables by '${defaultTemp.path}'")
                        yieldAll(getTempDirVariables(defaultTemp.path))
                    } else {
                        // create short TeamCity temp
                        if (!_fileSystemService.isExists(customTeamCityTemp)) {
                            _fileSystemService.createDirectory(customTeamCityTemp)
                        }

                        LOG.debug("Override temp environment variables by '${customTeamCityTemp}'")
                        yieldAll(getTempDirVariables(customTeamCityTemp.path))
                    }
                }
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(DotCoverEnvironmentVariables::class.java)

        internal val linuxDefaultVariables = sequenceOf(CommandLineEnvironmentVariable("LC_ALL", "C"))

        internal fun getTempDirVariables(tempPath: String = "") = sequenceOf(
            CommandLineEnvironmentVariable("TEMP", tempPath),
            CommandLineEnvironmentVariable("TMP", tempPath),
            CommandLineEnvironmentVariable("TMPDIR", tempPath)
        )

        internal val defaultTemp = File("/tmp")
        internal val customTeamCityTemp = File("~/teamcity_temp")
    }
}