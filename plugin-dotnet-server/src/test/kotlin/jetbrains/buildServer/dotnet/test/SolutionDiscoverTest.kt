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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class SolutionDiscoverTest {
    private val mainThreadSurrogate = newSingleThreadContext("Main thread")

    @BeforeClass
    fun setUpClass() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @AfterClass
    fun tearDownClass() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun shouldDiscover() {
        // Given
        val path1 = "projectPath1/aaa.sln"
        val path2 = "projectPath2/proj.sln"

        val streamFactory = StreamFactoryStub()
        val ctx = Mockery()
        val deserializer1 = ctx.mock(SolutionDeserializer::class.java, "deserializer1")
        val deserializer2 = ctx.mock(SolutionDeserializer::class.java, "deserializer2")
        val solution1 = Solution(listOf(Project("projectPath1", listOf(Configuration("Core"), Configuration("Release")), listOf(Framework("Netcoreapp2.0"), Framework("netcoreapp1.0")), listOf(Runtime("win7-x64"), Runtime("win-7x86"), Runtime("ubuntu.16.10-x64")), listOf(Reference("Microsoft.NET.Sdk")))))
        val solution2 = Solution(listOf(Project("projectPath2", listOf(Configuration("core")), listOf(Framework("netcoreapp2.0")), listOf(Runtime("win7-x64"), Runtime("win-7x86")), listOf(Reference("Microsoft.NET.sdk"), Reference("Microsoft.NET.test.sdk")))))

        ctx.checking(object : Expectations() {
            init {
                oneOf<SolutionDeserializer>(deserializer1).isAccepted(path1)
                will(returnValue(true))

                oneOf<SolutionDeserializer>(deserializer1).isAccepted(path2)
                will(returnValue(false))

                oneOf<SolutionDeserializer>(deserializer1).deserialize(path1, streamFactory)
                will(returnValue(solution1))

                oneOf<SolutionDeserializer>(deserializer2).isAccepted(path1)
                will(returnValue(false))

                oneOf<SolutionDeserializer>(deserializer2).isAccepted(path2)
                will(returnValue(true))

                oneOf<SolutionDeserializer>(deserializer2).deserialize(path2, streamFactory)
                will(returnValue(solution2))
            }
        })

        val discover = SolutionDiscoverImpl(Dispatchers.Main, listOf(deserializer1, deserializer2))

        // When
        val actualSolutions = discover.discover(streamFactory, sequenceOf(path1, path2)).toList()

        // Then
        Assert.assertEquals(actualSolutions, listOf(solution1, solution2))
    }
}