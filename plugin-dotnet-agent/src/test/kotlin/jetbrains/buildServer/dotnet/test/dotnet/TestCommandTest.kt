package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TestCommandTest {
    @DataProvider
    fun testTestArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")),
                        listOf("customArg1")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_FRAMEWORK, "dotcore"),
                        Pair(DotnetConstants.PARAM_CONFIG, "Release")),
                        listOf("--framework", "dotcore", "--configuration", "Release", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_SKIP_BUILD, "true")),
                        listOf("--no-build", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_OUTPUT_DIR, "out")),
                        listOf("--output", "out", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_TEST_CASE_FILTER, "filter")),
                        listOf("--filter", "filter", "customArg1")))
    }

    @Test(dataProvider = "testTestArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val command = createCommand(parameters=parameters, targets = sequenceOf("my.csproj"), arguments = sequenceOf(CommandLineArgument("customArg1")))

        // When
        val actualArguments = command.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @DataProvider
    fun projectsArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(listOf("my.csproj") as Any, listOf(listOf("my.csproj"))),
                arrayOf(emptyList<String>() as Any, emptyList<List<String>>()),
                arrayOf(listOf("my.csproj", "my2.csproj") as Any, listOf(listOf("my.csproj"), listOf("my2.csproj"))))
    }

    @Test(dataProvider = "projectsArgumentsData")
    fun shouldProvideProjectsArguments(targets: List<String>, expectedArguments: List<List<String>>) {
        // Given
        val command = createCommand(targets = targets.asSequence())

        // When
        val actualArguments = command.targetArguments.map { it.arguments.map { it.value }.toList() }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @Test
    fun shouldProvideCommandType() {
        // Given
        val command = createCommand()

        // When
        val actualCommand = command.commandType

        // Then
        Assert.assertEquals(actualCommand, DotnetCommandType.Test)
    }

    @DataProvider
    fun checkSuccessData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(0, false, true),
                arrayOf(0, true, true),
                arrayOf(1, true, true),
                arrayOf(1, false, false),
                arrayOf(99, true, true),
                arrayOf(99, false, false),
                arrayOf(-1, true, false),
                arrayOf(-1, false, false),
                arrayOf(-99, true, false),
                arrayOf(-99, false, false))
    }

    @Test(dataProvider = "checkSuccessData")
    fun shouldImplementCheckSuccess(exitCode: Int, hasFailedTest: Boolean, expectedResult: Boolean) {
        // Given
        val command = createCommand(emptyMap(), emptySequence(), emptySequence(), FailedTestDetectorStub(hasFailedTest))

        // When
        val actualResult = command.isSuccessful(CommandLineResult(sequenceOf(exitCode), emptySequence(), emptySequence()))

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }

    fun createCommand(
            parameters: Map<String, String> = emptyMap(),
            targets: Sequence<String> = emptySequence(),
            arguments: Sequence<CommandLineArgument> = emptySequence(),
            failedTestDetector: FailedTestDetector = FailedTestDetectorStub(false)): DotnetCommand =
            TestCommand(
                    ParametersServiceStub(parameters),
                    failedTestDetector,
                    TargetServiceStub(targets.map { CommandTarget(File(it)) }.asSequence()),
                    DotnetCommonArgumentsProviderStub(arguments),
                    DotnetToolResolverStub(File("dotnet"), true))

}