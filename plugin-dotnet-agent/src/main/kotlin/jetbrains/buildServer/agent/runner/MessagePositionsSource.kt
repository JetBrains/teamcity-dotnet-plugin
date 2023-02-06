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
import java.nio.ByteBuffer

private const val Long_SIZE = 8L

class MessagePositionsSource(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService)
    : Source<Long> {
    override fun read(source: String, fromPosition: Long, count: Long) = sequence<Long> {
        if (source.isNullOrBlank() || count < 0L) {
            throw IllegalArgumentException()
        }

        if (count > 0L) {
            val sourceFile = File(_pathsService.getPath(PathType.AgentTemp), source)
            if (_fileSystemService.isExists(sourceFile) && _fileSystemService.isFile(sourceFile)) {
                val bytes = ByteArray((count * Long_SIZE).toInt())
                var offset = fromPosition * Long_SIZE
                if (_fileSystemService.readBytes(sourceFile, sequenceOf(FileReadOperation(offset.toLong(), bytes))).singleOrNull()?.bytesRead == bytes.size) {
                    val buffer = ByteBuffer.wrap(bytes);
                    var positionIndex = 0L
                    var prevPosition = 0L
                    do {
                        val position = buffer.getLong();
                        if (position >= prevPosition) {
                            yield(position)
                            prevPosition = position
                        } else {
                            LOG.warn("Corrupted file \"$sourceFile\" at ${positionIndex * Long_SIZE}, the current position is $position but the previous position is $prevPosition.")
                            return@sequence
                        }
                    } while (++positionIndex < count)
                } else {
                    LOG.warn("Cannot read \"$sourceFile\" at position $offset and size ${bytes.size}.")
                }
            } else {
                LOG.debug("Cannot find \"$sourceFile\".")
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(MessagePositionsSource::class.java)
    }
}

