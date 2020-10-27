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

package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetCommonArgumentsProviderTest {
    @MockK private lateinit var _msBuildParametersProvider: MSBuildParametersProvider
    @MockK private lateinit var _msBuildParameterConverter: MSBuildParameterConverter

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(emptyMap<String, String>(), listOf("rspArg", "customArg")),
                arrayOf(mapOf(DotnetConstants.PARAM_RSP to "true"), listOf("rspArg", "customArg")),
                arrayOf(mapOf(DotnetConstants.PARAM_RSP to "false"), listOf("l:/logger", "/p:param=value", "/nodeReuse:false", "customArg")))
    }

    @Test(dataProvider = "testData")
    fun shouldGetArguments(parameters: Map<String, String>, expectedArguments: List<String>) {
        // Given
        val context = DotnetBuildContext(ToolPath(Path("wd")), mockk<DotnetCommand>())
        val msBuildParameter = MSBuildParameter("Param1", "Value1")
        every { _msBuildParametersProvider.getParameters(context) } returns sequenceOf(msBuildParameter)
        every { _msBuildParameterConverter.convert(match { it.toList().equals(listOf(msBuildParameter)) }, true) } returns sequenceOf("/p:param=value")

        val argumentsProvider = DotnetCommonArgumentsProviderImpl(
                ParametersServiceStub(parameters),
                ArgumentsProviderStub(sequenceOf(CommandLineArgument("rspArg"))),
                ArgumentsProviderStub(sequenceOf(CommandLineArgument("customArg"))),
                _msBuildParametersProvider,
                ArgumentsProviderStub(sequenceOf(CommandLineArgument("l:/logger"))),
                ArgumentsProviderStub(sequenceOf(CommandLineArgument("/nodeReuse:false"))),
                _msBuildParameterConverter)

        // When
        val actualArguments = argumentsProvider.getArguments(context).map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}