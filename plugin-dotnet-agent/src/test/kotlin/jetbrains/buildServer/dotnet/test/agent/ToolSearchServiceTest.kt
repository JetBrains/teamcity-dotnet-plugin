package jetbrains.buildServer.dotnet.test.agent

import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.ToolSearchService
import jetbrains.buildServer.agent.ToolSearchServiceImpl
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class ToolSearchServiceTest {
    private var _ctx: Mockery? = null
    private var _environment: Environment? = null
    private var _fileSystem: FileSystemService? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _environment = _ctx!!.mock(Environment::class.java)
        _fileSystem = _ctx!!.mock(FileSystemService::class.java)
    }

    @Test
    fun shouldFind() {
        // Given
        val target = "dotnet"
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<Environment>(_environment).tryGetVariable("TOOL_HOME")
                will(returnValue("home"))

                oneOf<Environment>(_environment).paths
                will(returnValue(sequenceOf(File("home1"), File("home2"))))

                oneOf<FileSystemService>(_fileSystem).list(File("home"))
                will(returnValue(emptySequence<File>()))

                oneOf<FileSystemService>(_fileSystem).list(File("home1"))
                will(returnValue(emptySequence<File>()))

                oneOf<FileSystemService>(_fileSystem).list(File("home2"))
                will(returnValue(sequenceOf(File("home2", "dotnet"))))
            }
        })
        val searchService = createInstance()

        // When
        val actualTools = searchService.find(target, "TOOL_HOME").toList()

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(actualTools, listOf(File("home2", "dotnet")))
    }

    @Test
    fun shouldFindWhenToolHomeOnly() {
        // Given
        val target = "dotnet"
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<Environment>(_environment).tryGetVariable("TOOL_HOME")
                will(returnValue("home"))

                oneOf<Environment>(_environment).paths
                will(returnValue(emptySequence<File>()))

                oneOf<FileSystemService>(_fileSystem).list(File("home", "bin"))
                will(returnValue(sequenceOf(File("home/bin", "dotnet"))))
            }
        })
        val searchService = createInstance()

        // When
        val actualTools = searchService.find(target, "TOOL_HOME") { File(it, "bin") }.toList()

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(actualTools, listOf(File("home/bin", "dotnet")))
    }

    @Test
    fun shouldFindWhenHasNoToolHome() {
        // Given
        val target = "dotnet"
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<Environment>(_environment).tryGetVariable("TOOL_HOME")
                will(returnValue(null))

                oneOf<Environment>(_environment).paths
                will(returnValue(sequenceOf(File("home1"), File("home2"))))

                oneOf<FileSystemService>(_fileSystem).list(File("home1"))
                will(returnValue(sequenceOf(File("home1", "dotnet"))))

                oneOf<FileSystemService>(_fileSystem).list(File("home2"))
                will(returnValue(sequenceOf(File("home2", "dotnet"))))
            }
        })
        val searchService = createInstance()

        // When
        val actualTools = searchService.find(target, "TOOL_HOME").toList()

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(actualTools, listOf(File("home1", "dotnet"), File("home2", "dotnet")))
    }

    private fun createInstance(): ToolSearchService = ToolSearchServiceImpl(_environment!!, _fileSystem!!)
}