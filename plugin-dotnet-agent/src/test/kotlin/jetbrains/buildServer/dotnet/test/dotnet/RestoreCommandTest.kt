package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class RestoreCommandTest {
    private lateinit var _ctx: Mockery
    private lateinit var _resultsAnalyzer: ResultsAnalyzer

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _resultsAnalyzer = _ctx.mock<ResultsAnalyzer>(ResultsAnalyzer::class.java)
    }

    @DataProvider
    fun testRestoreArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")),
                        listOf("customArg1")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_NUGET_PACKAGES_DIR, "packages/")),
                        listOf("--packages", "packages/", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_NUGET_PACKAGE_SOURCES, "http://jb.com")),
                        listOf("--source", "http://jb.com", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_NUGET_PACKAGE_SOURCES, "http://jb.com\nhttp://jb.ru")),
                        listOf("--source", "http://jb.com", "--source", "http://jb.ru", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_NUGET_PACKAGE_SOURCES, "http://jb.com http://jb.ru")),
                        listOf("--source", "http://jb.com", "--source", "http://jb.ru", "customArg1")))
    }

    @Test(dataProvider = "testRestoreArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val command = createCommand(parameters = parameters, targets = sequenceOf("my.csproj"), arguments = sequenceOf(CommandLineArgument("customArg1")))

        // When
        val actualArguments = command.getArguments(DotnetBuildContext(File("wd"), command, DotnetSdk(File("dotnet"), Version.Empty))).map { it.value }.toList()

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
        Assert.assertEquals(actualCommand, DotnetCommandType.Restore)
    }

    @Test
    fun shouldProvideToolExecutableFile() {
        // Given
        val command = createCommand()

        // When
        val actualToolExecutableFile = command.toolResolver.executableFile

        // Then
        Assert.assertEquals(actualToolExecutableFile, File("dotnet"))
    }

    fun createCommand(
            parameters: Map<String, String> = emptyMap(),
            targets: Sequence<String> = emptySequence(),
            arguments: Sequence<CommandLineArgument> = emptySequence()): DotnetCommand =
            RestoreCommand(
                    ParametersServiceStub(parameters),
                    _resultsAnalyzer,
                    ArgumentsServiceStub(),
                    TargetServiceStub(targets.map { CommandTarget(File(it)) }.asSequence()),
                    ArgumentsProviderStub(arguments),
                    DotnetToolResolverStub(ToolPlatform.DotnetCore, File("dotnet"), true))
}