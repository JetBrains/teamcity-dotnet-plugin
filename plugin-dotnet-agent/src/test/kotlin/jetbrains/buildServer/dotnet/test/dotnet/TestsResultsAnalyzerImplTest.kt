package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.PathMatcher
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CommandResult
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.TestsResultsAnalyzerImpl
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.Serializable
import java.util.*

class TestsResultsAnalyzerImplTest {
    private var _ctx: Mockery? = null
    private var _buildOptions: BuildOptions? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _buildOptions = _ctx!!.mock<BuildOptions>(BuildOptions::class.java)
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
        val resultsAnalyzer = TestsResultsAnalyzerImpl(_buildOptions!!)
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<BuildOptions>(_buildOptions).failBuildOnExitCode
                will(returnValue(failBuildOnExitCode))
            }
        })

        // When
        val actualResult = resultsAnalyzer.analyze(exitCode, if(hasFailedTest) EnumSet.of(CommandResult.FailedTests) else EnumSet.noneOf(CommandResult::class.java))

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }
}