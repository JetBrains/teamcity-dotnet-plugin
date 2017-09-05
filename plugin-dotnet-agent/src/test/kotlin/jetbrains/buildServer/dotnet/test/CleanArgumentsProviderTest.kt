package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.arguments.CleanArgumentsProvider
import jetbrains.buildServer.dotnet.arguments.CommandTarget
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class CleanArgumentsProviderTest {
    @DataProvider
    fun testCleanArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")), emptyList<String>()),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_CLEAN_FRAMEWORK, "dnxcore50"),
                        Pair(DotnetConstants.PARAM_CLEAN_CONFIG, "Release"),
                        Pair(DotnetConstants.PARAM_CLEAN_RUNTIME, "win7-x64")),
                        listOf("--framework", "dnxcore50", "--configuration", "Release", "--runtime", "win7-x64")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_CLEAN_OUTPUT, "output/")),
                        listOf("--output", "output/")))
    }

    @Test(dataProvider = "testCleanArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = CleanArgumentsProvider(ParametersServiceStub(parameters), TargetServiceStub(sequenceOf(CommandTarget(File("my.csproj")))))

        // When
        val actualArguments = argumentsProvider.arguments.map { it.value }.toList()

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
        val argumentsProvider = CleanArgumentsProvider(ParametersServiceStub(emptyMap()), TargetServiceStub(targetSeq))

        // When
        val actualArguments = argumentsProvider.targetArguments.map { it.arguments.map { it.value }.toList() }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @Test
    fun shouldProvideCommand() {
        // Given
        val argumentsProvider = CleanArgumentsProvider(ParametersServiceStub(emptyMap()), TargetServiceStub(emptySequence()))

        // When
        val actualCommand = argumentsProvider.command

        // Then
        Assert.assertEquals(actualCommand, DotnetCommand.Clean)
    }
}