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

package jetbrains.buildServer.dotnet.commands.test.runSettings

import jetbrains.buildServer.Serializer
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsFileProvider
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsProvider
import org.w3c.dom.Document

class TestRunSettingsFileNameProviderGenerated(
    private val _settingsProvider: TestRunSettingsProvider,
    private val _pathsService: PathsService,
    private val _fileSystem: FileSystemService,
    private val _serializer: Serializer<Document>,
) : TestRunSettingsFileProvider {
    override fun tryGet(context: DotnetCommandContext) =
        try {
            _settingsProvider.tryCreate(context)
                ?.let { settings ->
                    _pathsService.getTempFileName(RunSettingsFileExtension)
                        .let { runSettingsFile ->
                            _fileSystem.write(runSettingsFile) { stream ->
                                _serializer.serialize(settings, stream)
                                runSettingsFile
                            }
                        }
                }
        }
        catch (error: Throwable) {
            LOG.error(error)
            null
        }

    companion object {
        private val LOG = Logger.getLogger(TestRunSettingsFileNameProviderGenerated::class.java)
        internal val RunSettingsFileExtension = ".runsettings"
    }
}