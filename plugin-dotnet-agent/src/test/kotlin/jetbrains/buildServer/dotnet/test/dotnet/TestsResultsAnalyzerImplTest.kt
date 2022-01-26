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

import jetbrains.buildServer.agent.runner.BuildOptions
import jetbrains.buildServer.dotnet.CommandResult
import jetbrains.buildServer.dotnet.TestsResultsAnalyzerImpl
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.Serializable
import java.util.*

class TestsResultsAnalyzerImplTest {
    private lateinit var _ctx: Mockery
    private lateinit var _buildOptions: BuildOptions

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _buildOptions = _ctx.mock<BuildOptions>(BuildOptions::class.java)
    }

    @DataProvider
    fun checkAnalyzeResult(): Array<Array<Serializable>> {
        return arrayOf(
                arrayOf(0, false, true, EnumSet.of(CommandResult.Success)),
                arrayOf(0, true, true, EnumSet.of(CommandResult.Success)),
                arrayOf(1, true, true, EnumSet.of(CommandResult.Success, CommandResult.FailedTests)),
                arrayOf(1, false, true, EnumSet.of(CommandResult.Fail)),
                arrayOf(99, true, true, EnumSet.of(CommandResult.Success, CommandResult.FailedTests)),
                arrayOf(99, false, true, EnumSet.of(CommandResult.Fail)),
                arrayOf(-1, true, true, EnumSet.of(CommandResult.Fail)),
                arrayOf(-1, false, true, EnumSet.of(CommandResult.Fail)),
                arrayOf(-99, true, true, EnumSet.of(CommandResult.Fail)),
                arrayOf(-99, false, true, EnumSet.of(CommandResult.Fail)),

                arrayOf(0, false, false, EnumSet.of(CommandResult.Success)),
                arrayOf(0, true, false, EnumSet.of(CommandResult.Success)),
                arrayOf(1, true, false, EnumSet.of(CommandResult.Success, CommandResult.FailedTests)),
                arrayOf(1, false, false, EnumSet.of(CommandResult.Success)),
                arrayOf(99, true, false, EnumSet.of(CommandResult.Success, CommandResult.FailedTests)),
                arrayOf(99, false, false, EnumSet.of(CommandResult.Success)),
                arrayOf(-1, true, false, EnumSet.of(CommandResult.Success)),
                arrayOf(-1, false, false, EnumSet.of(CommandResult.Success)),
                arrayOf(-99, true, false, EnumSet.of(CommandResult.Success)),
                arrayOf(-99, false, false, EnumSet.of(CommandResult.Success)))
    }

    @Test(dataProvider = "checkAnalyzeResult")
    fun shouldAnalyzeResult(exitCode: Int, hasFailedTest: Boolean, failBuildOnExitCode: Boolean, expectedResult: EnumSet<CommandResult>) {
        // Given
        val resultsAnalyzer = TestsResultsAnalyzerImpl(_buildOptions)
        _ctx.checking(object : Expectations() {
            init {
                oneOf<BuildOptions>(_buildOptions).failBuildOnExitCode
                will(returnValue(failBuildOnExitCode))
            }
        })

        // When
        val actualResult = resultsAnalyzer.analyze(exitCode, if (hasFailedTest) EnumSet.of(CommandResult.FailedTests) else EnumSet.noneOf(CommandResult::class.java))

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }
}