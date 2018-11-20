package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.io.InputStreamReader

class SharedCompilationArgumentsProviderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _sharedCompilation: SharedCompilation

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _sharedCompilation= _ctx.mock(SharedCompilation::class.java)
    }

    @Test
    fun shouldProvideNodeReuseArgumentsWhenSharedCompilationRequiresSuppressing() {
        // Given
        val context = DotnetBuildContext(_ctx.mock(DotnetCommand::class.java), Verbosity.Detailed)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<SharedCompilation>(_sharedCompilation).requireSuppressing(context)
                will(returnValue(true))
            }
        })

        val actualArguments = createInstance().getArguments(context).toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualArguments, listOf(SharedCompilationArgumentsProvider.nodeReuseArgument))
    }

    @Test
    fun shouldNotProvideNodeReuseArgumentsWhenSharedCompilationDoesNotRequireSuppressing() {
        // Given
        val context = DotnetBuildContext(_ctx.mock(DotnetCommand::class.java), Verbosity.Detailed)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<SharedCompilation>(_sharedCompilation).requireSuppressing(context)
                will(returnValue(false))
            }
        })

        val actualArguments = createInstance().getArguments(context).toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualArguments, emptyList<CommandLineArgument>())
    }

    private fun createInstance(): ArgumentsProvider {
        return SharedCompilationArgumentsProvider(_sharedCompilation)
    }
}