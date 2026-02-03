

package jetbrains.buildServer

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.dotnet.NuGetPackageVersionParser
import jetbrains.buildServer.tools.ToolException
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersion
import jetbrains.buildServer.util.ArchiveUtil
import org.testng.Assert
import org.testng.annotations.BeforeTest
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ToolServiceImplTests {
    @MockK
    private lateinit var _packageVersionParserMock: NuGetPackageVersionParser

    @MockK
    private lateinit var _httpDownloaderMock: HttpDownloader

    @MockK
    private lateinit var _nuGetServiceMock: NuGetService

    @MockK
    private lateinit var _fileSystemServiceMock: FileSystemService

    @BeforeTest
    fun beforeTest() {
        clearAllMocks()
        MockKAnnotations.init(this)
    }

    @Test
    fun `should provide tools list`()
    {
        // arrange
        val packageId = "package"
        val toolTypeMock = mockk<ToolType>().also {
            every { it.type } answers { "packageType" }
        }
        val packageMock1 = mockk<NuGetPackage>().also { every { it.packageVersion } answers { "42" } }
        val packageMock2 = mockk<NuGetPackage>().also { every { it.packageVersion } answers { "43" } }
        every { _nuGetServiceMock.getPackagesById(any()) } answers { sequenceOf(packageMock1, packageMock2) }
        val toolService = createInstance()

        // act
        val result = toolService.getTools(toolTypeMock, packageId)

        // assert
        Assert.assertNotNull(result)
        Assert.assertEquals(result.size, 2)
        Assert.assertEquals(result[0].type, toolTypeMock)
        Assert.assertEquals(result[0].id, "packageType.43")
        Assert.assertEquals(result[1].type, toolTypeMock)
        Assert.assertEquals(result[1].id, "packageType.42")
    }

    @Test
    fun `should throws if cannot get packages list in NuGet`()
    {
        // arrange
        val packageId = "package"
        val toolTypeMock = mockk<ToolType>().also {
            every { it.displayName } answers { "Tool Display Name" }
        }
        val nugetException = Exception("nuget error")
        every { _nuGetServiceMock.getPackagesById(any()) } throws (Exception(nugetException))
        val toolService = createInstance()

        // act, assert
        Assert.assertThrows(ToolException::class.java) { toolService.getTools(toolTypeMock, packageId) }
    }

    @Test
    fun `should not resolve package version if package is not a file`() {
        // arrange
        val toolTypeMock = mockk<ToolType>()
        val toolPackageFileMock = mockk<File>().also {
            every { it.isFile } answers { false }
        }
        val toolService = createInstance()

        // act
        val result = toolService.tryGetPackageVersion(toolTypeMock, toolPackageFileMock, "")

        // assert
        Assert.assertNull(result)
    }

    @Test
    fun `should not resolve package version if package ile name doesn't match with any of package id's`() {
        // arrange
        val packageFileName = "package2"
        val toolTypeMock = mockk<ToolType>()
        val toolPackageFileMock = mockk<File>().also {
            every { it.isFile } answers { true }
            every { it.name } answers { packageFileName }
        }
        val toolService = createInstance()

        // act
        val result = toolService.tryGetPackageVersion(toolTypeMock, toolPackageFileMock, "package0", "package1")

        // assert
        Assert.assertNull(result)
    }

    @Test
    fun `should not resolve package version if package file extension is not allowed`() {
        // arrange
        val packageFileName = "package2.abc"
        val toolTypeMock = mockk<ToolType>()
        val toolPackageFileMock = mockk<File>().also {
            every { it.isFile } answers { true }
            every { it.name } answers { packageFileName }
        }
        val toolService = createInstance()

        // act
        val result = toolService.tryGetPackageVersion(toolTypeMock, toolPackageFileMock, "package1")

        // assert
        Assert.assertNull(result)
    }

    @Test
    fun `should not resolve package version if package version was not parsed`() {
        // arrange
        val packageId = "package"
        val packageFileName = "package.zip"
        val toolTypeMock = mockk<ToolType>()
        val toolPackageFileMock = mockk<File>().also {
            every { it.isFile } answers { true }
            every { it.name } answers { packageFileName }
        }
        every { _packageVersionParserMock.tryParse(any()) } answers { null }
        val toolService = createInstance()

        // act
        val result = toolService.tryGetPackageVersion(toolTypeMock, toolPackageFileMock, packageId)

        // assert
        Assert.assertNotNull(result)
        Assert.assertNull(result?.toolVersion)
        Assert.assertNotNull(result?.details)
    }

    @DataProvider
    fun getAllowedPackageFilesExtensions() = arrayOf(
        arrayOf(".nupkg"),
        arrayOf(".zip"),
        arrayOf(".tar.gz"),
    )

    @Test(dataProvider = "getAllowedPackageFilesExtensions")
    fun `should resolve package version from file`(packageFileExtension: String) {
        // arrange
        val packageId = "package"
        val packageFileName = "package.$packageFileExtension"
        val toolTypeMock = mockk<ToolType>().also {
            every { it.type } answers { "packageType" }
            every { it.displayName } answers { "package display name" }
        }
        val toolPackageFileMock = mockk<File>().also {
            every { it.isFile } answers { true }
            every { it.name } answers { packageFileName }
        }
        every { _packageVersionParserMock.tryParse(any()) } answers { mockk() }
        val toolService = createInstance()

        // act
        val result = toolService.tryGetPackageVersion(toolTypeMock, toolPackageFileMock, packageId)

        // assert
        Assert.assertNotNull(result)
        Assert.assertNotNull(result?.toolVersion)
        Assert.assertNull(result?.details)
    }

    @Test
    fun `should fetch tool package`()
    {
        // arrange
        val packageId = "package.42"
        val packageVersion = "42"
        val toolTypeMock = mockk<ToolType>().also {
            every { it.type } answers { "package" }
        }
        val toolVersionMock = mockk<ToolVersion>().also {
            every { it.version } answers { packageVersion }
            every { it.id } answers { packageId }
            every { it.displayName } answers { "" }
        }
        val targetDir = File("./")
        val packageMock1 = mockk<NuGetPackage>().also {
            every { it.packageVersion } answers { "42" }
            every { it.downloadUrl } answers { mockk() }
            every { it.packageId } answers { packageId }
        }
        val packageMock2 = mockk<NuGetPackage>().also { every { it.packageVersion } answers { "43" } }
        every { _nuGetServiceMock.getPackagesById(any()) } answers { sequenceOf(packageMock1, packageMock2) }
        justRun { _fileSystemServiceMock.write(any(), any()) }
        val toolService = createInstance()

        // act
        val result = toolService.fetchToolPackage(toolTypeMock, toolVersionMock, targetDir, packageId)

        // assert
        Assert.assertNotNull(result)
    }

    @Test
    fun `should not unpack invalid tool package`()
    {
        // arrange
        val packageId = "package"
        val toolPackageMock = mockk<File>().also {
            every { it.isFile } answers { true }
            every { it.name } answers { packageId }
        }
        val targetDirMock = File("./")
        val packageMock1 = mockk<NuGetPackage>().also {
            every { it.packageVersion } answers { "42" }
            every { it.downloadUrl } answers { mockk() }
            every { it.packageId } answers { packageId }
        }
        val packageMock2 = mockk<NuGetPackage>().also { every { it.packageVersion } answers { "43" } }
        every { _nuGetServiceMock.getPackagesById(any()) } answers { sequenceOf(packageMock1, packageMock2) }
        justRun { _fileSystemServiceMock.write(any(), any()) }
        mockkStatic(ArchiveUtil::class)
        every { ArchiveUtil.unpackZip(any(), any(), any()) } answers { true }
        val toolService = createInstance()

        // act
        toolService.unpackToolPackage(toolPackageMock, "./", targetDirMock, packageId)

        // assert
        verify (exactly = 0) { ArchiveUtil.unpackZip(any(), any(), any()) }
    }

    @Test
    fun `should throw exception while unpacking invalid tool package content`()
    {
        // arrange
        val packageId = "package"
        val toolPackageMock = mockk<File>().also {
            every { it.isFile } answers { true }
            every { it.name } answers { packageId + ".zip" }
        }
        val targetDirMock = File("./")
        mockkStatic(ArchiveUtil::class)
        every { ArchiveUtil.unpackZip(any(), any(), any()) } answers { false }
        val toolService = createInstance()

        // act, assert
        Assert.assertThrows { toolService.unpackToolPackage(toolPackageMock, "./", targetDirMock, packageId) }
        verify (exactly = 1) { ArchiveUtil.unpackZip(any(), any(), any()) }
    }

    @Test(dataProvider = "getAllowedPackageFilesExtensions")
    fun `should unpack valid tool package`(packageFileExtension: String)
    {
        // arrange
        val packageId = "package"
        val toolPackageMock = mockk<File>().also {
            every { it.isFile } answers { true }
            every { it.name } answers { packageId + "." + packageFileExtension }
        }
        val targetDirMock = File("./")
        mockkStatic(ArchiveUtil::class)
        every { ArchiveUtil.unpackZip(any(), any(), any()) } answers { true }
        val toolService = createInstance()

        // act
        toolService.unpackToolPackage(toolPackageMock, "./", targetDirMock, packageId)

        // assert
        verify (exactly = 1) { ArchiveUtil.unpackZip(any(), any(), any()) }
    }

    private fun createInstance() = ToolServiceImpl(_packageVersionParserMock, _httpDownloaderMock, _nuGetServiceMock, _fileSystemServiceMock)
}