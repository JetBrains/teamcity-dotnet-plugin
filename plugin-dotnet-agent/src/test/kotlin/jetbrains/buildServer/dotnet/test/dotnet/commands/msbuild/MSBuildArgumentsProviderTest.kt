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

package jetbrains.buildServer.dotnet.test.dotnet.commands.msbuild

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildArgumentsProvider
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameter
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameterConverter
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParametersProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MSBuildArgumentsProviderTest {
    private lateinit var _context: DotnetCommandContext
    @MockK private lateinit var _msBuildParameterConverter: MSBuildParameterConverter
    @MockK private lateinit var _msBuildParametersProvider: MSBuildParametersProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        _context = DotnetCommandContext(ToolPath(Path("wd")), mockk<DotnetCommand>())
    }

    @DataProvider
    fun argumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(listOf("/p:param=value"))
        )
    }

    @Test(dataProvider = "argumentsData")
    fun shouldGetArguments(expectedArguments: List<String>) {
        // Given
        val msBuildParameter = MSBuildParameter("Param1", "Value1")
        every { _msBuildParametersProvider.getParameters(_context) } returns sequenceOf(msBuildParameter)
        every { _msBuildParameterConverter.convert(match { it.toList().equals(listOf(msBuildParameter)) }) } returns sequenceOf("/p:param=value")
        val argumentsProvider = MSBuildArgumentsProvider(_msBuildParameterConverter, listOf(_msBuildParametersProvider))

        // When
        val actualArguments = argumentsProvider.getArguments(_context).map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}