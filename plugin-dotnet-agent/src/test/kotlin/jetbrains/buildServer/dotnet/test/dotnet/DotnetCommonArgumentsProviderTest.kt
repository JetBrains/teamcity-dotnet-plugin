

package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.mockk
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetCommonArgumentsProviderTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(true, listOf("rspArg", "customArg")),
            arrayOf(false, listOf("l:/logger", "/nodeReuse:false", "customArg"))
        )
    }

    @Test(dataProvider = "testData")
    fun shouldGetArguments(useRspFile: Boolean, expectedArguments: List<String>) {
        // Given
        val context = DotnetCommandContext(ToolPath(Path("wd")), mockk<DotnetCommand>())
        val argumentsProvider = DotnetCommonArgumentsProviderImpl(
            useRspFile,
            ArgumentsProviderStub(sequenceOf(CommandLineArgument("rspArg"))),
            ArgumentsProviderStub(sequenceOf(CommandLineArgument("customArg"))),
            listOf(
                ArgumentsProviderStub(sequenceOf(CommandLineArgument("l:/logger"))),
                ArgumentsProviderStub(sequenceOf(CommandLineArgument("/nodeReuse:false")))
            )
        )

        // When
        val actualArguments = argumentsProvider.getArguments(context).map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}