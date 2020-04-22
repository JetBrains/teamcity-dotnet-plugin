package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.MSBuildRegistryAgentPropertiesProvider
import jetbrains.buildServer.dotnet.MSBuildValidator
import jetbrains.buildServer.dotnet.MSBuildValidatorImpl
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.dotnet.test.agent.WindowsRegistryTest
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildValidatorTest {
    @MockK private lateinit var _fileSystemService: FileSystemService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testValidateData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File(File("msbuildPath"), "MSBuild.exe")),
                        File("msbuildPath"),
                        true
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File("msbuildPath")),
                        File("msbuildPath"),
                        false
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File(File("msbuildPathAbc"), "MSBuild.exe")),
                        File("msbuildPath"),
                        false
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File(File("msbuildPath"), "MSBuild")),
                        File("msbuildPath"),
                        false
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File(File("msbuildPath"), "MSBuild2.exe")),
                        File("msbuildPath"),
                        false
                ),
                arrayOf(
                        VirtualFileSystemService(),
                        File("msbuildPath"),
                        false
                )
        )
    }


    @Test(dataProvider = "testValidateData")
    fun shouldValidate(fileSystemService: FileSystemService, msbuildPath: File, expectedResult: Boolean) {
        // Given
        val validator = createInstance(fileSystemService)

        // When
        val actulResult = validator.isValide(msbuildPath)

        // Then
        Assert.assertEquals(actulResult, expectedResult)
    }

    private fun createInstance(fileSystemService: FileSystemService) = MSBuildValidatorImpl(fileSystemService)
}