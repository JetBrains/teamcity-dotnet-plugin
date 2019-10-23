package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetToolResolver
import jetbrains.buildServer.dotnet.DotnetToolResolverImpl
import jetbrains.buildServer.dotnet.test.rx.ObservablesTest
import jetbrains.buildServer.rx.NotificationCompleted
import jetbrains.buildServer.rx.NotificationError
import jetbrains.buildServer.rx.NotificationNext
import jetbrains.buildServer.rx.observableOf
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
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
        Assert.assertEquals(actualExecutable, ToolPath(Path(toolFile)))
    }

    @DataProvider
    fun osSpecificDotnet(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(OSType.WINDOWS, "dotnet.exe"),
                arrayOf(OSType.UNIX, "dotnet"),
                arrayOf(OSType.MAC, "dotnet")
        )
    }

    @Test(dataProvider = "osSpecificDotnet")
    fun shouldProvideExecutableFileWhenVirtualContext(os: OSType, expectedVirtualExecutable: String) {
        // Given
        val instance = createInstance()
        val toolFile = "dotnet.exe"

        // When
        every { _toolProvider.getPath(DotnetConstants.EXECUTABLE) } returns toolFile
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH) } returns null
        every { _virtualContext.isVirtual } returns true
        every { _virtualContext.targetOSType } returns os

        val actualExecutable = instance.executable

        // Then
        Assert.assertEquals(actualExecutable, ToolPath(Path("dotnet.exe"), Path(expectedVirtualExecutable)))
    }

    @Test
    fun shouldProvideExecutableFileWhenParameterWasOverridedByConfigParameter() {
        // Given
        val instance = createInstance()
        val toolFile = Path("dotnet.exe")

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