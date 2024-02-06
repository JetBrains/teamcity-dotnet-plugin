package jetbrains.buildServer.dotnet.test.dotnet.commands.test

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jetbrains.buildServer.ExtensionsProvider
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.DataProcessorContext
import jetbrains.buildServer.agent.impl.dataImport.AsyncDataProcessingTask
import jetbrains.buildServer.agent.impl.dataImport.AsyncDataProcessor
import jetbrains.buildServer.agent.impl.serviceProcess.ServiceProcessManager
import jetbrains.buildServer.agent.impl.serviceProcess.ServiceProcessScope
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Verbosity
import jetbrains.buildServer.dotnet.commands.test.TestCommandEnvironmentBuilder
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

internal class TestCommandEnvironmentBuilderTest {
    private lateinit var _processor: AsyncDataProcessor
    private lateinit var _extensionsProvider: ExtensionsProvider
    private lateinit var _buildStepContext: BuildStepContext
    private lateinit var _serviceProcessManager: ServiceProcessManager
    private lateinit var _parametersService: ParametersService
    private lateinit var _pathsService: PathsService
    private lateinit var _context: DotnetCommandContext

    @BeforeMethod
    fun initMocks() {
        _processor = mockk<AsyncDataProcessor>(relaxed = true) {
            every { type } returns "streamToBuildLog"
        }
        _extensionsProvider = mockk<ExtensionsProvider>(relaxed = true) {
            every { getExtensions(AsyncDataProcessor::class.java) } returns listOf(_processor)
        }
        _buildStepContext = mockk<BuildStepContext>(relaxed = true)
        _serviceProcessManager = mockk<ServiceProcessManager>(relaxed = true)
        _parametersService = mockk<ParametersService>(relaxed = true)
        _pathsService = mockk<PathsService>(relaxed = true)
        _context = mockk<DotnetCommandContext>(relaxed = true)
    }

    @Test
    fun `should return fallback to stdout variable if fallback is enabled in settings`() {
        // Arrange
        every { _parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns "true"
        val environmentBuilder = TestCommandEnvironmentBuilder(_extensionsProvider, _buildStepContext, _serviceProcessManager, _parametersService, _pathsService)

        // Act
        val environmentBuildResult = environmentBuilder.build(_context)

        // Assert
        val variables = environmentBuildResult.variables.toList()
        Assert.assertEquals(variables.size, 1)
        Assert.assertEquals(variables.first().name, "TEAMCITY_FALLBACK_TO_STDOUT_TEST_REPORTING")
        Assert.assertEquals(variables.first().value, "true")
    }

    @Test
    fun `should return test reports path variable if fallback is not enabled in settings`() {
        // Arrange
        val agentTempDirectory = File("/agentTmp")
        every { _parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns "false"
        every { _pathsService.getPath(PathType.AgentTemp) } returns agentTempDirectory
        val environmentBuilder = TestCommandEnvironmentBuilder(_extensionsProvider, _buildStepContext, _serviceProcessManager, _parametersService, _pathsService)

        // Act
        val environmentBuildResult = environmentBuilder.build(_context)

        // Assert
        val variables = environmentBuildResult.variables.toList()
        Assert.assertEquals(variables.size, 1)
        Assert.assertEquals(variables.first().name, "TEAMCITY_TEST_REPORT_FILES_PATH")
        Assert.assertTrue(variables.first().value.startsWith(agentTempDirectory.absolutePath.toString()), "actual path is ${variables.first().value}")
    }

    @Test
    fun `should start processing with the file streaming processor if fallback is not enabled in settings`() {
        // Arrange
        every { _parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns "false"

        val environmentBuilder = TestCommandEnvironmentBuilder(_extensionsProvider, _buildStepContext, _serviceProcessManager, _parametersService, _pathsService)

        // Act
        environmentBuilder.build(_context)

        // Assert
        verify { _processor.startDataProcessing(any()) }
    }

    @Test
    fun `should register started processing in service process manager, scoped to build step`() {
        // Arrange
        every { _parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns "false"

        val asyncDataProcessingTask = mockk<AsyncDataProcessingTask>(relaxed = true)
        every { _processor.startDataProcessing(any()) } returns asyncDataProcessingTask

        val environmentBuilder = TestCommandEnvironmentBuilder(_extensionsProvider, _buildStepContext, _serviceProcessManager, _parametersService, _pathsService)

        // Act
        environmentBuilder.build(_context)

        // Assert
        val runnableSlot = slot<Runnable>()
        verify { _serviceProcessManager.registerRunningProcess(any(), capture(runnableSlot), ServiceProcessScope.BUILD_STEP) }
        val shutdownRunnable = runnableSlot.captured
        shutdownRunnable.run()
        verify { asyncDataProcessingTask.shutdownGracefully() }
    }

    @Test
    fun `disposing of the build environment should call graceful shutdown of the started processing`() {
        // Arrange
        every { _parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns "false"

        val asyncDataProcessingTask = mockk<AsyncDataProcessingTask>(relaxed = true)
        every { _processor.startDataProcessing(any()) } returns asyncDataProcessingTask

        val environmentBuilder = TestCommandEnvironmentBuilder(_extensionsProvider, _buildStepContext, _serviceProcessManager, _parametersService, _pathsService)

        // Act
        val environmentBuildResult = environmentBuilder.build(_context)
        environmentBuildResult.dispose()

        // Assert
        verify { asyncDataProcessingTask.shutdownGracefully() }
    }

    @Test
    fun `should throw exception if processor was not found when setting up file-based test reporting`() {
        // Arrange
        every { _parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns "false"
        every { _extensionsProvider.getExtensions(AsyncDataProcessor::class.java) } returns listOf()
        val environmentBuilder = TestCommandEnvironmentBuilder(_extensionsProvider, _buildStepContext, _serviceProcessManager, _parametersService, _pathsService)

        // Act, Assert
        Assert.assertThrows(Exception::class.java) {
            environmentBuilder.build(_context)
        }
    }

    @Test
    fun `should start file streaming with expected arguments`() {
        // Arrange
        every { _parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns "false"

        val build = mockk<AgentRunningBuild>(relaxed = true)
        val runnerContext = mockk<BuildRunnerContext>(relaxed = true) { every { this@mockk.build } returns build }
        every { _buildStepContext.runnerContext } returns runnerContext

        val environmentBuilder = TestCommandEnvironmentBuilder(_extensionsProvider, _buildStepContext, _serviceProcessManager, _parametersService, _pathsService)

        // Act
        environmentBuilder.build(_context)
        val contextSlot = slot<DataProcessorContext>()
        verify { _processor.startDataProcessing(capture(contextSlot)) }
        val dataProcessorContext = contextSlot.captured

        // Assert
        Assert.assertEquals(dataProcessorContext.build, build)
        val arguments = dataProcessorContext.arguments
        Assert.assertEquals(arguments.size, 3)
        Assert.assertEquals(arguments["wrapFileContentInBlock"], "false")
    }

    @Test
    fun `should start file streaming with file pattern that matches environment variable with msg file mask`() {
        // Arrange
        every { _parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns "false"

        val environmentBuilder = TestCommandEnvironmentBuilder(_extensionsProvider, _buildStepContext, _serviceProcessManager, _parametersService, _pathsService)

        // Act
        val environmentBuildResult = environmentBuilder.build(_context)
        val contextSlot = slot<DataProcessorContext>()
        verify { _processor.startDataProcessing(capture(contextSlot)) }
        val dataProcessorContext = contextSlot.captured

        // Assert
        val arguments = dataProcessorContext.arguments
        val variableValue = environmentBuildResult.variables.first { it.name == "TEAMCITY_TEST_REPORT_FILES_PATH" }.value
        val expectedPattern = variableValue + File.separator + "*.msg"
        Assert.assertEquals(arguments["filePattern"], expectedPattern)
    }

    @DataProvider(name = "streaming processor quiet mode arguments")
    fun loggingVerbosityTestsProvider() = arrayOf(
        arrayOf(null, "true"),
        arrayOf(Verbosity.Quiet, "true"),
        arrayOf(Verbosity.Minimal, "true"),
        arrayOf(Verbosity.Normal, "true"),
        arrayOf(Verbosity.Detailed, "false"),
        arrayOf(Verbosity.Diagnostic, "false")
    )

    @Test(dataProvider = "streaming processor quiet mode arguments")
    fun `should set quiet mode argument to true if logging verbosity is lower than detailed`(verbosity: Verbosity?, argumentValue: String) {
        // Arrange
        every { _parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns "false"

        every { _context.verbosityLevel } returns verbosity

        val environmentBuilder = TestCommandEnvironmentBuilder(_extensionsProvider, _buildStepContext, _serviceProcessManager, _parametersService, _pathsService)

        // Act
        environmentBuilder.build(_context)
        val contextSlot = slot<DataProcessorContext>()
        verify { _processor.startDataProcessing(capture(contextSlot)) }
        val dataProcessorContext = contextSlot.captured

        // Assert
        val arguments = dataProcessorContext.arguments
        Assert.assertEquals(arguments["quiet"], argumentValue)
    }
}