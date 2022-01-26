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

import jetbrains.buildServer.JsonParserImpl
import jetbrains.buildServer.dotnet.discovery.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class JsonProjectDeserializerTest {
    @DataProvider
    fun testDeserializeData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        "/project.json",
                        Solution(listOf(Project("projectPath", emptyList(), listOf(Framework("dnx451"), Framework("dnxcore50")), emptyList(), emptyList())))))
    }

    @Test(dataProvider = "testDeserializeData")
    fun shouldDeserialize(target: String, expectedSolution: Solution) {
        // Given
        val path = "projectPath"
        val streamFactory = StreamFactoryStub().add(path, this::class.java.getResourceAsStream(target))
        val deserializer = JsonProjectDeserializer(JsonParserImpl(), ReaderFactoryImpl())

        // When
        val actualSolution = deserializer.deserialize(path, streamFactory)

        // Then
        Assert.assertEquals(actualSolution, expectedSolution)
    }

    @DataProvider
    fun testAcceptData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("project.json", true),
                arrayOf("abc\\project.json", true),
                arrayOf("abc//project.json", true),
                arrayOf("abc//ProjecT.JsoN", true),
                arrayOf("aaaproject.json", false),
                arrayOf("project.jsonaaa", false),
                arrayOf("abc\\project.jsonsss", false),
                arrayOf("project.json10aaa", false),
                arrayOf("10project.json", false),
                arrayOf("10rer323project.json", false),
                arrayOf(".json", false),
                arrayOf("json", false),
                arrayOf("  ", false),
                arrayOf("", false))
    }

    @Test(dataProvider = "testAcceptData")
    fun shouldAccept(path: String, expectedAccepted: Boolean) {
        // Given
        val deserializer = JsonProjectDeserializer(JsonParserImpl(), ReaderFactoryImpl())

        // When
        val actualAccepted = deserializer.isAccepted(path)

        // Then
        Assert.assertEquals(actualAccepted, expectedAccepted)
    }
}