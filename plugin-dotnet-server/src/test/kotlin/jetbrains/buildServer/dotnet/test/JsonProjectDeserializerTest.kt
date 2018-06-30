package jetbrains.buildServer.dotnet.test

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
        val deserializer = JsonProjectDeserializer(ReaderFactoryImpl())

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
        val deserializer = JsonProjectDeserializer(ReaderFactoryImpl())

        // When
        val actualAccepted = deserializer.accept(path)

        // Then
        Assert.assertEquals(actualAccepted, expectedAccepted)
    }
}