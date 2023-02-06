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

package jetbrains.buildServer.dotnet.test.dotnet.commands.test

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.test.TestReportingParametersImpl
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.util.*

class TestReportingParametersTest {
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _dotnetCommand: DotnetCommand

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(null, Version(5, 0, 104), mutableSetOf(TestReportingMode.On, TestReportingMode.MultiAdapterPath_5_0_103)),
                arrayOf(null, Version(5, 0, 103), mutableSetOf(TestReportingMode.On, TestReportingMode.MultiAdapterPath_5_0_103)),
                arrayOf(null, Version(5, 0, 102), mutableSetOf(TestReportingMode.On, TestReportingMode.MultiAdapterPath)),
                arrayOf(null, Version(3, 1, 100), mutableSetOf(TestReportingMode.On, TestReportingMode.MultiAdapterPath)),
                arrayOf(null, Version(2, 1, 102), mutableSetOf(TestReportingMode.On, TestReportingMode.MultiAdapterPath)),
                arrayOf(null, Version(2, 1, 101), mutableSetOf(TestReportingMode.On)),
                arrayOf(null, Version(1, 1, 100), mutableSetOf(TestReportingMode.On)),
                arrayOf("Off", Version(5, 0, 103), mutableSetOf(TestReportingMode.Off)),
                arrayOf("On", Version(5, 0, 103), mutableSetOf(TestReportingMode.On)),
                arrayOf("MultiAdapterPath", Version(5, 0, 103), mutableSetOf(TestReportingMode.MultiAdapterPath)),
                arrayOf("MultiAdapterPath_5_0_103", Version(2, 1, 101), mutableSetOf(TestReportingMode.MultiAdapterPath_5_0_103))
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideMode(
            reportigParam: String?,
            toolVersion: Version,
            expectedMode: Set<TestReportingMode>) {
        // Given
        val testReportingParameters = TestReportingParametersImpl(_parametersService)

        // When
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_TEST_REPORTING) } returns reportigParam
        val actualMode = testReportingParameters.getMode(DotnetBuildContext(ToolPath(Path("wd"), Path("v_wd")), _dotnetCommand, toolVersion))

        // Then
        Assert.assertEquals(actualMode, EnumSet.copyOf<TestReportingMode>(expectedMode))
    }
}