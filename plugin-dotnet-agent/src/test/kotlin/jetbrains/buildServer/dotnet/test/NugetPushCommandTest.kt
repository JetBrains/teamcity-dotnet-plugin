package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class NugetPushCommandTest {
    @DataProvider
    fun testNugetPushArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(
                        DotnetConstants.PARAM_PATHS to "package.nupkg",
                        DotnetConstants.PARAM_NUGET_PUSH_API_KEY to "key",
                        DotnetConstants.PARAM_NUGET_PUSH_SOURCE to "http://jb.com"),
                        listOf("--api-key", "key", "--source", "http://jb.com")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_PATHS to "package.nupkg",
                        DotnetConstants.PARAM_NUGET_PUSH_NO_BUFFER to "true",
                        DotnetConstants.PARAM_NUGET_PUSH_NO_SYMBOLS to "true"),
                        listOf("--no-symbols", "true", "--disable-buffering", "true"))
        )
    }

    @Test(dataProvider = "testNugetPushArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val command = NugetPushCommand(ParametersServiceStub(parameters), TargetServiceStub(sequenceOf(CommandTarget(File("my.nupkg")))))

        // When
        val actualArguments = command.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @DataProvider
    fun projectsArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(listOf<String>("my.nupkg") as Any, listOf<List<String>>(listOf<String>("my.nupkg"))),
                arrayOf(emptyList<String>() as Any, emptyList<List<String>>()),
                arrayOf(listOf<String>("my.nupkg", "my2.nupkg") as Any, listOf<List<String>>(listOf<String>("my.nupkg"), listOf<String>("my2.nupkg"))))
    }

    @Test(dataProvider = "projectsArgumentsData")
    fun shouldProvideProjectsArguments(targets: List<String>, expectedArguments: List<List<String>>) {
        // Given
        val targetSeq = targets.map { CommandTarget(File(it)) }.asSequence()
        val command = NugetPushCommand(ParametersServiceStub(emptyMap()), TargetServiceStub(targetSeq))

        // When
        val args = command.targetArguments.map { it.arguments.map { it.value }.toList() }.toList()

        // Then
        Assert.assertEquals(args, expectedArguments)
    }

    @Test
    fun shouldProvideCommandType() {
        // Given
        val command = NugetPushCommand(ParametersServiceStub(emptyMap()), TargetServiceStub(emptySequence()))

        // When
        val actualCommand = command.commandType

        // Then
        Assert.assertEquals(actualCommand, DotnetCommandType.NuGetPush)
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
        val command = BuildCommand(ParametersServiceStub(emptyMap()), TargetServiceStub(emptySequence()))

        // When
        val actualResult = command.isSuccess(exitCode)

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }
}