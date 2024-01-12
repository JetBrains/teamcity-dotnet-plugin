

package jetbrains.buildServer.dotnet.test.dotnet.discovery.dotnetFramework

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.discovery.dotnetFramework.DotnetFramework
import jetbrains.buildServer.dotnet.discovery.dotnetFramework.DotnetFrameworkValidatorImpl
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetFrameworkValidatorTest {
    private val _file = File("file")
    private val _dir = File("dir")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        DotnetFramework(Platform.x86, Version(1, 0, 0), _dir),
                        VirtualFileSystemService(),
                        false
                ),
                arrayOf(
                        DotnetFramework(Platform.x86, Version(1, 0, 0), _file),
                        VirtualFileSystemService().addFile(_file),
                        false
                ),
                arrayOf(
                        DotnetFramework(Platform.x86, Version(3, 0), _dir),
                        VirtualFileSystemService().addDirectory(_dir),
                        true
                ),
                arrayOf(
                        DotnetFramework(Platform.x86, Version(1, 0), _dir),
                        VirtualFileSystemService().addDirectory(_dir),
                        false
                ),
                arrayOf(
                        DotnetFramework(Platform.x86, Version(2, 0), _dir),
                        VirtualFileSystemService().addDirectory(_dir),
                        false
                ),
                arrayOf(
                        DotnetFramework(Platform.x86, Version(4, 8, 123), _dir),
                        VirtualFileSystemService().addDirectory(_dir),
                        false
                ),
                arrayOf(
                        DotnetFramework(Platform.x86, Version(1, 1, 0), _dir),
                        VirtualFileSystemService().addDirectory(_dir).addFile(File(_dir, "csc.exe")).addFile(File(_dir, "Vbc.exe")),
                        true
                ),
                arrayOf(
                        DotnetFramework(Platform.x86, Version(4, 8, 123), _dir),
                        VirtualFileSystemService().addDirectory(_dir).addFile(File(_dir, "csc.exe")).addFile(File(_dir, "Vbc.exe")),
                        false
                ),
                arrayOf(
                        DotnetFramework(Platform.x86, Version(4, 8, 123), _dir),
                        VirtualFileSystemService().addDirectory(_dir).addFile(File(_dir, "csc.exe")).addFile(File(_dir, "Vbc.exe")).addFile(File(_dir, "MSBuild.Exe")),
                        true
                ),
                arrayOf(
                        DotnetFramework(Platform.x86, Version(3, 5, 123), _dir),
                        VirtualFileSystemService().addDirectory(_dir).addFile(File(_dir, "csc.exe")).addFile(File(_dir, "Vbc.exe")).addFile(File(_dir, "MSBuild.Exe")),
                        true
                )
        )
    }

    @Test(dataProvider = "testData")
    fun shouldValidate(framework: DotnetFramework, fileSystemService: FileSystemService, expectedResult: Boolean) {
        // Given
        val validator = createInstance(fileSystemService)

        // When
        val actualResult = validator.isValid(framework)

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            DotnetFrameworkValidatorImpl(fileSystemService)
}