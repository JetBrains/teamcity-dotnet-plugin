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

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.Logger

class DotnetVersionProviderImpl(
        private val _buildStepContext: BuildStepContext,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _versionParser: VersionParser,
        private val _fileSystemService: FileSystemService,
        private val _dotnetToolResolver: DotnetToolResolver)
    : DotnetVersionProvider {

    override fun getVersion(dotnetExecutable: Path, workingDirectory: Path): Version {
        LOG.debug("Try getting the dotnet CLI version for directory \"$workingDirectory\".")
        val versionResult = _commandLineExecutor.tryExecute(
                CommandLine(
                        null,
                        TargetType.Tool,
                        dotnetExecutable,
                        workingDirectory,
                        versionArgs,
                        emptyList()))

        if (versionResult == null || versionResult.exitCode !=0 || versionResult.errorOutput.filter { it.isNotBlank() }.any()) {
            LOG.warn("The error occurred getting the dotnet CLI version.")
            return Version.Empty
        }
        else {
            val version = _versionParser.parse(versionResult.standardOutput)
            if (version == Version.Empty) {
                LOG.warn("The error occurred parsing the dotnet CLI version.")
            }

            return version
        }
    }


    companion object {
        private val LOG = Logger.getLogger(DotnetVersionProviderImpl::class.java)
        internal val versionArgs = listOf(CommandLineArgument("--version"))
    }
}