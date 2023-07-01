package jetbrains.buildServer.dotnet.test.dotnet.commands.custom

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetBuildContext
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.commands.custom.TeamCityDotnetTestSuppressorCommand
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import org.testng.Assert
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.nio.file.Paths

class TeamCityDotnetTestSuppressorCommandTest {
    @MockK private lateinit var customCommand: DotnetCommand
    @MockK private lateinit var pathsService: PathsService
    @MockK private lateinit var parametersService: ParametersService
    private val testToolPath = Paths.get("tools/test-suppressor/TeamCity.Dotnet.TestSuppressor.dll")
    private lateinit var command: TeamCityDotnetTestSuppressorCommand


    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { pathsService.resolvePath(any(), any()) } returns testToolPath
        command = TeamCityDotnetTestSuppressorCommand(customCommand, pathsService, parametersService)
    }

    @AfterMethod
    fun tearDown() = unmockkAll()

    @Test
    fun `should return empty sequence when targetArguments is called`() {
        // arrange
        val expected = emptySequence<TargetArguments>()

        // act
        val result = command.targetArguments

        // assert
        Assert.assertEquals(result, expected)
    }

    @Test
    fun `should return filename when title is called`() {
        // arrange
        val expected = "TeamCity.Dotnet.TestSuppressor.dll"
        every { pathsService.resolvePath(any(), any()) } returns java.nio.file.Paths.get(expected)

        // act
        val result = command.title

        // assert
        Assert.assertEquals(result, expected)
    }

    @Test
    fun `should return true when isAuxiliary is called`() {
        // arrange
        val expected = true

        // act
        val result = command.isAuxiliary

        // assert
        Assert.assertEquals(result, expected)
    }

    @Test
    fun `should return correct arguments when getArguments is called`() {
        // arrange
        val expectedPath = java.nio.file.Paths.get("TeamCity.Dotnet.TestSuppressor.dll")
        every { pathsService.resolvePath(any(), any()) } returns expectedPath
        every { parametersService.tryGetParameter(any(), any()) } returns "Detailed"
        val expectedArguments = sequenceOf(
            CommandLineArgument("--roll-forward"),
            CommandLineArgument("LatestMajor"),
            CommandLineArgument(expectedPath.toString(), CommandLineArgumentType.Mandatory),
            CommandLineArgument("--verbosity"),
            CommandLineArgument("detailed")
        ).toList()
        val context: DotnetBuildContext = mockk()

        // act
        val result = command.getArguments(context).toList()

        // assert
        Assert.assertEquals(result, expectedArguments)
    }
}
