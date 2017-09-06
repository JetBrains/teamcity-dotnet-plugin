package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.runners.CommandLineArgument
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TestCommandTest {
    @DataProvider
    fun testTestArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")), emptyList<String>()),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_TEST_FRAMEWORK, "dotcore"),
                        Pair(DotnetConstants.PARAM_TEST_CONFIG, "Release")),
                        listOf("--framework", "dotcore", "--configuration", "Release")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_TEST_RUNTIME, "active"),
                        Pair(DotnetConstants.PARAM_TEST_NO_BUILD, "true")),
                        listOf("--runtime", "active", "--no-build")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_TEST_OUTPUT, "out")),
                        listOf("--output", "out")))
    }

    @Test(dataProvider = "testTestArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val command = TestCommand(ParametersServiceStub(parameters), argumentsProvider, TargetServiceStub(sequenceOf(CommandTarget(File("my.csproj")))))

        // When
        val actualArguments = command.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @DataProvider
    fun projectsArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(listOf<String>("my.csproj") as Any, listOf<List<String>>(listOf<String>("my.csproj"))),
                arrayOf(emptyList<String>() as Any, emptyList<List<String>>()),
                arrayOf(listOf<String>("my.csproj", "my2.csproj") as Any, listOf<List<String>>(listOf<String>("my.csproj"), listOf<String>("my2.csproj"))))
    }

    @Test(dataProvider = "projectsArgumentsData")
    fun shouldProvideProjectsArguments(targets: List<String>, expectedArguments: List<List<String>>) {
        // Given
        val targetSeq = targets.map { CommandTarget(File(it)) }.asSequence()
        val command = TestCommand(ParametersServiceStub(emptyMap()), argumentsProvider, TargetServiceStub(targetSeq))

        // When
        val actualArguments = command.targetArguments.map { it.arguments.map { it.value }.toList() }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @Test
    fun shouldProvideCommandType() {
        // Given
        val command = TestCommand(ParametersServiceStub(emptyMap()), argumentsProvider, TargetServiceStub(emptySequence()))

        // When
        val actualCommand = command.commandType

        // Then
        Assert.assertEquals(actualCommand, DotnetCommandType.Test)
    }

    @DataProvider
    fun checkSuccessData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(0, true),
                arrayOf(1, true),
                arrayOf(99, true),
                arrayOf(-1, false),
                arrayOf(-99, false))
    }

    @Test(dataProvider = "checkSuccessData")
    fun shouldImplementCheckSuccess(exitCode: Int, expectedResult: Boolean) {
        // Given
        val command = TestCommand(ParametersServiceStub(emptyMap()), argumentsProvider, TargetServiceStub(emptySequence()))

        // When
        val actualResult = command.isSuccess(exitCode)

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }

    companion object {
        val argumentsProvider = object: ArgumentsProvider {
            override val arguments: Sequence<CommandLineArgument>
                get() = emptySequence()
        }
    }
}