

package jetbrains.buildServer.dotnet.test.dotnet.commands.responseFile

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameter
import jetbrains.buildServer.dotnet.commands.responseFile.ResponseFileArgumentsProvider
import jetbrains.buildServer.dotnet.commands.responseFile.ResponseFileFactory
import jetbrains.buildServer.dotnet.test.dotnet.ArgumentsProviderStub
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class ResponseFileArgumentsProviderTest {
    @MockK private lateinit var _responseFileFactory: ResponseFileFactory
    @MockK private lateinit var _argumentsService: ArgumentsService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldProvideArguments() {
        // Given
        val rspFileName = "rspFile"
        val argsProvider1 = ArgumentsProviderStub(sequenceOf(CommandLineArgument("arg1"), CommandLineArgument("arg2")))
        val argsProvider2 = ArgumentsProviderStub(emptySequence())
        val argsProvider3 = ArgumentsProviderStub(sequenceOf(CommandLineArgument("arg3")))
        val argumentsProvider = createInstance(listOf(argsProvider1, argsProvider2, argsProvider3))
        val context = DotnetCommandContext(ToolPath(Path("wd")), mockk<DotnetCommand>(), Version(1, 2), Verbosity.Detailed)

        every {
            _responseFileFactory.createResponeFile(
                "",
                any(),
                any(),
                Verbosity.Detailed) } returns Path(rspFileName)

        // When
        val actualArguments = argumentsProvider.getArguments(context).toList()

        // Then
        verify {
            _responseFileFactory.createResponeFile(
                "",
                match { it.toList().equals(listOf(CommandLineArgument("arg1"), CommandLineArgument("arg2"), CommandLineArgument("arg3"))) },
                emptySequence<MSBuildParameter>(),
                Verbosity.Detailed)
        }

        Assert.assertEquals(actualArguments, listOf(CommandLineArgument("@${rspFileName}", CommandLineArgumentType.Infrastructural)))
    }

    private fun createInstance(argumentsProviders: List<ArgumentsProvider>) =
        ResponseFileArgumentsProvider(
                _responseFileFactory,
                argumentsProviders)
}