package jetbrains.buildServer.dotnet.test.dotnet.commands.test

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.impl.AgentEventDispatcher
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.BuildStepScopedTestEnvironmentBuilderImpl
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File
import java.util.regex.Pattern

class BuildStepScopedTestEnvironmentBuilderImplTest {
    @Test
    fun `should not set up environment for test reporting if fall back to stdout test reporting is enabled`() {
        // Arrange
        val agentEventDispatcher = mockk<AgentEventDispatcher>(relaxed = true)
        val parametersService = mockk<ParametersService>()
        every { parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns "true"
        val pathsService = mockk<PathsService>()
        val loggerService = mockk<LoggerService>(relaxed = true)
        val environmentBuilder = BuildStepScopedTestEnvironmentBuilderImpl(agentEventDispatcher, parametersService, pathsService, loggerService)

        // Act
        environmentBuilder.setupEnvironmentForTestReporting()

        // Assert
        verify(exactly = 0) {
            loggerService.writeMessage(any())
        }
    }

    @Test
    fun `should set up environment for test reporting if environment was not set up for current build step`() {
        // Arrange
        val agentEventDispatcher = mockk<AgentEventDispatcher>(relaxed = true)
        val parametersService = mockk<ParametersService>()
        every { parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns null
        val pathsService = mockk<PathsService>()
        every { pathsService.getPath(PathType.AgentTemp) } returns File("/agentTmp")
        val loggerService = mockk<LoggerService>(relaxed = true)
        val environmentBuilder = BuildStepScopedTestEnvironmentBuilderImpl(agentEventDispatcher, parametersService, pathsService, loggerService)

        // Act
        environmentBuilder.setupEnvironmentForTestReporting()

        // Assert
        verify {
            loggerService.writeMessage(match {
                it.messageName == "importData" &&
                        it.attributes["type"] == "streamToBuildLog" &&
                        it.attributes["wrapFileContentInBlock"] == "false" &&
                        it.attributes["filePattern"] != null &&
                        // e.g. /agentTmp/TestReports/QQGkU6voRLudOukHGLgfgg==/*.msg
                        Pattern.matches("""[/\\]agentTmp[/\\]TestReports[/\\].{24}[/\\]\*\.msg""", it.attributes["filePattern"])
            })
        }
    }

    @Test
    fun `should not set up environment for test reporting if environment was already set up for current build step`() {
        // Arrange
        val agentEventDispatcher = mockk<AgentEventDispatcher>(relaxed = true)
        val parametersService = mockk<ParametersService>()
        every { parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns null
        val pathsService = mockk<PathsService>()
        every { pathsService.getPath(PathType.AgentTemp) } returns File("/agentTmp")
        val loggerService = mockk<LoggerService>(relaxed = true)
        val environmentBuilder = BuildStepScopedTestEnvironmentBuilderImpl(agentEventDispatcher, parametersService, pathsService, loggerService)

        // Act
        environmentBuilder.setupEnvironmentForTestReporting()
        environmentBuilder.setupEnvironmentForTestReporting()

        // Assert
        verify(exactly = 1) {
            loggerService.writeMessage(any())
        }
    }

    @Test
    fun `should set up environment for test reporting if environment was set up previously but build step has changed`() {
        // Arrange
        val agentEventDispatcher = mockk<AgentEventDispatcher>(relaxed = true)
        val parametersService = mockk<ParametersService>()
        every { parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns null
        val pathsService = mockk<PathsService>()
        every { pathsService.getPath(PathType.AgentTemp) } returns File("/agentTmp")
        val loggerService = mockk<LoggerService>(relaxed = true)
        val environmentBuilder = BuildStepScopedTestEnvironmentBuilderImpl(agentEventDispatcher, parametersService, pathsService, loggerService)

        // Act
        environmentBuilder.setupEnvironmentForTestReporting()
        environmentBuilder.beforeRunnerStart(mockk<BuildRunnerContext>())
        environmentBuilder.setupEnvironmentForTestReporting()

        // Assert
        verify(exactly = 2) {
            loggerService.writeMessage(match {
                it.messageName == "importData" &&
                        it.attributes["type"] == "streamToBuildLog" &&
                        it.attributes["wrapFileContentInBlock"] == "false" &&
                        it.attributes["filePattern"] != null &&
                        // e.g. /agentTmp/TestReports/QQGkU6voRLudOukHGLgfgg==/*.msg
                        Pattern.matches("""[/\\]agentTmp[/\\]TestReports[/\\].{24}[/\\]\*\.msg""", it.attributes["filePattern"])
            })
        }
    }

    @Test
    fun `should return test reports files path with unique part`() {
        // Arrange
        val agentEventDispatcher = mockk<AgentEventDispatcher>(relaxed = true)
        val parametersService = mockk<ParametersService>()
        every { parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns null
        val pathsService = mockk<PathsService>()
        every { pathsService.getPath(PathType.AgentTemp) } returns File("/agentTmp")
        val loggerService = mockk<LoggerService>(relaxed = true)
        val environmentBuilder = BuildStepScopedTestEnvironmentBuilderImpl(agentEventDispatcher, parametersService, pathsService, loggerService)

        // Act
        val testReportsFilePath = environmentBuilder.getTestReportsFilesPathForBuildStep()

        // Assert
        // e.g. /agentTmp/TestReports/QQGkU6voRLudOukHGLgfgg==
        Assert.assertTrue(Pattern.matches("""[/\\]agentTmp[/\\]TestReports[/\\].{24}""", testReportsFilePath.toString()))
    }

    @Test
    fun `should return a constant test reports file path for a specific build step`() {
        // Arrange
        val agentEventDispatcher = mockk<AgentEventDispatcher>(relaxed = true)
        val parametersService = mockk<ParametersService>()
        every { parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns null
        val pathsService = mockk<PathsService>()
        every { pathsService.getPath(PathType.AgentTemp) } returns File("/agentTmp")
        val loggerService = mockk<LoggerService>(relaxed = true)
        val environmentBuilder = BuildStepScopedTestEnvironmentBuilderImpl(agentEventDispatcher, parametersService, pathsService, loggerService)

        // Act
        val testReportsFilePath1 = environmentBuilder.getTestReportsFilesPathForBuildStep()
        val testReportsFilePath2 = environmentBuilder.getTestReportsFilesPathForBuildStep()

        // Assert
        Assert.assertEquals(testReportsFilePath1, testReportsFilePath2)
    }

    @Test
    fun `should return unique test reports file path for each build step`() {
        // Arrange
        val agentEventDispatcher = mockk<AgentEventDispatcher>(relaxed = true)
        val parametersService = mockk<ParametersService>()
        every { parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns null
        val pathsService = mockk<PathsService>()
        every { pathsService.getPath(PathType.AgentTemp) } returns File("/agentTmp")
        val loggerService = mockk<LoggerService>(relaxed = true)
        val environmentBuilder = BuildStepScopedTestEnvironmentBuilderImpl(agentEventDispatcher, parametersService, pathsService, loggerService)

        // Act
        val pathForFirstStep = environmentBuilder.getTestReportsFilesPathForBuildStep()
        environmentBuilder.beforeRunnerStart(mockk<BuildRunnerContext>())
        val pathForSecondStep = environmentBuilder.getTestReportsFilesPathForBuildStep()

        // Assert
        // e.g. /agentTmp/TestReports/QQGkU6voRLudOukHGLgfgg==
        Assert.assertTrue(Pattern.matches("""[/\\]agentTmp[/\\]TestReports[/\\].{24}""", pathForFirstStep.toString()))
        Assert.assertTrue(Pattern.matches("""[/\\]agentTmp[/\\]TestReports[/\\].{24}""", pathForSecondStep.toString()))
        Assert.assertNotEquals(pathForFirstStep, pathForSecondStep)
    }

    @Test
    fun `test reports file path unqiue part should not contain path separators or illegal characters`() {
        // Arrange
        val uniquePathPartGenerator = BuildStepScopedTestEnvironmentBuilderImpl.UniquePathPartGenerator()
        val forbiddenCharacters = arrayOf('/', '\\', '<', '>', ':', '"', '|', '?', '*').toCharArray()

        for (i in 0..100000)
        {
            // Act
            val uniquePathPart = uniquePathPartGenerator.generateUniquePathPart()

            // Assert
            Assert.assertTrue(
                uniquePathPart.indexOfAny(forbiddenCharacters) == -1,
                "Forbidden characters in unique path part $uniquePathPart")
        }
    }
}
