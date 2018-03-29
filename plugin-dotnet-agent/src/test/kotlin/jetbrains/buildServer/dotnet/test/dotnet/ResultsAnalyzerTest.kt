package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.agent.ToolProvidersRegistry
import jetbrains.buildServer.agent.runner.BuildOptions
import jetbrains.buildServer.dotnet.ResultsAnalyzer
import jetbrains.buildServer.dotnet.ResultsAnalyzerImpl
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ResultsAnalyzerTest {
    private lateinit var _ctx: Mockery
    private lateinit var _buildOptions: BuildOptions

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _buildOptions = _ctx.mock<BuildOptions>(BuildOptions::class.java)
    }

    @DataProvider
    fun checkIsSuccessData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(0, true, true),
                arrayOf(1, true, false),
                arrayOf(99, true, false),
                arrayOf(-1, true, false),
                arrayOf(-99, true, false),

                arrayOf(0, false, true),
                arrayOf(1, false, true),
                arrayOf(99, false, true),
                arrayOf(-1, false, true),
                arrayOf(-99, false, true))
    }

    @Test(dataProvider = "checkIsSuccessData")
    fun shouldCheckIsSuccess(exitCode: Int, failBuildOnExitCode: Boolean, expectedResult: Boolean) {
        // Given
        val resultsAnalyzer = ResultsAnalyzerImpl(_buildOptions)
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<BuildOptions>(_buildOptions).failBuildOnExitCode
                will(returnValue(failBuildOnExitCode))
            }
        })

        // When
        val actualResult = resultsAnalyzer.isSuccessful(CommandLineResult(sequenceOf(exitCode), emptySequence(), emptySequence()))

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }
}