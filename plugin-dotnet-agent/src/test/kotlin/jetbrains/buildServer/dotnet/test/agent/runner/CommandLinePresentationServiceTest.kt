package jetbrains.buildServer.dotnet.test.agent.runner

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.runner.Color
import jetbrains.buildServer.agent.CommandLinePresentationServiceImpl
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.runner.StdOutText
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class CommandLinePresentationServiceTest {
    @DataProvider
    fun testExecutableFilePresentation(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(Path("Dotnet.exe"), listOf(StdOutText("Dotnet.exe", Color.Header))),
                arrayOf(Path(File("Dir", "Dotnet.exe").path), listOf(StdOutText("Dir" + File.separator, Color.Minor), StdOutText("Dotnet.exe", Color.Header))))
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
                arrayOf(listOf(CommandLineArgument("Arg1", CommandLineArgumentType.Mandatory)), listOf(StdOutText(" Arg1", Color.Header))),
                arrayOf(listOf(CommandLineArgument("Arg1", CommandLineArgumentType.Secondary)), listOf(StdOutText(" Arg1", Color.Default))),
                arrayOf(listOf(CommandLineArgument("Arg1", CommandLineArgumentType.Custom)), listOf(StdOutText(" Arg1", Color.Details))),
                arrayOf(listOf(CommandLineArgument("Arg1", CommandLineArgumentType.Infrastructural)), listOf(StdOutText(" Arg1", Color.Minor))),
                arrayOf(listOf(CommandLineArgument("Arg1", CommandLineArgumentType.Mandatory), CommandLineArgument("Arg2", CommandLineArgumentType.Custom)), listOf(StdOutText(" Arg1", Color.Header), StdOutText(" Arg2", Color.Details))))
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
            CommandLinePresentationServiceImpl()
}