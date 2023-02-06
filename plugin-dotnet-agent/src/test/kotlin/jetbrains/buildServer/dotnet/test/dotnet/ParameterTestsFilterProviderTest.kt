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

package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.ParameterTestsFilterProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ParameterTestsFilterProviderTest {
    @MockK private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider(name = "testData")
    fun testData(): Any? {
        return arrayOf(
                arrayOf("Abc", "Abc"),
                arrayOf("\"Abc\"", "Abc"),
                arrayOf("\"  \"", ""),
                arrayOf("  ", ""),
                arrayOf("", ""),
                arrayOf(null, "")
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideFilter(filterParamValue: String?, expecedFilter: String) {
        // Given
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_TEST_CASE_FILTER) } returns filterParamValue
        val provider = createInstance()

        // When
        val actulFilter = provider.filterExpression;

        // Then
        Assert.assertEquals(actulFilter, expecedFilter)
    }

    private fun createInstance() = ParameterTestsFilterProvider(_parametersService)
}