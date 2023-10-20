package jetbrains.buildServer.dotCover

import jetbrains.buildServer.XmlDocumentService
import jetbrains.buildServer.XmlDocumentServiceImpl
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class DotCoverToolProviderAdapterTest {

    private lateinit var _xmlDocumentService: XmlDocumentService

    @BeforeMethod
    fun setUp() {
        _xmlDocumentService = XmlDocumentServiceImpl()
    }

    @Test
    fun deserialize_bundledDotCoverNuSpecFile_shouldParseIdAndVersion() {
        // arrange
        val source = DotCoverToolProviderAdapterTest::class.java.getResource("JetBrains.dotCover.CommandLineTools.nuspec")!!.openStream()

        // act
        val result = _xmlDocumentService.deserialize(source)
        val bundledPackageId = DotCoverToolProviderAdapter.getContents(result, "/package/metadata/id").firstOrNull()
        val bundledPackageVersion = DotCoverToolProviderAdapter.getContents(result, "/package/metadata/version").firstOrNull()

        // assert
        Assert.assertNotNull(bundledPackageId)
        Assert.assertNotNull(bundledPackageVersion)
        Assert.assertEquals(bundledPackageId, "JetBrains.dotCover.CommandLineTools")
        Assert.assertEquals(bundledPackageVersion, "2023.3.0-tc03")
    }
}