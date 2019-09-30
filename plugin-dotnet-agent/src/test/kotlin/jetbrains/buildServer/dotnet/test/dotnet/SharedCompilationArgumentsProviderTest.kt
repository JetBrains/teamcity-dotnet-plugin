package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

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
        val context = DotnetBuildContext(ToolPath(Path("wd")), _ctx.mock(DotnetCommand::class.java), Version(1, 2), Verbosity.Detailed)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<SharedCompilation>(_sharedCompilation).requireSuppressing(Version(1, 2))
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
        val context = DotnetBuildContext(ToolPath(Path("wd")), _ctx.mock(DotnetCommand::class.java), Version(1,2,3), Verbosity.Detailed)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<SharedCompilation>(_sharedCompilation).requireSuppressing(Version(1,2,3))
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