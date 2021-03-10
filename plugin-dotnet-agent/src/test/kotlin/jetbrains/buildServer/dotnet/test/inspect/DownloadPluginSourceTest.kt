package jetbrains.buildServer.dotnet.test.inspect

import jetbrains.buildServer.E
import jetbrains.buildServer.inspect.DownloadPluginSource
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DownloadPluginSourceTest {
    @DataProvider(name = "getPluginCases")
    fun getPluginCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf("Abc/123", E("Download").a("Id", "Abc").a("Version", "123")),
                arrayOf("Abc", E("Download")),
                arrayOf("Abc/123/345", E("Download")),
                arrayOf("", E("Download")),
                arrayOf("  ", E("Download"))
        )
    }

    @Test(dataProvider = "getPluginCases")
    fun shouldGetPlugin(specification: String, expectedPlugin: E) {
        // Given
        val source = createInstance()

        // When
        val aclualPlugin = source.getPlugin(specification)

        // Then
        Assert.assertEquals(aclualPlugin, expectedPlugin)
    }

    private fun createInstance() =
            DownloadPluginSource()
}