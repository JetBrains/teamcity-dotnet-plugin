package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.MSBuildValidatorImpl
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildValidatorTest {
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
        val actualResult = validator.isValid(msbuildPath)

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }

    private fun createInstance(fileSystemService: FileSystemService) = MSBuildValidatorImpl(fileSystemService)
}