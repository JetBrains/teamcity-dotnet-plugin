package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.arguments.NugetPushArgumentsProvider
import jetbrains.buildServer.dotnet.arguments.CommandTarget
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class NugetPushArgumentsProviderTest {
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
        val argumentsProvider = NugetPushArgumentsProvider(ParametersServiceStub(parameters), TargetServiceStub(sequenceOf(CommandTarget(File("my.nupkg")))))

        // When
        val actualArguments = argumentsProvider.getArguments().map { it.value }.toList()

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
        val targetSeq = targets.map { CommandTarget(File(it )) }.asSequence()
        val argumentsProvider = NugetPushArgumentsProvider(ParametersServiceStub(emptyMap()), TargetServiceStub(targetSeq))

        // When
        val actualArguments = argumentsProvider.targetArguments.map { it.arguments.map { it.value }.toList() }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @Test
    fun shouldProvideCommand() {
        // Given
        val argumentsProvider = NugetPushArgumentsProvider(ParametersServiceStub(emptyMap()), TargetServiceStub(emptySequence()))

        // When
        val actualCommand = argumentsProvider.command

        // Then
        Assert.assertEquals(actualCommand, DotnetCommand.NuGetPush)
    }
}