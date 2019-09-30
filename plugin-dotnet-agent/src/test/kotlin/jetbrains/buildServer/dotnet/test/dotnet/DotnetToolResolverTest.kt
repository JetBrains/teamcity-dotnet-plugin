package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetToolResolver
import jetbrains.buildServer.dotnet.DotnetToolResolverImpl
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class DotnetToolResolverTest {
    @MockK private lateinit var _toolProvider: ToolProvider
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldProvideExecutableFile() {
        // Given
        val instance = createInstance()
        val toolFile = "dotnet.exe"

        // When
        every { _toolProvider.getPath(DotnetConstants.EXECUTABLE) } returns toolFile
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH) } returns null
        every { _virtualContext.isVirtual } returns false

        val actualExecutable = instance.executable

        // Then
        Assert.assertEquals(actualExecutable, ToolPath(File(toolFile)))
    }

    @Test
    fun shouldProvideExecutableFileWhenVirtualContext() {
        // Given
        val instance = createInstance()
        val toolFile = "dotnet.exe"

        // When
        every { _toolProvider.getPath(DotnetConstants.EXECUTABLE) } returns toolFile
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH) } returns null
        every { _virtualContext.isVirtual } returns true

        val actualExecutable = instance.executable

        // Then
        Assert.assertEquals(actualExecutable, ToolPath(File("dotnet.exe"), File("dotnet")))
    }

    @Test
    fun shouldProvideExecutableFileWhenParameterWasOverridedByConfigParameter() {
        // Given
        val instance = createInstance()
        val toolFile = File("dotnet.exe")

        // When
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH) } returns toolFile.path
        every { _virtualContext.isVirtual } returns false

        val actualExecutable = instance.executable

        // Then
        Assert.assertEquals(actualExecutable, ToolPath(toolFile))
    }

    private fun createInstance(): DotnetToolResolver {
        return DotnetToolResolverImpl(_toolProvider, _parametersService, _virtualContext)
    }
}