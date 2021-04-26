package jetbrains.buildServer.dotnet.test.inspect

import jetbrains.buildServer.E
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.inspect.FilePluginSource
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class FilePluginSourceTest {
    @DataProvider(name = "getPluginCases")
    fun getPluginCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        "MyFile",
                        VirtualFileSystemService().addFile(File("MyFile")),
                        E("File").a("Path", File("MyFile").canonicalFile.absolutePath)
                ),
                arrayOf(
                        "MyFile",
                        VirtualFileSystemService().addFile(File("MyFile2")),
                        E("File")
                ),
                arrayOf(
                        "MyFile",
                        VirtualFileSystemService().addDirectory(File("MyFile")),
                        E("File")
                ),
                arrayOf(
                        "MyFile",
                        VirtualFileSystemService(),
                        E("File")
                )
        )
    }

    @Test(dataProvider = "getPluginCases")
    fun shouldGetPlugin(specification: String, fileSystem: FileSystemService, expectedPlugin: E) {
        // Given
        val source = createInstance(fileSystem)

        // When
        val aclualPlugin = source.getPlugin(specification)

        // Then
        Assert.assertEquals(aclualPlugin, expectedPlugin)
    }

    private fun createInstance(fileSystem: FileSystemService) =
            FilePluginSource(fileSystem)
}