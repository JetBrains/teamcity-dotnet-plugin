package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.util.OSType
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class EnvironmentVariablesTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(OSType.UNIX, Version(2, 1, 200), sequenceOf(CommandLineEnvironmentVariable("UseSharedCompilation", "false"))),
                arrayOf(OSType.MAC, Version(2, 1, 200), sequenceOf(CommandLineEnvironmentVariable("UseSharedCompilation", "false"))),
                arrayOf(OSType.UNIX, Version(2, 1, 300), sequenceOf(CommandLineEnvironmentVariable("UseSharedCompilation", "false"))),
                arrayOf(OSType.MAC, Version(2, 1, 300), sequenceOf(CommandLineEnvironmentVariable("UseSharedCompilation", "false"))),
                arrayOf(OSType.WINDOWS, Version(2, 1, 200), emptySequence<CommandLineEnvironmentVariable>()),
                arrayOf(OSType.WINDOWS, Version(2, 1, 300), emptySequence<CommandLineEnvironmentVariable>()),
                arrayOf(OSType.WINDOWS, Version(2, 1, 105), emptySequence<CommandLineEnvironmentVariable>()),
                arrayOf(OSType.UNIX, Version(2, 1, 105), emptySequence<CommandLineEnvironmentVariable>()),
                arrayOf(OSType.MAC, Version(2, 1, 105), emptySequence<CommandLineEnvironmentVariable>()),
                arrayOf(OSType.WINDOWS, Version(2, 0, 0), emptySequence<CommandLineEnvironmentVariable>()),
                arrayOf(OSType.UNIX, Version(2, 0, 0), emptySequence<CommandLineEnvironmentVariable>()),
                arrayOf(OSType.MAC, Version(2, 0, 0), emptySequence<CommandLineEnvironmentVariable>()),
                arrayOf(OSType.WINDOWS, Version(1, 0, 1), emptySequence<CommandLineEnvironmentVariable>()),
                arrayOf(OSType.UNIX, Version(1, 0, 1), emptySequence<CommandLineEnvironmentVariable>()),
                arrayOf(OSType.MAC, Version(1, 0, 1), emptySequence<CommandLineEnvironmentVariable>())
        )
    }

    @Test(dataProvider = "testData")
    fun shouldGetVariables(
            os: OSType,
            version: Version,
            expectedVariables: Sequence<CommandLineEnvironmentVariable>) {
        // Given
        val ctx = Mockery()
        val environment = ctx.mock(Environment::class.java)
        val dotnetCliToolInfo = ctx.mock(DotnetCliToolInfo::class.java)
        val environmentVariables = EnvironmentVariablesImpl(environment, dotnetCliToolInfo)

        // When
        ctx.checking(object : Expectations() {
            init {
                oneOf<Environment>(environment).OS
                will(returnValue(os))

                oneOf<DotnetCliToolInfo>(dotnetCliToolInfo).Version
                will(returnValue(version))
            }
        })

        val actualVariables = environmentVariables.variables.toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.DefaultVariables + expectedVariables).toList())
    }
}