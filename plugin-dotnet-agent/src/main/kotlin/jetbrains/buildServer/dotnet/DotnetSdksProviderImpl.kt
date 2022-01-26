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
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.Logger
import java.io.File

class DotnetSdksProviderImpl(
        private val _fileSystemService: FileSystemService)
    : DotnetSdksProvider {
    override fun getSdks(dotnetExecutable: File): Sequence<DotnetSdk> {
        val sdksPath = File(dotnetExecutable.parent, "sdk")
        if(!_fileSystemService.isExists(sdksPath) || !_fileSystemService.isDirectory(sdksPath)) {
            LOG.warn("The directory <$sdksPath> does not exists.")
        }

        LOG.debug("Try getting the list of .NET SDK from directory <$sdksPath>.")
        return _fileSystemService.list(sdksPath)
                .filter { _fileSystemService.isDirectory(it) }
                .map { DotnetSdk(it, Version.parse(it.name)) }
                .filter { it.version != Version.Empty }
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetSdksProviderImpl::class.java)
    }
}