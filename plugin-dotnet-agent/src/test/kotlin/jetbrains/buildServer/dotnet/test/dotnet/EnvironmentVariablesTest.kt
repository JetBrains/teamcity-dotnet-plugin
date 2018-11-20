package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.runner.TargetRegistry
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.util.OSType
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class EnvironmentVariablesTest {
    private lateinit var _ctx: Mockery
    private lateinit var _environment: Environment
    private lateinit var _sharedCompilation: SharedCompilation

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _environment = _ctx.mock<Environment>(Environment::class.java)
        _sharedCompilation = _ctx.mock<SharedCompilation>(SharedCompilation::class.java)
    }

    @Test
    fun shouldProvideDefaultVars() {
        // Given
        val context = DotnetBuildContext(_ctx.mock(DotnetCommand::class.java), null, emptySet())
        val environmentVariables = EnvironmentVariablesImpl(_environment, _sharedCompilation)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<Environment>(_environment).os
                will(returnValue(OSType.WINDOWS))

                oneOf<Environment>(_environment).tryGetVariable("USERPROFILE")
                will(returnValue("path"))

                oneOf<SharedCompilation>(_sharedCompilation).requireSuppressing(context)
                will(returnValue(false))
            }
        })

        val actualVariables = environmentVariables.getVariables(context).toList()

        // Then
        Assert.assertEquals(actualVariables, EnvironmentVariablesImpl.defaultVariables.toList())
    }

    @Test
    fun shouldUseSharedCompilation() {
        // Given
        val context = DotnetBuildContext(_ctx.mock(DotnetCommand::class.java), null, emptySet())
        val environmentVariables = EnvironmentVariablesImpl(_environment, _sharedCompilation)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<Environment>(_environment).os
                will(returnValue(OSType.WINDOWS))

                oneOf<Environment>(_environment).tryGetVariable("USERPROFILE")
                will(returnValue("path"))

                oneOf<SharedCompilation>(_sharedCompilation).requireSuppressing(context)
                will(returnValue(true))
            }
        })

        val actualVariables = environmentVariables.getVariables(context).toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.defaultVariables + sequenceOf(EnvironmentVariablesImpl.useSharedCompilationEnvironmentVariable)).toList())
    }
}