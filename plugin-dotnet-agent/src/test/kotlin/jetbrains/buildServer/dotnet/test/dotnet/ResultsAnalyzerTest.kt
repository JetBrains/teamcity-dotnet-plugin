package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.agent.ToolProvidersRegistry
import jetbrains.buildServer.agent.runner.BuildOptions
import jetbrains.buildServer.dotnet.CommandResult
import jetbrains.buildServer.dotnet.ResultsAnalyzer
import jetbrains.buildServer.dotnet.ResultsAnalyzerImpl
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.Serializable
import java.util.*

class ResultsAnalyzerTest {
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
                arrayOf(0, true, EnumSet.of(CommandResult.Success)),
                arrayOf(1, true, EnumSet.of(CommandResult.Fail)),
                arrayOf(99, true, EnumSet.of(CommandResult.Fail)),
                arrayOf(-1, true, EnumSet.of(CommandResult.Fail)),
                arrayOf(-99, true, EnumSet.of(CommandResult.Fail)),

                arrayOf(0, false, EnumSet.of(CommandResult.Success)),
                arrayOf(1, false, EnumSet.of(CommandResult.Success)),
                arrayOf(99, false, EnumSet.of(CommandResult.Success)),
                arrayOf(-1, false, EnumSet.of(CommandResult.Success)),
                arrayOf(-99, false, EnumSet.of(CommandResult.Success)))
    }

    @Test(dataProvider = "checkAnalyzeResult")
    fun shouldAnalyzeResult(exitCode: Int, failBuildOnExitCode: Boolean, expectedResult: EnumSet<CommandResult>) {
        // Given
        val resultsAnalyzer = ResultsAnalyzerImpl(_buildOptions)
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<BuildOptions>(_buildOptions).failBuildOnExitCode
                will(returnValue(failBuildOnExitCode))
            }
        })

        // When
        val actualResult = resultsAnalyzer.analyze(CommandLineResult(sequenceOf(exitCode), emptySequence(), emptySequence()))

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }
}