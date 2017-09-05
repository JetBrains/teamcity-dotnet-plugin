package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.arguments.CommandTarget
import jetbrains.buildServer.dotnet.arguments.RestoreArgumentsProvider
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class RestoreArgumentsProviderTest {
    @DataProvider
    fun testRestoreArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")), emptyList<String>()),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_RESTORE_PACKAGES, "packages/"),
                        Pair(DotnetConstants.PARAM_RESTORE_PARALLEL, "false")),
                        listOf("--packages", "packages/")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_RESTORE_PARALLEL, "true")), listOf("--disable-parallel")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_RESTORE_SOURCE, "http://jb.com")),
                        listOf("--source", "http://jb.com")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_RESTORE_SOURCE, "http://jb.com\nhttp://jb.ru")),
                        listOf("--source", "http://jb.com", "--source", "http://jb.ru")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_RESTORE_SOURCE, "http://jb.com http://jb.ru")),
                        listOf("--source", "http://jb.com", "--source", "http://jb.ru")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_RESTORE_NO_CACHE to " tRue",
                        DotnetConstants.PARAM_RESTORE_IGNORE_FAILED to "True ",
                        DotnetConstants.PARAM_RESTORE_ROOT_PROJECT to "true"),
                        listOf("--no-cache", "--ignore-failed-sources", "--no-dependencies")))
    }

    @Test(dataProvider = "testRestoreArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = RestoreArgumentsProvider(ParametersServiceStub(parameters), ArgumentsServiceStub(), TargetServiceStub(sequenceOf(CommandTarget(File("my.csproj")))))

        // When
        val actualArguments = argumentsProvider.getArguments().map { it.value }.toList()

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
        val targetSeq = targets.map { CommandTarget(File(it )) }.asSequence()
        val argumentsProvider = RestoreArgumentsProvider(ParametersServiceStub(emptyMap()), ArgumentsServiceStub(), TargetServiceStub(targetSeq))

        // When
        val actualArguments = argumentsProvider.targetArguments.map { it.arguments.map { it.value }.toList() }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @Test
    fun shouldProvideCommand() {
        // Given
        val argumentsProvider = RestoreArgumentsProvider(ParametersServiceStub(emptyMap()), ArgumentsServiceStub(), TargetServiceStub(emptySequence()))

        // When
        val actualCommand = argumentsProvider.command

        // Then
        Assert.assertEquals(actualCommand, DotnetCommand.Restore)
    }
}