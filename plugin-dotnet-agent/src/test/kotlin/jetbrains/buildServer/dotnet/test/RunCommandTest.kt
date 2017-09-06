package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class RunCommandTest {
    @DataProvider
    fun testRunArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(
                        DotnetConstants.PARAM_PATHS to "path/"),
                        emptyList<String>()),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_RUN_FRAMEWORK to "dotcore",
                        DotnetConstants.PARAM_RUN_CONFIG to "Release"),
                        listOf("--framework", "dotcore", "--configuration", "Release")))
    }

    @Test(dataProvider = "testRunArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val command = RunCommand(ParametersServiceStub(parameters), TargetServiceStub(sequenceOf(CommandTarget(File("my.csproj")))))

        // When
        val actualArguments = command.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @DataProvider
    fun projectsArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(listOf<String>("my.csproj") as Any, listOf<List<String>>(listOf<String>("--project", "my.csproj"))),
                arrayOf(emptyList<String>() as Any, emptyList<List<String>>()),
                arrayOf(listOf<String>("my.csproj", "my2.csproj") as Any, listOf<List<String>>(listOf<String>("--project", "my.csproj"), listOf<String>("--project", "my2.csproj"))))
    }

    @Test(dataProvider = "projectsArgumentsData")
    fun shouldProvideProjectsArguments(targets: List<String>, expectedArguments: List<List<String>>) {
        // Given
        val targetSeq = targets.map { CommandTarget(File(it)) }.asSequence()
        val command = RunCommand(ParametersServiceStub(emptyMap()), TargetServiceStub(targetSeq))

        // When
        val actualArguments = command.targetArguments.map { it.arguments.map { it.value }.toList() }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @Test
    fun shouldProvideCommandType() {
        // Given
        val command = RunCommand(ParametersServiceStub(emptyMap()), TargetServiceStub(emptySequence()))

        // When
        val actualCommand = command.commandType

        // Then
        Assert.assertEquals(actualCommand, DotnetCommandType.Run)
    }

    @DataProvider
    fun checkSuccessData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(0, true),
                arrayOf(1, true),
                arrayOf(99, true),
                arrayOf(-1, true),
                arrayOf(-99, true))
    }

    @Test(dataProvider = "checkSuccessData")
    fun shouldImplementCheckSuccess(exitCode: Int, expectedResult: Boolean) {
        // Given
        val command = RunCommand(ParametersServiceStub(emptyMap()), TargetServiceStub(emptySequence()))

        // When
        val actualResult = command.isSuccess(exitCode)

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }
}