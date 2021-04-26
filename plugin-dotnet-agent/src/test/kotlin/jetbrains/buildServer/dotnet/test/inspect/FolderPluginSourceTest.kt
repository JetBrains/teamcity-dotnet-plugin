package jetbrains.buildServer.dotnet.test.inspect

import jetbrains.buildServer.E
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.inspect.FolderPluginSource
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class FolderPluginSourceTest {
    @DataProvider(name = "getPluginCases")
    fun getPluginCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        "MyFolder",
                        VirtualFileSystemService().addDirectory(File("MyFolder")),
                        E("Folder").a("Path", File("MyFolder").canonicalFile.absolutePath)
                ),
                arrayOf(
                        "MyFolder",
                        VirtualFileSystemService().addDirectory(File("MyFolder2")),
                        E("Folder")
                ),
                arrayOf(
                        "MyFolder",
                        VirtualFileSystemService().addFile(File("MyFolder")),
                        E("Folder")
                ),
                arrayOf(
                        "MyFolder",
                        VirtualFileSystemService(),
                        E("Folder")
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
            FolderPluginSource(fileSystem)
}