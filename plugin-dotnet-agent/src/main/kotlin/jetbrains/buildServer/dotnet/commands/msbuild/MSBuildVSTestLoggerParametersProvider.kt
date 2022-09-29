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

package jetbrains.buildServer.dotnet.commands.msbuild

import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.test.TestReportingParameters
import jetbrains.buildServer.dotnet.logging.LoggerParameters
import jetbrains.buildServer.dotnet.logging.LoggerResolver
import java.util.*

class MSBuildVSTestLoggerParametersProvider(
    private val _pathsService: PathsService,
    private val _loggerResolver: LoggerResolver,
    private val _testReportingParameters: TestReportingParameters,
    private val _loggerParameters: LoggerParameters,
    private val _virtualContext: VirtualContext)
    : MSBuildParametersProvider {

    override fun getParameters(context: DotnetBuildContext): Sequence<MSBuildParameter> = sequence {
        val testReportingMode = _testReportingParameters.getMode(context)
        if (testReportingMode.contains(TestReportingMode.Off)) {
            return@sequence
        }

        _loggerResolver.resolve(ToolType.VSTest).parentFile?.let {
            yield(MSBuildParameter("VSTestLogger", "logger://teamcity", MSBuildParameterType.Predefined))
            var paths = _virtualContext.resolvePath(_pathsService.getPath(PathType.Checkout).canonicalPath)
            if (testReportingMode.contains(TestReportingMode.MultiAdapterPath_5_0_103)) {
                paths = ".;${_virtualContext.resolvePath(it.canonicalPath)}"
            } else {
                if (testReportingMode.contains(TestReportingMode.MultiAdapterPath)) {
                    paths = "${_virtualContext.resolvePath(it.canonicalPath)};."
                }
            }

            yield(MSBuildParameter("VSTestTestAdapterPath", paths, MSBuildParameterType.Predefined))
        }

        yield(MSBuildParameter("VSTestVerbosity", _loggerParameters.vsTestVerbosity.id.lowercase(Locale.getDefault()), MSBuildParameterType.Predefined))
    }
}