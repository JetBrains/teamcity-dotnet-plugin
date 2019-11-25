package jetbrains.buildServer.dotnet.test.agent

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolSearchService
import jetbrains.buildServer.agent.ToolSearchServiceImpl
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ToolSearchServiceTest {
    @MockK private lateinit var _fileSystem: FileSystemService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(File("home2", "dotnet"), true),
                arrayOf(File("home2", "dotnet.exe"), true),
                arrayOf(File("home1", "dotnet"), false),
                arrayOf(File("home1", "dotnet.exe"), false),
                arrayOf(File("home1", "dotnet.a"), false),
                arrayOf(File("home2", "dotnet.exe"), true),
                arrayOf(File("home2", "abc.exe"), false),
                arrayOf(File("home2", "Dotnet.exe"), false),
                arrayOf(File("home2", "_dotnet.exe"), false),
                arrayOf(File("home2", "abc_dotnet.exe"), false),
                arrayOf(File("home2", "dotnet.exea"), false),
                arrayOf(File("home2", "dotnet.a"), false),
                arrayOf(File("home2", "dotneta"), false)
        )
    }

    @Test(dataProvider = "testData")
    fun shouldFind(executable: File, success: Boolean) {
        // Given
        val target = "dotnet"
        every { _fileSystem.list(File("home1")) } returns emptySequence()
        every { _fileSystem.list(File("home2")) } returns sequenceOf(executable)

        val searchService = createInstance()

        // When
        val actualTools = searchService.find(target, sequenceOf(Path(File(executable.path).parent))).toList()

        // Then
        Assert.assertEquals(actualTools.contains(executable), success)
    }

    private fun createInstance(): ToolSearchService = ToolSearchServiceImpl(_fileSystem)
}