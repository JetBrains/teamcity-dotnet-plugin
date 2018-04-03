package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.util.*

class DotnetWorkflowAnalyzerTest {
    private lateinit var _ctx: Mockery
    private lateinit var _loggerService: LoggerService

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _loggerService = _ctx.mock<LoggerService>(LoggerService::class.java)
    }

    @Test
    fun shouldLogErrorWhenStepHasFailedTests() {
        // Given
        val instance = createInstance()
        val context = DotnetWorkflowAnalyzerContext()

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<LoggerService>(_loggerService).onErrorOutput("Process finished with positive exit code 99 (some tests have failed). Reporting step success as all the tests have run. Use \"at least one test failed\" failure condition to fail the build.")
            }
        })

        instance.registerResult(context, EnumSet.of(CommandResult.Success, CommandResult.FailedTests), 99)

        // Then
        _ctx.assertIsSatisfied()
    }

    @Test
    fun shouldCreateBuildProblemWhenStepFailed() {
        // Given
        val instance = createInstance()
        val context = DotnetWorkflowAnalyzerContext()

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<LoggerService>(_loggerService).onBuildProblem(BuildProblemData.createBuildProblem("dotnet_exit_code-99", BuildProblemData.TC_EXIT_CODE_TYPE, "Process exited with code -99"))
            }
        })

        instance.registerResult(context, EnumSet.of(CommandResult.Fail), -99)

        // Then
        _ctx.assertIsSatisfied()
    }

    @Test
    fun shouldNotProduceAnyLogsWhenSuccess() {
        // Given
        val instance = createInstance()
        val context = DotnetWorkflowAnalyzerContext()

        // When
        instance.registerResult(context, EnumSet.of(CommandResult.Success), 0)

        // Then
        _ctx.assertIsSatisfied()
    }

    @Test
    fun shouldSummarize() {
        // Given
        val instance = createInstance()
        val context = DotnetWorkflowAnalyzerContext()

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<LoggerService>(_loggerService).onErrorOutput("Process finished with positive exit code 99 (some tests have failed). Reporting step success as all the tests have run. Use \"at least one test failed\" failure condition to fail the build.")
                oneOf<LoggerService>(_loggerService).onErrorOutput("Some of processes finished with positive exit code (some tests have failed). Reporting step success as all the tests have run. Use \"at least one test failed\" failure condition to fail the build.")
            }
        })

        instance.registerResult(context, EnumSet.of(CommandResult.Success, CommandResult.FailedTests), 99)
        instance.registerResult(context, EnumSet.of(CommandResult.Success), 0)
        instance.summarize(context)

        // Then
        _ctx.assertIsSatisfied()
    }

    @Test
    fun shouldNotSummarizeWhenLastResultIsFailedTests() {
        // Given
        val instance = createInstance()
        val context = DotnetWorkflowAnalyzerContext()

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<LoggerService>(_loggerService).onErrorOutput("Process finished with positive exit code 99 (some tests have failed). Reporting step success as all the tests have run. Use \"at least one test failed\" failure condition to fail the build.")
                oneOf<LoggerService>(_loggerService).onErrorOutput("Process finished with positive exit code 33 (some tests have failed). Reporting step success as all the tests have run. Use \"at least one test failed\" failure condition to fail the build.")
            }
        })

        instance.registerResult(context, EnumSet.of(CommandResult.Success, CommandResult.FailedTests), 99)
        instance.registerResult(context, EnumSet.of(CommandResult.Success, CommandResult.FailedTests), 33)
        instance.summarize(context)

        // Then
        _ctx.assertIsSatisfied()
    }

    @Test
    fun shouldNotSummarizeWhenSingleCommand() {
        // Given
        val instance = createInstance()
        val context = DotnetWorkflowAnalyzerContext()

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<LoggerService>(_loggerService).onErrorOutput("Process finished with positive exit code 99 (some tests have failed). Reporting step success as all the tests have run. Use \"at least one test failed\" failure condition to fail the build.")
            }
        })

        instance.registerResult(context, EnumSet.of(CommandResult.Success, CommandResult.FailedTests), 99)
        instance.summarize(context)

        // Then
        _ctx.assertIsSatisfied()
    }

    private fun createInstance(): DotnetWorkflowAnalyzer {
        return DotnetWorkflowAnalyzerImpl(_loggerService)
    }
}