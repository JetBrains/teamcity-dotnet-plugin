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
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.disposableOf
import jetbrains.buildServer.rx.emptyDisposable
import jetbrains.buildServer.agent.Logger
import java.io.Closeable
import java.io.File
import java.io.OutputStreamWriter

class VSTestLoggerEnvironmentBuilder(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _loggerResolver: LoggerResolver,
        private val _loggerService: LoggerService,
        private val _testReportingParameters: TestReportingParameters,
        private val _environmentCleaner: EnvironmentCleaner,
        private val _environmentAnalyzer: VSTestLoggerEnvironmentAnalyzer)
    : EnvironmentBuilder {
    override fun build(context: DotnetBuildContext): Disposable {
        val testReportingMode = _testReportingParameters.getMode(context)
        LOG.debug("Test reporting mode: $testReportingMode")

        if (testReportingMode.contains(TestReportingMode.Off)) {
            return emptyDisposable()
        }

        if (testReportingMode.contains(TestReportingMode.MultiAdapterPath)) {
            return emptyDisposable()
        }

        val targets = context.command.targetArguments.flatMap { it.arguments }.map { File(it.value) }.toList()
        LOG.debug("Targets: ${targets.joinToString (", "){ it.name }}")
        val checkoutDirectory = _pathsService.getPath(PathType.Checkout)
        LOG.debug("Checkout directory: $checkoutDirectory")
        val loggerDirectory = File(checkoutDirectory, "$directoryPrefix${_pathsService.uniqueName}")
        LOG.debug("Logger directory: $loggerDirectory")

        LOG.debug("Clean ...")
        _environmentCleaner.clean()
        LOG.debug("Analyze ...")
        _environmentAnalyzer.analyze(targets)

        return _loggerResolver.resolve(ToolType.VSTest).parentFile?.absoluteFile?.let {
            try {
                LOG.debug("Copy logger to \"$loggerDirectory\" from \"$it\"")
                _fileSystemService.copy(it, loggerDirectory)
                LOG.debug("Create \"$readmeFileName\" file in the directory \"$loggerDirectory\"")
                _fileSystemService.write(File(loggerDirectory, readmeFileName)) {
                    OutputStreamWriter(it).use {
                        it.write(readmeFileContent)
                    }
                }
            } catch (ex: Exception) {
                LOG.error(ex)
                _loggerService.writeErrorOutput("Failed to create logger directory \"$loggerDirectory\"")
            }

            return disposableOf { _fileSystemService.remove(loggerDirectory) }
        } ?: emptyDisposable()
    }

    companion object {
        private val LOG = Logger.getLogger(VSTestLoggerEnvironmentBuilder::class.java)
        internal const val directoryPrefix = "teamcity.logger."
        internal const val readmeFileName = "readme.txt"
        internal const val readmeFileContent = "This directory is created by TeamCity agent.\nIt contains files necessary for real-time tests reporting.\nThe directory will be removed automatically."
        private val EmptyClosable = Closeable { }
    }
}