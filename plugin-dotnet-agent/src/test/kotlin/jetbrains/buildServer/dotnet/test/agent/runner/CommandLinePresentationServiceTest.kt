package jetbrains.buildServer.dotnet.test.agent.runner

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.StdOutText
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class CommandLinePresentationServiceTest {
    @MockK private lateinit var _environment: Environment
    @MockK private lateinit var _argumentsService: ArgumentsService
    @MockK private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        every { _environment.os } returns OSType.WINDOWS
        every { _argumentsService.normalize(any()) } answers { "\"${arg<String>(0)}\"" }
        every { _virtualContext.targetOSType } returns OSType.UNIX
    }

    @DataProvider
    fun testExecutableFilePresentation(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(Path("Dotnet.exe"), listOf(StdOutText("Dotnet.exe"))),
                arrayOf(Path(File("Dir", "Dotnet.exe").path), listOf(StdOutText("Dir/"), StdOutText("Dotnet.exe"))))
    }

    @Test(dataProvider = "testExecutableFilePresentation")
    fun shouldBuildExecutableFilePresentation(executableFile: Path, expectedOutput: List<StdOutText>) {
        // Given
        var presentation = createInstance()

        // When
        val actualOutput = presentation.buildExecutablePresentation(executableFile)

        // Then
        Assert.assertEquals(actualOutput, expectedOutput)
    }

    @DataProvider
    fun testArgsPresentation(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(listOf(CommandLineArgument("Arg1", CommandLineArgumentType.Mandatory)), listOf(StdOutText(" \"Arg1\""))),
                arrayOf(listOf(CommandLineArgument("Arg1", CommandLineArgumentType.Target)), listOf(StdOutText(" \"Arg1\""))),
                arrayOf(listOf(CommandLineArgument("Arg1", CommandLineArgumentType.Secondary)), listOf(StdOutText(" \"Arg1\""))),
                arrayOf(listOf(CommandLineArgument("Arg1", CommandLineArgumentType.Custom)), listOf(StdOutText(" \"Arg1\""))),
                arrayOf(listOf(CommandLineArgument("Arg1", CommandLineArgumentType.Infrastructural)), listOf(StdOutText(" \"Arg1\""))),
                arrayOf(listOf(CommandLineArgument("Arg1", CommandLineArgumentType.Mandatory), CommandLineArgument("Arg2", CommandLineArgumentType.Custom)), listOf(StdOutText(" \"Arg1\""), StdOutText(" \"Arg2\""))))
    }

    @Test(dataProvider = "testArgsPresentation")
    fun shouldBuildArgsPresentation(arguments: List<CommandLineArgument>, expectedOutput: List<StdOutText>) {
        // Given
        var presentation = createInstance()

        // When
        val actualOutput = presentation.buildArgsPresentation(arguments)

        // Then
        Assert.assertEquals(actualOutput, expectedOutput)
    }

    private fun createInstance() =
            CommandLinePresentationServiceImpl(_environment, _argumentsService, _virtualContext)
}