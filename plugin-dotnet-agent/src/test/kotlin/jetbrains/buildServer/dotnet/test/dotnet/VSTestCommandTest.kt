package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VSTestCommandTest {
    @DataProvider
    fun argumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")),
                        listOf("vstestlog", "customArg1")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_VSTEST_CONFIG_FILE to "myconfig.txt",
                        DotnetConstants.PARAM_VSTEST_TEST_NAMES to "abc,zxy",
                        DotnetConstants.PARAM_VSTEST_IN_ISOLATION to "TrUe",
                        DotnetConstants.PARAM_VSTEST_PLATFORM to "x86",
                        DotnetConstants.PARAM_VSTEST_FRAMEWORK to "net45",
                        DotnetConstants.PARAM_VSTEST_TEST_CASE_FILTER to "myfilter"),
                        listOf("/Settings:myconfig.txt", "/Tests:abc,zxy", "/InIsolation", "/Platform:x86", "/Framework:net45", "/TestCaseFilter:myfilter", "vstestlog", "customArg1")),
                arrayOf(mapOf(DotnetConstants.PARAM_PATHS to "my.dll",
                        DotnetConstants.PARAM_VSTEST_TEST_NAMES to "test1 test2; test3"),
                        listOf("/Tests:test1,test2,test3", "vstestlog", "customArg1")))
    }

    @Test(dataProvider = "argumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val command = createCommand(parameters=parameters, targets = sequenceOf("my.dll"), arguments = sequenceOf(CommandLineArgument("customArg1")))

        // When
        val actualArguments = command.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @DataProvider
    fun projectsArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(listOf("my.dll") as Any, listOf(listOf("my.dll"))),
                arrayOf(emptyList<String>() as Any, emptyList<List<String>>()),
                arrayOf(listOf("my.dll", "my2.dll") as Any, listOf(listOf("my.dll"), listOf("my2.dll"))))
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
        Assert.assertEquals(actualCommand, DotnetCommandType.VSTest)
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
        val command = createCommand()

        // When
        val actualResult = command.isSuccessfulExitCode(exitCode)

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }

    @Test
    fun shouldProvideToolExecutableFile() {
        // Given
        val command = createCommand()

        // When
        val actualToolExecutableFile = command.toolResolver.executableFile

        // Then
        Assert.assertEquals(actualToolExecutableFile, File("vstest.console.exe"))
    }

    fun createCommand(
            parameters: Map<String, String> = emptyMap(),
            targets: Sequence<String> = emptySequence(),
            arguments: Sequence<CommandLineArgument> = emptySequence()): DotnetCommand =
            VSTestCommand(
                    ParametersServiceStub(parameters),
                    TargetServiceStub(targets.map { CommandTarget(File(it)) }.asSequence()),
                    DotnetCommonArgumentsProviderStub(sequenceOf(CommandLineArgument("vstestlog"))),
                    DotnetCommonArgumentsProviderStub(arguments),
                    DotnetToolResolverStub(File("vstest.console.exe"), true))
}