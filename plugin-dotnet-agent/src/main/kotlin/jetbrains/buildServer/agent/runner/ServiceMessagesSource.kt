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

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.FileReadOperation
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import java.io.File

class ServiceMessagesSource(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _indicesSource: Source<Index>)
    : Source<String> {
    override fun read(source: String, fromPosition: Long, count: Long) = sequence {
        if (source.isNullOrBlank() || count < 0L) {
            throw IllegalArgumentException()
        }

        val sourceFile = File(_pathsService.getPath(PathType.AgentTemp), source + ".msg")
        if (_fileSystemService.isExists(sourceFile) && _fileSystemService.isFile(sourceFile)) {
            val readOperations = _indicesSource.read(source, fromPosition, count).map { FileReadOperation(it.fromPosition, ByteArray(it.size.toInt())) }
            val readResults = _fileSystemService.readBytes(sourceFile, readOperations)
            for (readResult in readResults) {
                if (readResult.operation.to.size == readResult.bytesRead) {
                    yield(String(readResult.operation.to, Charsets.UTF_8).trimEnd())
                } else {
                    LOG.warn("Cannot read \"$sourceFile\" at position ${readResult.operation} and size ${readResult.operation.to.size}.")
                }
            }
        } else {
            LOG.debug("Cannot find \"$sourceFile\".")
        }
    }

    companion object {
        private val LOG = Logger.getLogger(ServiceMessagesSource::class.java)
    }
}