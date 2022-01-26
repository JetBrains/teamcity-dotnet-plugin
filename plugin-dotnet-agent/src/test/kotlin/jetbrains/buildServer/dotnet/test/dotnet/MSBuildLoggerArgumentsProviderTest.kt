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

package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildLoggerArgumentsProviderTest {
    @MockK private lateinit var _loggerParameters: LoggerParameters
    @MockK private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0)}
    }

    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf(
                        File("logger.dll") as File?,
                        null,
                        listOf(
                                "/noconsolelogger",
                                "\"/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,v_${File("logger.dll").absolutePath};TeamCity;additionalParam;params\"")),
                arrayOf(
                        File("logger.dll") as File?,
                        Verbosity.Normal,
                        listOf(
                                "/noconsolelogger",
                                "\"/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,v_${File("logger.dll").absolutePath};TeamCity;verbosity=normal;additionalParam;params\"")),
                arrayOf(
                        File("logger.dll") as File?,
                        Verbosity.Quiet,
                        listOf(
                                "/noconsolelogger",
                                "\"/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,v_${File("logger.dll").absolutePath};TeamCity;verbosity=quiet;additionalParam;params\"")),
                arrayOf(
                        File("logger.dll") as File?,
                        Verbosity.Minimal,
                        listOf(
                                "/noconsolelogger",
                                "\"/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,v_${File("logger.dll").absolutePath};TeamCity;verbosity=minimal;additionalParam;params\"")))
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            loggerFile: File,
            verbosity: Verbosity?,
            expectedArguments: List<String>) {
        // Given
        val context = DotnetBuildContext(ToolPath(Path("wd")), mockk<DotnetCommand>())
        val argumentsProvider = MSBuildLoggerArgumentsProvider(LoggerResolverStub(loggerFile, File("vstestlogger")),_loggerParameters, _virtualContext)
        every { _loggerParameters.msBuildLoggerVerbosity } returns verbosity
        every { _loggerParameters.msBuildParameters } returns "params"
        every { _loggerParameters.getAdditionalLoggerParameters(context) } returns sequenceOf("additionalParam")

        // When
        val actualArguments = argumentsProvider.getArguments(context).map { it.value }.toList()

        // Then
        verify { _virtualContext.resolvePath(any()) }
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}