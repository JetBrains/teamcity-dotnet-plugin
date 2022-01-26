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

package jetbrains.buildServer.dotnet.test.agent.runner

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class LoggerServiceTest {
    @MockK private lateinit var _buildStepContext: BuildStepContext
    @MockK private lateinit var _buildInfo: BuildInfo
    @MockK private lateinit var _colorTheme: ColorTheme
    @MockK private lateinit var _buildProgressLogger: BuildProgressLogger

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _buildStepContext.runnerContext } returns
            mockk<BuildRunnerContext>() {
                every { build } returns
                        mockk<AgentRunningBuild>() {
                            every { buildLogger } returns _buildProgressLogger
                        }
            }

        every { _colorTheme.getAnsiColor(Color.Header) } returns "#Header#"
        every { _colorTheme.getAnsiColor(Color.Default) } returns "#Default#"
        every { _colorTheme.getAnsiColor(Color.Success) } returns "#Success#"
    }

    @DataProvider
    fun testWriteStandardOutput(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(listOf(StdOutText("")), listOf("")),
                arrayOf(listOf(StdOutText("text")), listOf("text")),
                arrayOf(listOf(StdOutText("text"), StdOutText("abc", Color.Header)), listOf("text\u001B[#Header#mabc")),
                arrayOf(listOf(StdOutText("abc", Color.Header), StdOutText("text")), listOf("\u001B[#Header#mabc\u001B[0mtext")),
                arrayOf(listOf(StdOutText("abc", Color.Header), StdOutText("text"), StdOutText(" xyz")), listOf("\u001B[#Header#mabc\u001B[0mtext xyz")),
                arrayOf(listOf(StdOutText("text", Color.Header)), listOf("\u001B[#Header#mtext")),
                arrayOf(listOf(StdOutText("text", Color.Header), StdOutText(" abc", Color.Success)), listOf("\u001B[#Header#mtext\u001B[#Success#m abc")),
                arrayOf(listOf(StdOutText("text", Color.Header), StdOutText(" abc", Color.Header)), listOf("\u001B[#Header#mtext abc")),
                arrayOf(listOf(StdOutText("text", Color.Header), StdOutText(" abc", Color.Header), StdOutText(" xyz")), listOf("\u001B[#Header#mtext abc\u001B[0m xyz")))
    }

    @Test(dataProvider = "testWriteStandardOutput")
    fun shouldWriteStandardOutput(output: List<StdOutText>, expectedBuildLog: List<StdOutText>) {
        // Given
        val actualBuildLog = mutableListOf<String>()
        var logger = createInstance()
        every { _buildProgressLogger.message(any()) } answers { actualBuildLog.add(arg(0)) }

        // When
        logger.writeStandardOutput(*output.toTypedArray())

        // Then
        Assert.assertEquals(actualBuildLog, expectedBuildLog)
    }

    @Test
    fun shouldWriteBuildProblem() {
        // Given
        val actualBuildLog = mutableListOf<String>()
        var logger = createInstance()
        every { _buildInfo.id } returns "runnerId"
        every { _buildInfo.name } returns "MyStepName"
        every { _buildProgressLogger.logBuildProblem(any()) } returns Unit

        // When
        logger.writeBuildProblem("myId", "myType", "myDescription");

        // Then
        verify { _buildProgressLogger.logBuildProblem(BuildProblemData.createBuildProblem("runnerId:myId", "myType", "myDescription (Step: MyStepName)")) }
    }

    private fun createInstance() =
            LoggerServiceImpl(_buildStepContext, _buildInfo, _colorTheme)
}