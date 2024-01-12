

package jetbrains.buildServer.dotnet.test.agent

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ToolSearchServiceTest {
    @MockK private lateinit var _fileSystem: FileSystemService
    @MockK private lateinit var _environment: Environment

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(File("home2", "dotnet"), OSType.UNIX, true),
                arrayOf(File("home2", "dotnet"), OSType.MAC, true),
                arrayOf(File("home2", "dotnet"), OSType.WINDOWS, false),
                arrayOf(File("home2", "dotnet.exe"), OSType.WINDOWS, true),
                arrayOf(File("home2", "dotnet.exe"), OSType.UNIX, false),
                arrayOf(File("home2", "dotnet.exe"), OSType.MAC, false),
                arrayOf(File("home1", "dotnet"), OSType.UNIX, false),
                arrayOf(File("home1", "dotnet.exe"), OSType.WINDOWS, false),
                arrayOf(File("home1", "dotnet.a"), OSType.WINDOWS, false),
                arrayOf(File("home2", "dotnet.exe"), OSType.WINDOWS, true),
                arrayOf(File("home2", "abc.exe"), OSType.WINDOWS, false),
                arrayOf(File("home2", "Dotnet.exe"), OSType.WINDOWS, false),
                arrayOf(File("home2", "_dotnet.exe"), OSType.WINDOWS, false),
                arrayOf(File("home2", "abc_dotnet.exe"), OSType.WINDOWS, false),
                arrayOf(File("home2", "dotnet.exea"), OSType.WINDOWS, false),
                arrayOf(File("home2", "dotnet.a"), OSType.WINDOWS, false),
                arrayOf(File("home2", "dotneta"), OSType.WINDOWS, false)
        )
    }

    @Test(dataProvider = "testData")
    fun shouldFind(executable: File, os: OSType, success: Boolean) {
        // Given
        val target = "dotnet"
        every { _environment.os } returns os
        every { _fileSystem.list(File("home1")) } returns emptySequence()
        every { _fileSystem.list(File("home2")) } returns sequenceOf(executable)

        val searchService = createInstance()

        // When
        val actualTools = searchService.find(target, sequenceOf(Path(File(executable.path).parent))).toList()

        // Then
        Assert.assertEquals(actualTools.contains(executable), success)
    }

    private fun createInstance(): ToolSearchService = ToolSearchServiceImpl(_fileSystem, _environment)
}