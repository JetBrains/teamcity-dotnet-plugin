package jetbrains.buildServer.dotnet.test

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.XmlDocumentServiceImpl
import jetbrains.buildServer.dotnet.discovery.MSBuildProjectDeserializer
import jetbrains.buildServer.dotnet.discovery.Project
import jetbrains.buildServer.dotnet.discovery.SlnxSolutionDeserializer
import jetbrains.buildServer.dotnet.discovery.Solution
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class SlnxSolutionDeserializerTest {

    @Test
    fun shouldDeserialize() {
        // given
        val rootPath = "path/to/"
        val solutionPath = rootPath + "solution.slnx"
        val p1Path = rootPath + "my-app/my-app.csproj"
        val p2Path = rootPath + "my-lib/my-lib.csproj"
        val solution = """
            <Solution>
              <Project Path="${p1Path.removePrefix(rootPath)}" />
              <Project Path="${p2Path.removePrefix(rootPath)}" />
            </Solution>
        """.trimIndent()
        val streamFactory = StreamFactoryStub().add(solutionPath, solution.byteInputStream())
        val projectDeserializer = mockk<MSBuildProjectDeserializer> {
            every { isAccepted(p1Path) } returns true
            every { deserialize(p1Path, streamFactory) } returns Solution(listOf(Project(p1Path)))
            every { isAccepted(p2Path) } returns true
            every { deserialize(p2Path, streamFactory) } returns Solution(listOf(Project(p2Path)))
        }
        val xmlService = XmlDocumentServiceImpl()
        val deserializer = SlnxSolutionDeserializer(xmlService, projectDeserializer)

        // when
        val res = deserializer.deserialize(solutionPath, streamFactory)

        // then
        Assert.assertEquals(res.solution, solutionPath)
        Assert.assertEquals(res.projects.size, 2)
    }

    @DataProvider
    fun testAcceptData(): Array<Array<Any>> = arrayOf(
        arrayOf("abc.slnx", true),
        arrayOf("abc.sln", false),
        arrayOf("slnx", false),
        arrayOf("abc.", false),
        arrayOf("abc", false),
        arrayOf("abc.proj", false),
        arrayOf(".slnx", false),
        arrayOf("  ", false),
        arrayOf("", false)
    )

    @Test(dataProvider = "testAcceptData")
    fun shouldAccept(path: String, expectedAccepted: Boolean) {
        // given
        val xmlService = XmlDocumentServiceImpl()
        val projectDeserializer = mockk<MSBuildProjectDeserializer>()
        val deserializer = SlnxSolutionDeserializer(xmlService, projectDeserializer)

        // when, then
        Assert.assertEquals(deserializer.isAccepted(path), expectedAccepted)
    }
}