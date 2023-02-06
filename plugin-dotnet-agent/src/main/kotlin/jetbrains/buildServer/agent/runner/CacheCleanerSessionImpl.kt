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

import jetbrains.buildServer.agent.DirectoryCleanersProvider
import jetbrains.buildServer.agent.DirectoryCleanersProviderContext
import jetbrains.buildServer.agent.DirectoryCleanersRegistry
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.DotnetConstants
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.*

class CacheCleanerSessionImpl(
        private val _cleaners: List<CacheCleaner>)
    : DirectoryCleanersProvider {

    override fun getCleanerName() = DotnetConstants.CLEANER_NAME

    override fun registerDirectoryCleaners(context: DirectoryCleanersProviderContext, registry: DirectoryCleanersRegistry) {
        val now = Instant.now()
        val targets = mutableSetOf<File>()
        for (cleaner in _cleaners) {
            val fullName = "${DotnetConstants.CLEANER_NAME} for ${cleaner.name}"
            val date = Date.from(now.minus(Duration.ofDays(cleaner.type.weight)))
            LOG.info("Register $fullName.")
            for (target in cleaner.targets) {
                if (targets.add(target)) {
                    registry.addCleaner(
                            target,
                            date,
                            Runnable {
                                LOG.info("Cleaning \"$target\" by $fullName.")
                                try {
                                    if (cleaner.clean(target)) {
                                        LOG.info("Path \"$target\" has been cleaned by $fullName.")
                                    } else {
                                        LOG.warn("Path \"$target\" has not been cleaned by $fullName.")                                    }
                                } catch (error: Throwable) {
                                    LOG.error("Path \"$target\" has not been cleaned by $fullName.", error)
                                }
                            })

                    LOG.info("Path \"$target\" is added by $fullName.")
                } else {
                    LOG.info("Path \"$target\" is already added.")
                }
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(CacheCleanerSessionImpl::class.java)
    }
}