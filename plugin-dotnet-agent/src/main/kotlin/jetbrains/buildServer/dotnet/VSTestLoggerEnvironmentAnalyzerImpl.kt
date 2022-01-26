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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.Logger
import java.io.File

class VSTestLoggerEnvironmentAnalyzerImpl(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _loggerService: LoggerService)
    : VSTestLoggerEnvironmentAnalyzer {

    override fun analyze(targets: List<File>) {
        LOG.debug("Analyze targets to run tests")
        val checkoutDir = _pathsService.getPath(PathType.Checkout)
        val checkoutCanonical = checkoutDir.absoluteFile.canonicalPath
        val invalidTargets = mutableListOf<File>()
        val allTargets = targets.toMutableList()
        var useWorkingDirectory = false
        if (allTargets.isEmpty()) {
            allTargets.add(_pathsService.getPath(PathType.WorkingDirectory))
            useWorkingDirectory = true
        }

        for (target in allTargets) {
            if (_fileSystemService.isAbsolute(target)) {
                if (!target.absoluteFile.canonicalPath.startsWith(checkoutCanonical)) {
                    invalidTargets.add(target)
                    LOG.debug("\"$target\" is invalid to run tests")
                }

                continue
            }

            LOG.debug("\"$target\" is ok to run tests")
        }

        if (invalidTargets.isNotEmpty()) {
            val invalidTargetsList = invalidTargets.distinctBy { it.absolutePath }.joinToString(", ") { it.path }
            val targetType = if (useWorkingDirectory) "directory \"$invalidTargetsList\" is" else "file(s) \"$invalidTargetsList\" are"
            val warning = "The $targetType located outside of the build checkout directory: \"$checkoutDir\". In this case there can be problems with running this build tests on TeamCity agent. Please refer to this issue for details: https://youtrack.jetbrains.com/issue/TW-52485"
            LOG.warn(warning)
            _loggerService.writeWarning(warning)
        }
    }

    companion object {
        private val LOG = Logger.getLogger(VSTestLoggerEnvironmentAnalyzerImpl::class.java)
    }
}