/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.discovery.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MSBuildSolutionDeserializerTest {
    @Test
    fun shouldDeserialize() {
        // Given
        val target = "/solution.sln"
        val path = "projectPath/aaa.sln"
        val streamFactory = StreamFactoryStub().add(path, this::class.java.getResourceAsStream(target))
        val ctx = Mockery()
        val msBuildProjectDeserializer = ctx.mock(SolutionDeserializer::class.java)

        val solution1 = Solution(listOf(Project("projectPath1", listOf(Configuration("Core"), Configuration("Release")), listOf(Framework("Netcoreapp2.0"), Framework("netcoreapp1.0")), listOf(Runtime("win7-x64"), Runtime("win-7x86"), Runtime("ubuntu.16.10-x64")), listOf(Reference("Microsoft.NET.Sdk")))))
        val solution2 = Solution(listOf(Project("projectPath2", listOf(Configuration("core")), listOf(Framework("netcoreapp2.0")), listOf(Runtime("win7-x64"), Runtime("win-7x86")), listOf(Reference("Microsoft.NET.sdk"), Reference("Microsoft.NET.test.sdk")))))
        val expectedSolution = Solution(solution1.projects.plus(solution2.projects), path)

        ctx.checking(object : Expectations() {
            init {
                oneOf<SolutionDeserializer>(msBuildProjectDeserializer).isAccepted("projectPath/proj1.csproj")
                will(returnValue(true))

                oneOf<SolutionDeserializer>(msBuildProjectDeserializer).deserialize("projectPath/proj1.csproj", streamFactory)
                will(returnValue(solution1))

                oneOf<SolutionDeserializer>(msBuildProjectDeserializer).isAccepted("projectPath/dir2/proj2.csproj")
                will(returnValue(true))

                oneOf<SolutionDeserializer>(msBuildProjectDeserializer).deserialize("projectPath/dir2/proj2.csproj", streamFactory)
                will(returnValue(solution2))

                oneOf<SolutionDeserializer>(msBuildProjectDeserializer).isAccepted("projectPath/Solution Items")
                will(returnValue(false))
            }
        })

        val deserializer = MSBuildSolutionDeserializer(ReaderFactoryImpl(), msBuildProjectDeserializer)

        // When
        val actualSolution = deserializer.deserialize(path, streamFactory)

        // Then
        Assert.assertEquals(actualSolution, expectedSolution)
    }

    @DataProvider
    fun testAcceptData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("abc.sln", true),
                arrayOf("abc.sLn", true),
                arrayOf("abc122.sLn", true),
                arrayOf("ab c.sLn", true),
                arrayOf("abcPsln", false),
                arrayOf("abc.", false),
                arrayOf("abc", false),
                arrayOf("abc.proj", false),
                arrayOf(".sln", false),
                arrayOf("sln", false),
                arrayOf("  ", false),
                arrayOf("", false))
    }

    @Test(dataProvider = "testAcceptData")
    fun shouldAccept(path: String, expectedAccepted: Boolean) {
        // Given
        val ctx = Mockery()
        val msBuildProjectDeserializer = ctx.mock(SolutionDeserializer::class.java)
        val deserializer = MSBuildSolutionDeserializer(ReaderFactoryImpl(), msBuildProjectDeserializer)

        // When
        val actualAccepted = deserializer.isAccepted(path)

        // Then
        Assert.assertEquals(actualAccepted, expectedAccepted)
    }

    @DataProvider
    fun testNormalizePathData(): Array<Array<String>> {
        return arrayOf(
                arrayOf("dir/abc.sln", "my.proj", "dir/my.proj"),
                arrayOf("Dir/abc.sln", "MY.proj", "Dir/MY.proj"),
                arrayOf("dir\\abc.sln", "my.proj", "dir/my.proj"),
                arrayOf("dir", "my.proj", "my.proj"),
                arrayOf("dir/abc.sln", "dir2/my.proj", "dir/dir2/my.proj"),
                arrayOf("dir\\abc.sln", "dir2\\my.proj", "dir/dir2/my.proj"))
    }

    @Test(dataProvider = "testNormalizePathData")
    fun shouldNormalizePath(basePath: String, path: String, expectedPath: String) {
        // Given
        val ctx = Mockery()
        val msBuildProjectDeserializer = ctx.mock(SolutionDeserializer::class.java)
        val deserializer = MSBuildSolutionDeserializer(ReaderFactoryImpl(), msBuildProjectDeserializer)

        // When
        val actualPath = deserializer.normalizePath(basePath, path)

        // Then
        Assert.assertEquals(actualPath, expectedPath)
    }
}