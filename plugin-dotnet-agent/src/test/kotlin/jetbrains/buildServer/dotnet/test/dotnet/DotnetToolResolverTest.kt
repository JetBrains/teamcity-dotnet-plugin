package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetToolResolver
import jetbrains.buildServer.dotnet.DotnetToolResolverImpl
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetToolResolverTest {
    private val _toolFile = Path("dotnet.exe")
    @MockK private lateinit var _environment: Environment
    @MockK private lateinit var _toolSearchService: ToolSearchService
    @MockK private lateinit var _toolEnvironment: ToolEnvironment
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _toolSearchService.find(DotnetConstants.EXECUTABLE, emptySequence()) } returns emptySequence()
        every { _toolEnvironment.homePaths } returns emptySequence()
        every { _virtualContext.isVirtual } returns false
    }

    @Test
    fun shouldProvideExecutableFile() {
        // Given
        val instance = createInstance()
        val toolFile = "dotnet.exe"

        // When
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH) } returns _toolFile.path
        val actualExecutable = instance.executable

        // Then
        Assert.assertEquals(actualExecutable, ToolPath(Path(toolFile)))
    }

    @Test
    fun shouldProvideExecutableFileFromHomeEnvVarWhenItWasSpecified() {
        // Given
        val instance = createInstance()
        val homePaths = sequenceOf(Path("home"))

        // When
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH) } returns _toolFile.path
        every { _toolEnvironment.homePaths } returns homePaths
        every { _toolSearchService.find(DotnetConstants.EXECUTABLE, homePaths) } returns sequenceOf(File("home_dotnet.exe"))

        val actualExecutable = instance.executable

        // Then
        Assert.assertEquals(actualExecutable, ToolPath(Path("home_dotnet.exe")))
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
    fun shouldProvideExecutableFileWhenInVirtualContext(os: OSType, expectedVirtualExecutable: String) {
        // Given
        val instance = createInstance()

        // When
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH) } returns _toolFile.path
        every { _virtualContext.isVirtual } returns true
        every { _virtualContext.targetOSType } returns os

        val actualExecutable = instance.executable

        // Then
        Assert.assertEquals(actualExecutable, ToolPath(Path("dotnet.exe"), Path(expectedVirtualExecutable)))
    }

    @DataProvider(name = "osVariants")
    fun osVariants(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(OSType.WINDOWS, OSType.UNIX, ToolPath(Path("home\\dotnet.exe"), Path("home/dotnet"))),
                arrayOf(OSType.WINDOWS, OSType.MAC, ToolPath(Path("home\\dotnet.exe"), Path("home/dotnet"))),
                arrayOf(OSType.UNIX, OSType.WINDOWS, ToolPath(Path("home/dotnet"), Path("home\\dotnet.exe"))),
                arrayOf(OSType.MAC, OSType.WINDOWS, ToolPath(Path("home/dotnet"), Path("home\\dotnet.exe"))),
                arrayOf(OSType.WINDOWS, OSType.WINDOWS, ToolPath(Path("home\\dotnet.exe"), Path("home\\dotnet.exe"))),
                arrayOf(OSType.MAC, OSType.MAC, ToolPath(Path("home/dotnet"), Path("home/dotnet")))
        )
    }

    @Test(dataProvider = "osVariants")
    fun shouldProvideExecutableFileFromHomeEnvVarWhenItWasSpecifiedWhenInVirtualContext(hostOs: OSType, containerOs: OSType, expectedTool: ToolPath) {
        // Given
        val instance = createInstance()
        val homePaths = sequenceOf(Path("home"))

        // When
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH) } returns null
        every { _virtualContext.isVirtual } returns true
        every { _environment.os } returns hostOs
        every { _virtualContext.targetOSType } returns containerOs
        every { _toolEnvironment.homePaths } returns homePaths
        every { _toolSearchService.find(DotnetConstants.EXECUTABLE, homePaths) } returns emptySequence()
        val actualExecutable = instance.executable

        // Then
                //Path("home_dotnet.exe")
        Assert.assertEquals(actualExecutable, expectedTool)
    }

    @Test
    fun shouldProvideExecutableFileFromHomeEnvVarWhenItWasSpecifiedWhenInVirtualContextAndHasOnTheHost() {
        // Given
        val instance = createInstance()
        val homePaths = sequenceOf(Path("home"))

        // When
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH) } returns null
        every { _virtualContext.isVirtual } returns true
        every { _virtualContext.targetOSType } returns OSType.UNIX
        every { _toolEnvironment.homePaths } returns homePaths
        every { _toolSearchService.find(DotnetConstants.EXECUTABLE, homePaths) } returns sequenceOf(File("home_dotnet.exe"))
        val actualExecutable = instance.executable

        // Then
        Assert.assertEquals(actualExecutable, ToolPath(Path("home_dotnet.exe"), Path("home/dotnet")))
    }

    private fun createInstance(): DotnetToolResolver {
        return DotnetToolResolverImpl(_parametersService, _toolEnvironment, _toolSearchService, _environment, _virtualContext)
    }
}