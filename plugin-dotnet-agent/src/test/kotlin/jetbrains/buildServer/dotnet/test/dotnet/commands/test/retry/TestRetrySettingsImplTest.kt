package jetbrains.buildServer.dotnet.test.dotnet.commands.test.retry

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetrySettingsImpl
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File

class TestRetrySettingsImplTest {
    @Test
    fun `should return default values`() {
        // arrange
        val parametersService = mockk<ParametersService> {
            every { tryGetParameter(any(), any()) } returns null
        }
        val settings = TestRetrySettingsImpl(parametersService, mockk<PathsService>())

        // act, assert
        Assert.assertFalse(settings.isEnabled)
        Assert.assertEquals(settings.maxRetries, 0)
        Assert.assertEquals(settings.maxFailures, 1000)
    }

    @Test
    fun `should return enabled when retry attempts count is not zero`() {
        // arrange
        val parametersService = mockk<ParametersService> {
            every { tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_TEST_RETRY_MAX_RETRIES) } returns "10"
        }
        val settings = TestRetrySettingsImpl(parametersService, mockk<PathsService>())

        // act, assert
        Assert.assertTrue(settings.isEnabled)
        Assert.assertEquals(settings.maxRetries, 10)
    }

    @Test
    fun `should return maximum number of tests to retry`() {
        // arrange
        val parametersService = mockk<ParametersService> {
            every { tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_TEST_RETRY_MAX_FAILURES) } returns "42"
        }
        val settings = TestRetrySettingsImpl(parametersService, mockk<PathsService>())

        // act, assert
        Assert.assertEquals(settings.maxFailures, 42)
    }

    @Test
    fun `should return location for vstest adapter failed tests reports`() {
        // arrange
        val pathsService = mockk<PathsService> {
            every { getPath(PathType.AgentTemp) } returns File("/agentTmp")
        }
        val settings = TestRetrySettingsImpl(mockk<ParametersService>(), pathsService)

        // act, assert
        Assert.assertEquals(settings.reportPath, "/agentTmp/TestRetry")
    }
}