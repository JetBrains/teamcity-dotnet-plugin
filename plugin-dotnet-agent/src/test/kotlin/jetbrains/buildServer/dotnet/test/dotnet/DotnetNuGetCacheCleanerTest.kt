package jetbrains.buildServer.dotnet.test.dotnet

import com.intellij.execution.ExecutionException
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.CleanType
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetNugetCacheCleaner
import jetbrains.buildServer.dotnet.EnvironmentVariables
import jetbrains.buildServer.agent.Version
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetNuGetCacheCleanerTest {
    @MockK private lateinit var _toolProvider: ToolProvider
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _environmentVariables: EnvironmentVariables
    @MockK private lateinit var _commandLineExecutor: CommandLineExecutor
    private val _dotnetExecutable = "Dotnet.exe"
    private val _workingDirectory = File("wd")
    private val _envVars = sequenceOf(CommandLineEnvironmentVariable("name`", "val"))

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _toolProvider.getPath(DotnetConstants.EXECUTABLE) } returns _dotnetExecutable
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns _workingDirectory
        every { _environmentVariables.getVariables(Version.Empty) } returns _envVars
    }

    @DataProvider
    fun targetsTestData(): Array<Array<List<Any>>> {
        return arrayOf(
                arrayOf(listOf("info : type: .nuget/packages/"), listOf(File(".nuget/packages/"))),
                arrayOf(listOf("info : TyPe: .nuget/packages/"), listOf(File(".nuget/packages/"))),
                arrayOf(listOf("TyPe: .nuget/packages/"), listOf(File(".nuget/packages/"))),
                arrayOf(listOf("info : type: ", "info : type: .nuget/packages/", ""), listOf(File(".nuget/packages/"))),
                arrayOf(listOf("info : type: .nuget/packages/", "info : type: .nuget/packages2/"), listOf(File(".nuget/packages/"))),
                arrayOf(listOf(" dds ", "info : type: .nuget/packages/", "info : abc: .nuget/packages/"), listOf(File(".nuget/packages/"))),
                arrayOf(listOf("info : type: "), emptyList<File>()),
                arrayOf(listOf("type: "), emptyList<File>()),
                arrayOf(listOf("info :"), emptyList<File>()),
                arrayOf(listOf(""), emptyList<File>()))
    }

    @Test(dataProvider = "targetsTestData")
    fun shouldProvideTargets(stdOut: List<String>, expectedTargets: List<File>) {
        // Given
        val instance = createInstance()

        // When
        every {  _commandLineExecutor.tryExecute(createComandLine()) } returns CommandLineResult(0, stdOut, emptyList())
        val actualTargets = instance.targets.toList()

        // Then
        Assert.assertEquals(actualTargets, expectedTargets)
    }

    @Test
    fun shouldNotProvideTargetsWhenHasNoDotnetTool() {
        // Given
        val instance = createInstance()

        // When
        every { _toolProvider.getPath(DotnetConstants.EXECUTABLE) } throws ToolCannotBeFoundException(DotnetConstants.EXECUTABLE)
        every {  _commandLineExecutor.tryExecute(createComandLine()) } returns CommandLineResult(0, listOf("info : type: .nuget/packages/"), emptyList())
        val actualTargets = instance.targets.toList()

        // Then
        Assert.assertEquals(actualTargets, emptyList<File>())
    }

    @Test
    fun shouldNotProvideTargetsWhenTryExecuteThrowsException() {
        // Given
        val instance = createInstance()

        // When
        every { _toolProvider.getPath(DotnetConstants.EXECUTABLE) } throws ToolCannotBeFoundException(DotnetConstants.EXECUTABLE)
        every {  _commandLineExecutor.tryExecute(createComandLine()) } throws ExecutionException("Cannot execute.")

        val actualTargets = instance.targets.toList()

        // Then
        Assert.assertEquals(actualTargets, emptyList<File>())
    }

    @Test
    fun shouldNotProvideTargetsWhenExitCodeIsNotZero() {
        // Given
        val instance = createInstance()

        // When
        every {  _commandLineExecutor.tryExecute(createComandLine()) } returns CommandLineResult(23, listOf("info : type: .nuget/packages/"), emptyList())
        val actualTargets = instance.targets.toList()

        // Then
        Assert.assertEquals(actualTargets, emptyList<File>())
    }

    @Test
    fun shouldDoCleanup() {
        // Given
        val instance = createInstance()
        every { _commandLineExecutor.tryExecute(any()) } returns CommandLineResult(0, emptyList<String>(), emptyList<String>())

        // When
        instance.clean(File("target"))

        // Then
        verify { _commandLineExecutor.tryExecute(createComandLine(listOf(DotnetNugetCacheCleaner.NUGET_ARG, DotnetNugetCacheCleaner.LOCALS_ARG, CommandLineArgument("type"), DotnetNugetCacheCleaner.CLEAR_ARG))) }
    }

    private fun createComandLine(args: List<CommandLineArgument> = listOf(DotnetNugetCacheCleaner.NUGET_ARG, DotnetNugetCacheCleaner.LOCALS_ARG, CommandLineArgument("type"), DotnetNugetCacheCleaner.LIST_ARG)) = CommandLine(
            null,
            TargetType.SystemDiagnostics,
            Path(_dotnetExecutable),
            Path(_workingDirectory.path),
            args,
            _envVars.toList())

    private fun createInstance() = DotnetNugetCacheCleaner(
            "type",
            "some description",
            CleanType.Medium,
            _toolProvider,
            _pathsService,
            _environmentVariables,
            _commandLineExecutor)
}