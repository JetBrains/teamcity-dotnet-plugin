package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.LoggerParameters
import jetbrains.buildServer.dotnet.VSTestLoggerArgumentsProvider
import jetbrains.buildServer.dotnet.Verbosity
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VSTestLoggerArgumentsProviderTest {
    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                // Success scenario
                arrayOf(
                        File("loggerPath", "vstestlogger.dll") as File?,
                        Verbosity.Normal,
                        listOf(
                                "/logger:logger://teamcity",
                                "/TestAdapterPath:${File("loggerPath").absolutePath}",
                                "/logger:console;verbosity=normal")),

                arrayOf(
                        File("loggerPath", "vstestlogger.dll") as File?,
                        Verbosity.Detailed,
                        listOf(
                                "/logger:logger://teamcity",
                                "/TestAdapterPath:${File("loggerPath").absolutePath}",
                                "/logger:console;verbosity=detailed"))
        )
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            loggerFile: File,
            verbosity: Verbosity,
            expectedArguments: List<String>) {
        // Given
        val ctx = Mockery()
        val loggerParameters = ctx.mock(LoggerParameters::class.java)
        val argumentsProvider = VSTestLoggerArgumentsProvider(LoggerResolverStub(File("msbuildlogger"), loggerFile), loggerParameters)

        // When
        ctx.checking(object : Expectations() {
            init {
                oneOf<LoggerParameters>(loggerParameters).vsTestVerbosity
                will(returnValue(verbosity))
            }
        })
        val actualArguments = argumentsProvider.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}