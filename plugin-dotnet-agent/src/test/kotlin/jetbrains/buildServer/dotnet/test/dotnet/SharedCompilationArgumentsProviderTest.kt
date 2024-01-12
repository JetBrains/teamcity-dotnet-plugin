

package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.mockk
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.Test

class SharedCompilationArgumentsProviderTest {
    @Test
    fun `should provide node reuse arguments when shared compilation requires suppression`() {
        // Given
        val context = DotnetCommandContext(ToolPath(Path("wd")), mockk<DotnetCommand>(), Version(2, 1, 106), Verbosity.Detailed)

        // When
        val actualArguments = createInstance().getArguments(context).toList()

        // Then
        Assert.assertEquals(actualArguments, listOf(SharedCompilationArgumentsProvider.nodeReuseArgument))
    }

    @Test
    fun `should provide node reuse arguments when shared compilation does not require suppression`() {
        // Given
        val context = DotnetCommandContext(ToolPath(Path("wd")), mockk<DotnetCommand>(), Version.LastVersionWithoutSharedCompilation, Verbosity.Detailed)

        // When
        val actualArguments = createInstance().getArguments(context).toList()

        // Then
        Assert.assertEquals(actualArguments, emptyList<CommandLineArgument>())
    }


    private fun createInstance(): ArgumentsProvider {
        return SharedCompilationArgumentsProvider()
    }
}