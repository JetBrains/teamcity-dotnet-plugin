package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.discovery.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File
import java.io.FileInputStream

class SolutionDiscoverTest {
    @Test
    fun shouldDiscover() {
        // Given
        val path1 = "projectPath1/aaa.sln";
        val path2 = "projectPath2/proj.sln";

        val streamFactory = StreamFactoryStub()
        val ctx = Mockery()
        val deserializer1 = ctx.mock(SolutionDeserializer::class.java, "deserializer1")
        val deserializer2 = ctx.mock(SolutionDeserializer::class.java, "deserializer2")
        val solution1 = Solution(listOf(Project("projectPath1", listOf(Configuration("Core"), Configuration("Release")), listOf(Framework("Netcoreapp2.0"), Framework("netcoreapp1.0")), listOf(Runtime("win7-x64"), Runtime("win-7x86"), Runtime("ubuntu.16.10-x64")), listOf(Reference("Microsoft.NET.Sdk")))))
        val solution2 = Solution(listOf(Project("projectPath2", listOf(Configuration("core")), listOf(Framework("netcoreapp2.0")), listOf(Runtime("win7-x64"), Runtime("win-7x86")), listOf(Reference("Microsoft.NET.sdk"), Reference("Microsoft.NET.test.sdk")))))

        ctx.checking(object : Expectations() {
            init {
                oneOf<SolutionDeserializer>(deserializer1).accept(path1)
                will(returnValue(true))

                oneOf<SolutionDeserializer>(deserializer1).accept(path2)
                will(returnValue(false))

                oneOf<SolutionDeserializer>(deserializer1).deserialize(path1, streamFactory)
                will(returnValue(solution1))

                oneOf<SolutionDeserializer>(deserializer2).accept(path1)
                will(returnValue(false))

                oneOf<SolutionDeserializer>(deserializer2).accept(path2)
                will(returnValue(true))

                oneOf<SolutionDeserializer>(deserializer2).deserialize(path2, streamFactory)
                will(returnValue(solution2))
            }
        })

        val discover = SolutionDiscoverImpl(listOf(deserializer1, deserializer2))

        // When
        val actualSolutions = discover.discover(streamFactory, sequenceOf(path1, path2)).toList()

        // Then
        Assert.assertEquals(actualSolutions, listOf(solution1, solution2))
    }
}