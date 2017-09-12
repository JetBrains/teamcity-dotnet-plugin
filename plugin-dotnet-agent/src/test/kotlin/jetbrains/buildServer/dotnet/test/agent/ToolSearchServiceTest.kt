package jetbrains.buildServer.dotnet.test.agent

import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.ToolSearchService
import jetbrains.buildServer.agent.ToolSearchServiceImpl
import jetbrains.buildServer.agent.PathMatcher
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class ToolSearchServiceTest {
    private var _ctx: Mockery? = null
    private var _environment: Environment? = null
    private var _pathMatcher: PathMatcher? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _environment = _ctx!!.mock(Environment::class.java)
        _pathMatcher = _ctx!!.mock(PathMatcher::class.java)
    }

    @Test
    fun shouldFind() {
        // Given
        val targets = sequenceOf("dotnet.exe", "dotnet")
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<Environment>(_environment).tryGetVariable("TOOL_HOME")
                will(returnValue("home"))

                oneOf<Environment>(_environment).paths
                will(returnValue(sequenceOf(File("home1"), File("home2"))))

                oneOf<PathMatcher>(_pathMatcher).match(File("home", "bin"), targets, emptySequence())
                will(returnValue(sequenceOf(File("tool"), File("tool0"))))

                oneOf<PathMatcher>(_pathMatcher).match(File("home1", "bin"), targets, emptySequence())
                will(returnValue(sequenceOf(File("tool1"), File("tool2"))))

                oneOf<PathMatcher>(_pathMatcher).match(File("home2", "bin"), targets, emptySequence())
                will(returnValue(sequenceOf(File("tool3"), File("tool4"))))
            }
        })
        val searchService = createInstance()

        // When
        val actualTools = searchService.find("TOOL_HOME", targets) { File(it, "bin") }.toList()

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(actualTools, listOf(File("tool"), File("tool0"), File("tool1"), File("tool2"), File("tool3"), File("tool4")))
    }

    @Test
    fun shouldFindWhenToolHomeOnly() {
        // Given
        val targets = sequenceOf("dotnet.exe", "dotnet")
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<Environment>(_environment).tryGetVariable("TOOL_HOME")
                will(returnValue("home"))

                oneOf<Environment>(_environment).paths
                will(returnValue(emptySequence<File>()))

                oneOf<PathMatcher>(_pathMatcher).match(File("home", "bin"), targets, emptySequence())
                will(returnValue(sequenceOf(File("tool"), File("tool0"))))
            }
        })
        val searchService = createInstance()

        // When
        val actualTools = searchService.find("TOOL_HOME", targets) { File(it, "bin") }.toList()

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(actualTools, listOf(File("tool"), File("tool0")))
    }

    @Test
    fun shouldFindWhenHasNoToolHome() {
        // Given
        val targets = sequenceOf("dotnet.exe", "dotnet")
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<Environment>(_environment).tryGetVariable("TOOL_HOME")
                will(returnValue(null))

                oneOf<Environment>(_environment).paths
                will(returnValue(sequenceOf(File("home1"), File("home2"))))

                oneOf<PathMatcher>(_pathMatcher).match(File("home1", "bin"), targets, emptySequence())
                will(returnValue(sequenceOf(File("tool1"), File("tool2"))))

                oneOf<PathMatcher>(_pathMatcher).match(File("home2", "bin"), targets, emptySequence())
                will(returnValue(sequenceOf(File("tool3"), File("tool4"))))
            }
        })
        val searchService = createInstance()

        // When
        val actualTools = searchService.find("TOOL_HOME", targets) { File(it, "bin") }.toList()

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(actualTools, listOf(File("tool1"), File("tool2"), File("tool3"), File("tool4")))
    }

    private fun createInstance(): ToolSearchService =
            ToolSearchServiceImpl(
                    _environment!!,
                    _pathMatcher!!)
}