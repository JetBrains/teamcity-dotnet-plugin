package jetbrains.buildServer.dotnet.test.script

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.EnvironmentVariables
import jetbrains.buildServer.script.CommandLineFactoryImpl
import jetbrains.buildServer.script.ScriptResolver
import jetbrains.buildServer.script.ToolResolver
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class CommandLineFactoryTest {
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _toolResolver: ToolResolver
    @MockK private lateinit var _scriptProvider: ScriptResolver
    @MockK private lateinit var _nugetEnvironmentVariables: EnvironmentVariables
    @MockK private lateinit var _virtualContext: VirtualContext

    private val _envVars = listOf(CommandLineEnvironmentVariable("env1", "val1"))

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0) }
        every { _nugetEnvironmentVariables.getVariables(Version.Empty) } returns _envVars.asSequence()
    }

    @Test
    fun shouldCreate() {
        // Given
        val factory = createInstance()
        val workingDirectory = File("wd")
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns workingDirectory
        val tool = File("tool")
        every { _toolResolver.resolve() } returns tool
        val script = File("script")
        every { _scriptProvider.resolve() } returns script

        // When
        var commandLine = factory.create()

        // Then
        Assert.assertEquals(commandLine.baseCommandLine, null)
        Assert.assertEquals(commandLine.target, TargetType.Tool)
        Assert.assertEquals(commandLine.executableFile, Path(""))
        Assert.assertEquals(commandLine.workingDirectory, Path("v_" + workingDirectory.path))
        Assert.assertEquals(
                commandLine.arguments,
                listOf(
                        CommandLineArgument("v_tool"),
                        CommandLineArgument("v_script")
                ))
        Assert.assertEquals(commandLine.environmentVariables, _envVars)
    }

    private fun createInstance() =
            CommandLineFactoryImpl(_pathsService, _toolResolver, _scriptProvider, _nugetEnvironmentVariables, _virtualContext)
}