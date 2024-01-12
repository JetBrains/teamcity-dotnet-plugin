

package jetbrains.buildServer.dotnet.test.dotnet.discovery.vstest

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.vstest.VSTestLoggerArgumentsProvider
import jetbrains.buildServer.dotnet.logging.LoggerParameters
import jetbrains.buildServer.dotnet.test.dotnet.logging.LoggerResolverStub
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VSTestLoggerArgumentsProviderTest {
    @MockK private lateinit var _loggerParameters: LoggerParameters
    @MockK private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0)}
    }

    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                // Success scenario
                arrayOf(
                        File("loggerPath", "vstestlogger.dll") as File?,
                        Verbosity.Normal,
                        listOf(
                                "/logger:logger://teamcity",
                                "/TestAdapterPath:v_${File("loggerPath").canonicalPath}",
                                "/logger:console;verbosity=normal")),

                arrayOf(
                        File("loggerPath", "vstestlogger.dll") as File?,
                        Verbosity.Detailed,
                        listOf(
                                "/logger:logger://teamcity",
                                "/TestAdapterPath:v_${File("loggerPath").canonicalPath}",
                                "/logger:console;verbosity=detailed"))
        )
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            loggerFile: File,
            verbosity: Verbosity,
            expectedArguments: List<String>) {
        // Given

        val context = DotnetCommandContext(ToolPath(Path("wd")), mockk<DotnetCommand>())
        val argumentsProvider = VSTestLoggerArgumentsProvider(LoggerResolverStub(File("msbuildlogger"), loggerFile), _loggerParameters, _virtualContext)
        every { _loggerParameters.vsTestVerbosity } returns verbosity

        // When
        val actualArguments = argumentsProvider.getArguments(context).map { it.value }.toList()

        // Then
        verify { _virtualContext.resolvePath(any()) }
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}