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

import jetbrains.buildServer.agent.Logger

class MessageIndicesSource(
        private val _positionsSource: Source<Long>)
    : Source<Index> {
    override fun read(source: String, fromPosition: Long, count: Long) = sequence {
        if (source.isNullOrBlank()) {
            throw IllegalArgumentException()
        }

        var initPosition = fromPosition
        var mesageCount = count
        var firstPosition = true
        if (initPosition > 0L) {
            initPosition--
            mesageCount++
        }

        var prevMesssagePosotion = 0L
        for (messagePosition in _positionsSource.read(source, initPosition, mesageCount)) {
            if (firstPosition && initPosition != fromPosition) {
                prevMesssagePosotion = messagePosition
                firstPosition = false
                continue
            }

            if (messagePosition > prevMesssagePosotion) {
                yield(Index(prevMesssagePosotion, messagePosition - prevMesssagePosotion))
                prevMesssagePosotion = messagePosition
            }
            else {
                LOG.warn("Invalid position $messagePosition, the previous position was $prevMesssagePosotion.")
                break
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(MessageIndicesSource::class.java)
    }
}