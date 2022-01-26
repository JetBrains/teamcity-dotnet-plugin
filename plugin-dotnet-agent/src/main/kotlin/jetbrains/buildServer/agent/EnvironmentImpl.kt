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

import jetbrains.buildServer.agent.impl.OSTypeDetector
import jetbrains.buildServer.util.OSType
import jetbrains.buildServer.util.StringUtil
import java.io.File

class EnvironmentImpl(
        private val _fileSystemService: FileSystemService,
        private val _osTypeDetector: OSTypeDetector)
    : Environment {

    override fun tryGetVariable(name: String): String? {
        return System.getenv(name)
    }

    override val paths: Sequence<Path> get() =
            tryGetVariable(PathEnvironmentVariableName)?.let { path ->
                StringUtil.splitHonorQuotes(path, File.pathSeparatorChar)
                        .asSequence()
                        .map { Path(it) }
                        .filter { _fileSystemService.isExists(File(it.path)) }
            } ?: emptySequence()

    override val os: OSType
        get() = _osTypeDetector.detect() ?: OSType.UNIX

    override val osName: String?
        get() = System.getProperty("os.name")

    companion object {
        private const val PathEnvironmentVariableName = "PATH"
    }
}