package jetbrains.buildServer.dotnet.test.mono

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.test.agent.ToolSearchServiceStub
import jetbrains.buildServer.mono.MonoToolProvider
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MonoToolProviderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _toolProvidersRegistry: ToolProvidersRegistry
    private lateinit var _toolSearchService: ToolSearchService

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _toolProvidersRegistry = _ctx.mock(ToolProvidersRegistry::class.java)
        _toolSearchService = _ctx.mock(ToolSearchService::class.java)
    }

    @DataProvider
    fun supportToolCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("mono", true),
                arrayOf("MoNo", true),
                arrayOf("MONO", true),
                arrayOf("MONO2", false),
                arrayOf("abc", false),
                arrayOf(" mono ", false),
                arrayOf("   ", false),
                arrayOf("", false))
    }

    @Test(dataProvider = "supportToolCases")
    fun shouldSupportTool(toolName: String, expectedResult: Boolean) {
        // Given
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ToolProvidersRegistry>(_toolProvidersRegistry).registerToolProvider(with(any(ToolProvider::class.java)))
            }
        })
        val toolProvider = createInstance(emptySequence())

        // When
        val actualResult = toolProvider.supports(toolName)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualResult, expectedResult)
    }

    @DataProvider
    fun getPathCases(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf(
                        sequenceOf(File("path1"), File("path1")),
                        File("path1").absoluteFile,
                        false),
                arrayOf(
                        sequenceOf(File("path2")),
                        File("path2").absoluteFile,
                        false),
                arrayOf(
                        emptySequence<File>(),
                        File("a").absoluteFile,
                        true))
    }

    @Test(dataProvider = "getPathCases")
    fun shouldGetPath(
            files: Sequence<File>,
            expectedPath: File,
            expectedToolCannotBeFoundException: Boolean) {
        // Given
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ToolProvidersRegistry>(_toolProvidersRegistry).registerToolProvider(with(any(ToolProvider::class.java)))
            }
        })
        val toolProvider = createInstance(files)

        var actualToolCannotBeFoundException = false

        // When
        var actualPath = ""
        try {
            actualPath = toolProvider.getPath("tool")
        } catch (ex: ToolCannotBeFoundException) {
            actualToolCannotBeFoundException = true
        }


        // Then
        if (!expectedToolCannotBeFoundException) {
            Assert.assertEquals(actualPath, expectedPath.absolutePath)
        }

        Assert.assertEquals(actualToolCannotBeFoundException, expectedToolCannotBeFoundException)
    }

    @Test
    fun shouldNotSearchToolInVirtualContext() {
        val build = _ctx.mock(AgentRunningBuild::class.java)
        val context = _ctx.mock(BuildRunnerContext::class.java)
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ToolProvidersRegistry>(_toolProvidersRegistry).registerToolProvider(with(any(MonoToolProvider::class.java)))

                allowing(context).isVirtualContext
                will(returnValue(true))
            }
        })

        val toolProvider = createInstance(emptySequence())
        val path = toolProvider.getPath("mono", build, context)

        Assert.assertEquals(path, "mono")
    }

    private fun createInstance(files: Sequence<File>): ToolProvider =
            MonoToolProvider(
                    _toolProvidersRegistry,
                    ToolSearchServiceStub(files))
}