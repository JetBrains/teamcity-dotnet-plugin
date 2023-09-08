package jetbrains.buildServer.dotnet.test.dotnet.commands.test

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetBuildContext
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.TestCommandEnvironmentBuilder
import org.testng.annotations.Test
import java.io.File

class TestCommandEnvironmentBuilderTest {
    @Test
    fun `should not send file streaming service message if fallback to stdout test reporting is enabled`() {
        // Given
        val parametersService = mockk<ParametersService>()
        every { parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns "true"
        val pathsService = mockk<PathsService>()
        val loggerService = mockk<LoggerService>()
        val buildContext = mockk<DotnetBuildContext>()
        val environmentBuilder = TestCommandEnvironmentBuilder(parametersService, pathsService, loggerService)

        // When
        environmentBuilder.build(buildContext)

        // Then
        verify(exactly = 0) { loggerService.writeMessage(any()) }
    }

    @Test
    fun `should send file streaming service message if fallback to stdout test reporting is not enabled`() {
        // Given
        val parametersService = mockk<ParametersService>()
        every { parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns null
        val pathsService = mockk<PathsService>()
        every { pathsService.getPath(PathType.AgentTemp) } returns File("/agentTmp")
        val loggerService = mockk<LoggerService>(relaxed = true)
        val buildContext = mockk<DotnetBuildContext>()
        val environmentBuilder = TestCommandEnvironmentBuilder(parametersService, pathsService, loggerService)

        // When
        environmentBuilder.build(buildContext)

        // Then
        val expectedFilePattern = "/agentTmp/TestReports/*.msg"
        verify {
            loggerService.writeMessage(match {
                it.messageName == "importData" &&
                it.attributes["type"] == "streamToBuildLog" &&
                it.attributes["filePattern"] == expectedFilePattern &&
                it.attributes["wrapFileContentInBlock"] == "false"
            })
        }
    }
}
