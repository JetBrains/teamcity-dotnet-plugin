package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.mockk
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class SharedCompilationArgumentsProviderTest {
    @Test
    fun shouldProvideNodeReuseArgumentsWhenSharedCompilationRequiresSuppressing() {
        // Given
        val context = DotnetBuildContext(ToolPath(Path("wd")), mockk<DotnetCommand>(), Version(2, 1, 106), Verbosity.Detailed)

        // When
        val actualArguments = createInstance().getArguments(context).toList()

        // Then
        Assert.assertEquals(actualArguments, listOf(SharedCompilationArgumentsProvider.nodeReuseArgument))
    }

    @Test
    fun shouldProvideNodeReuseArgumentsWhenSharedCompilationDoesNotRequireSuppressing() {
        // Given
        val context = DotnetBuildContext(ToolPath(Path("wd")), mockk<DotnetCommand>(), Version.LastVersionWithoutSharedCompilation, Verbosity.Detailed)

        // When
        val actualArguments = createInstance().getArguments(context).toList()

        // Then
        Assert.assertEquals(actualArguments, emptyList<CommandLineArgument>())
    }


    private fun createInstance(): ArgumentsProvider {
        return SharedCompilationArgumentsProvider()
    }
}