package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.dotnet.MSBuildParameter
import jetbrains.buildServer.dotnet.ResponseFileAlternative
import jetbrains.buildServer.dotnet.ResponseFileFactory
import jetbrains.buildServer.dotnet.Verbosity
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ResponseFileAlternativeTest {
    @MockK private lateinit var _responseFileFactory: ResponseFileFactory

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<List<CommandLineArgument>>> {
        return arrayOf(
                arrayOf(listOf(CommandLineArgument("-f"), CommandLineArgument("z")), listOf(CommandLineArgument("-f"), CommandLineArgument("z"))),
                arrayOf(listOf(CommandLineArgument("-f"), CommandLineArgument("z".repeat(ResponseFileAlternative.MaxArgSize / 2))), listOf(CommandLineArgument("-f"), CommandLineArgument("z".repeat(ResponseFileAlternative.MaxArgSize / 2)))),
                arrayOf(listOf(CommandLineArgument("-f"), CommandLineArgument("z".repeat(ResponseFileAlternative.MaxArgSize + 1))), listOf(CommandLineArgument("@rsp"))),
                arrayOf(listOf(CommandLineArgument("f".repeat(ResponseFileAlternative.MaxArgSize / 2 + 3)), CommandLineArgument("z".repeat(ResponseFileAlternative.MaxArgSize / 2))), listOf(CommandLineArgument("@rsp"))),
                arrayOf(listOf(), listOf())
        )
    }

    @Test(dataProvider = "testData")
    fun shouldSelectArguments(
            arguments: Collection<CommandLineArgument>,
            expectedArguments: Collection<CommandLineArgument>) {
        // Given
        val alternative = createInstance()
        val alternativeParameters = sequenceOf(MSBuildParameter("ff", "zz"));
        val alternativeArguments = sequenceOf(CommandLineArgument("aaa"));
        every { _responseFileFactory.createResponeFile("abc", alternativeArguments, alternativeParameters, Verbosity.Detailed) } returns Path("rsp")

        // When
        var actualArguments = alternative.select("abc", arguments, alternativeArguments, alternativeParameters, Verbosity.Detailed).toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments.toList())
    }

    private fun createInstance() = ResponseFileAlternative(_responseFileFactory)
}