package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.dotnet.*
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

        val context = DotnetBuildContext(File("wd"), mockk<DotnetCommand>())
        val argumentsProvider = VSTestLoggerArgumentsProvider(LoggerResolverStub(File("msbuildlogger"), loggerFile), _loggerParameters, _virtualContext)
        every { _loggerParameters.vsTestVerbosity } returns verbosity

        // When
        val actualArguments = argumentsProvider.getArguments(context).map { it.value }.toList()

        // Then
        verify { _virtualContext.resolvePath(any()) }
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}