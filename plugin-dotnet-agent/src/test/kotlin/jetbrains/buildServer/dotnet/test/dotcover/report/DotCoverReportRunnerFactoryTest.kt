package jetbrains.buildServer.dotnet.test.dotcover.report

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.dotcover.DotCoverEntryPointSelector
import jetbrains.buildServer.dotnet.coverage.dotcover.DotCoverReportRunnerFactory
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class DotCoverReportRunnerFactoryTest {
    @MockK private lateinit var _dotnetToolResolver: DotnetToolResolver
    @MockK private lateinit var _entryPointSelector: DotCoverEntryPointSelector
    @MockK private lateinit var _dotnetCoverageParameters: DotnetCoverageParameters
    @MockK private lateinit var _factory: DotCoverReportRunnerFactory

    @BeforeMethod
    fun setUp() {
        _dotnetToolResolver = mockk()
        _entryPointSelector = mockk()
        _dotnetCoverageParameters = mockk()

        _factory = DotCoverReportRunnerFactory(_dotnetToolResolver, _entryPointSelector)
    }

    @Test
    fun `should throw RunBuildException when entry point returns failure`() {
        // arrange
        every { _entryPointSelector.select() } returns Result.failure(Exception())

        // act, assert
        Assert.assertThrows(Exception::class.java) {
            _factory.getDotCoverReporter(_dotnetCoverageParameters)
        }
    }

    @Test
    fun `should return valid runner when file extension is dll`() {
        // arrange
        val entryPointFile = File("abc.dll")
        every { _entryPointSelector.select() } returns Result.success(entryPointFile)
        every { _dotnetToolResolver.executable.virtualPath } returns Path("/")

        // act
        val reporter = _factory.getDotCoverReporter(_dotnetCoverageParameters)

        // assert
        Assert.assertNotNull(reporter)
        verify (exactly = 1) { _dotnetToolResolver.executable.virtualPath }
    }

    @Test
    fun `should return null runner when file extension is not dll`() {
        // arrange
        val entryPointFile = File("abc.txt")
        every { _entryPointSelector.select() } returns Result.success(entryPointFile)
        every { _dotnetToolResolver.executable.virtualPath } returns Path("/")

        // act
        val reporter = _factory.getDotCoverReporter(_dotnetCoverageParameters)

        // assert
        Assert.assertNotNull(reporter)
        verify (exactly = 0) { _dotnetToolResolver.executable.virtualPath }
    }

    @Test
    fun `should throw RunBuildException when entry point selector throws ToolCannotBeFoundException`() {
        // arrange
        every { _entryPointSelector.select() } returns Result.failure(ToolCannotBeFoundException("Tool not found"))

        // act, assert
        Assert.assertThrows(RunBuildException::class.java) {
            _factory.getDotCoverReporter(_dotnetCoverageParameters)
        }
    }
}
