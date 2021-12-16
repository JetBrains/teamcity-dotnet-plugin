package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.HttpDownloaderImpl
import jetbrains.buildServer.NuGetServiceImpl
import org.testng.Assert
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream

class NuGetServiceIntegrationTest {
    @Test
    fun shouldGetPackages() {
        // Given
        val downloader = HttpDownloaderImpl()
        val nuget = NuGetServiceImpl(downloader)

        // When
        val packages = nuget.getPackagesById("IoC.Container").toList()
        val downloadUrl = packages.last().downloadUrl
        var data: ByteArray? = null
        ByteArrayOutputStream().use { stream ->
            downloader.download(downloadUrl, stream)
            data = stream.toByteArray()
        }

        // Then
        Assert.assertTrue(packages.size >= 3)
        Assert.assertTrue(data!!.size >= 10000)
    }
}