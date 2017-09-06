package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class RestoreCommandTest {
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
        val command = RestoreCommand(ParametersServiceStub(parameters), ArgumentsServiceStub(), TargetServiceStub(sequenceOf(CommandTarget(File("my.csproj")))))

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
        val command = RestoreCommand(ParametersServiceStub(emptyMap()), ArgumentsServiceStub(), TargetServiceStub(targetSeq))

        // When
        val actualArguments = command.targetArguments.map { it.arguments.map { it.value }.toList() }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @Test
    fun shouldProvideCommandType() {
        // Given
        val command = RestoreCommand(ParametersServiceStub(emptyMap()), ArgumentsServiceStub(), TargetServiceStub(emptySequence()))

        // When
        val actualCommand = command.commandType

        // Then
        Assert.assertEquals(actualCommand, DotnetCommandType.Restore)
    }

    @DataProvider
    fun checkSuccessData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(0, true),
                arrayOf(1, false),
                arrayOf(99, false),
                arrayOf(-1, false),
                arrayOf(-99, false))
    }

    @Test(dataProvider = "checkSuccessData")
    fun shouldImplementCheckSuccess(exitCode: Int, expectedResult: Boolean) {
        // Given
        val command = RestoreCommand(ParametersServiceStub(emptyMap()), ArgumentsServiceStub(), TargetServiceStub(emptySequence()))

        // When
        val actualResult = command.isSuccess(exitCode)

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }
}