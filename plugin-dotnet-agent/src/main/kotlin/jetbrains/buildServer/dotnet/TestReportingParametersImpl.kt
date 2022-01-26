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

import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.util.*

class TestReportingParametersImpl(
        private val _parametersService: ParametersService)
    : TestReportingParameters {
    override fun getMode(context: DotnetBuildContext): EnumSet<TestReportingMode> {
        val modes = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_TEST_REPORTING)?.let {
            TestReportingMode.parse(it)
        } ?: EnumSet.noneOf<TestReportingMode>(TestReportingMode::class.java)

        if (!modes.isEmpty()) {
            return modes
        }

        val modeSet = mutableSetOf(TestReportingMode.On)
        if (context.toolVersion >= Version.MultiAdapterPath_5_0_103_Version) {
            modeSet.add(TestReportingMode.MultiAdapterPath_5_0_103)
        }
        else {
            if (context.toolVersion >= Version.MultiAdapterPathVersion) {
                modeSet.add(TestReportingMode.MultiAdapterPath)
            }
        }

        return EnumSet.copyOf<TestReportingMode>(modeSet)
    }
}