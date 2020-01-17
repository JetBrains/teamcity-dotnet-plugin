/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

package jetbrains.buildServer.dotnet.discovery

import com.intellij.openapi.diagnostic.Logger

class SolutionDiscoverImpl(
        private val _discoverers: List<SolutionDeserializer>)
    : SolutionDiscover {

    override fun discover(streamFactory: StreamFactory, paths: Sequence<String>): Sequence<Solution> =
            paths.map { createSolutionSource(streamFactory, it) }.flatMap { it }

    private fun createSolutionSource(streamFactory: StreamFactory, path: String): Sequence<Solution> = sequence {
        LOG.debug("Discover \"$path\"")
        for (discoverer in _discoverers) {
            if (!discoverer.accept(path)) {
                continue
            }

            LOG.debug("Use discoverer \"$discoverer\" for \"$path\"")
            try {
                val solution = discoverer.deserialize(path, streamFactory)
                LOG.debug("\"$discoverer\" finds \"$solution\"")
                yield(solution)
                break
            } catch (ex: Exception) {
                LOG.error("Discover error", ex)
            }
        }
    }

    companion object {
        private val LOG: Logger = Logger.getInstance(SolutionDiscoverImpl::class.java.name)
    }
}