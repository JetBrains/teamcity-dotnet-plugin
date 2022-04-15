package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.SplittedTestsFilterProvider
import jetbrains.buildServer.dotnet.SplittedTestsFilterType
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.*
import java.util.zip.GZIPOutputStream

class SplittedTestsFilterProviderTest {
    @MockK private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider(name = "testData")
    fun testData(): Any {
        return arrayOf(
                arrayOf(
                        TestsPartsFile.path,
                        VirtualFileSystemService().addFile(
                                TestsPartsFile,
                                VirtualFileSystemService.Attributes(),
                                pack(
                                        """
                                            #version=1.0
                                            #algorithm=test
                                            #batch_num=1
                                            #total=1
                                            #filtering_mode=include all
                                            Abc
                                        """.trimIndent()
                                )),
                        ""
                ),
                arrayOf(
                        TestsPartsFile.path,
                        VirtualFileSystemService().addFile(
                                TestsPartsFile,
                                VirtualFileSystemService.Attributes(),
                                pack(
                                        """
                                            #version=1.0
                                            #algorithm=test
                                            #batch_num=1
                                            #total=1
                                            #filtering_mode=exclude all
                                            Abc
                                        """.trimIndent()
                                )),
                        ""
                ),
                arrayOf(
                        TestsPartsFile.path,
                        VirtualFileSystemService().addFile(
                                TestsPartsFile,
                                VirtualFileSystemService.Attributes(),
                                pack(
                                        """
                                            #version=1.0
                                            #algorithm=test
                                            #batch_num=2
                                            #total=2
                                            #filtering_mode=includes
                                            Abc
                                            #filtering_mode=excludes
                                            Cba
                                        """.trimIndent()
                                )),
                        "FullyQualifiedName~Abc"
                ),
                arrayOf(
                        TestsPartsFile.path,
                        VirtualFileSystemService().addFile(
                                TestsPartsFile,
                                VirtualFileSystemService.Attributes(),
                                pack(
                                        """
                                            #version=1.0
                                            #algorithm=test
                                            #batch_num=1
                                            #total=1
                                            #filtering_mode=includes
                                            Cba
                                            #filtering_mode=excludes
                                            Abc
                                        """.trimIndent()
                                )),
                        "FullyQualifiedName!~Abc"
                ),
                arrayOf(
                        TestsPartsFile.path,
                        VirtualFileSystemService().addFile(
                                TestsPartsFile,
                                VirtualFileSystemService.Attributes(),
                                pack(
                                        """
                                            #version=1.0
                                            #algorithm=test
                                            #batch_num=2
                                            #total=2
                                            #filtering_mode=includes
                                            #suite=suite1
                                            Abc
                                            #suite=suite2
                                            Xyz
                                            #filtering_mode=excludes
                                            #suite=suite1
                                            Cba
                                            #suite=suite2
                                            Zyx
                                        """.trimIndent()
                                )),
                        "FullyQualifiedName~Abc | FullyQualifiedName~Xyz"
                ),
                arrayOf(
                        TestsPartsFile.path,
                        VirtualFileSystemService().addFile(
                                TestsPartsFile,
                                VirtualFileSystemService.Attributes(),
                                pack(
                                        """
                                            #version=1.0
                                            #algorithm=test
                                            #batch_num=1
                                            #total=2
                                            #filtering_mode=includes
                                            #suite=suite1
                                            Cba
                                            #suite=suite2
                                            Zyx
                                            #filtering_mode=excludes
                                            #suite=suite1
                                            Abc
                                            #suite=suite1
                                            Xyz
                                        """.trimIndent()
                                )),
                        "FullyQualifiedName!~Abc & FullyQualifiedName!~Xyz"
                ),
                arrayOf(
                        TestsPartsFile.path,
                        VirtualFileSystemService().addFile(
                                TestsPartsFile,
                                VirtualFileSystemService.Attributes(),
                                pack(
                                        """
                                            #version=1.0
                                            #algorithm=test
                                            #batch_num=2
                                            #total=2
                                            #filtering_mode=includes
                                        """.trimIndent()
                                )),
                        ""
                ),
                arrayOf(
                        TestsPartsFile.path,
                        VirtualFileSystemService().addFile(
                                TestsPartsFile,
                                VirtualFileSystemService.Attributes(),
                                pack(
                                        """
                                            #version=1.0
                                            #algorithm=test
                                            #batch_num=1
                                            #total=1
                                            #filtering_mode=excludes
                                        """.trimIndent()
                                )),
                        ""
                ),
                arrayOf(
                        TestsPartsFile.path,
                        VirtualFileSystemService().addFile(
                                TestsPartsFile,
                                VirtualFileSystemService.Attributes(),
                                pack(
                                        """
                                            #version=1.0
                                            #algorithm=test
                                            #batch_num=1
                                            #total=1
                                            #filtering_mode=none
                                            aaa
                                        """.trimIndent()
                                )),
                        ""
                ),
                arrayOf(
                        TestsPartsFile.path,
                        VirtualFileSystemService().addFile(
                                TestsPartsFile,
                                VirtualFileSystemService.Attributes(),
                                pack(
                                        " "
                                )),
                        ""
                ),
                arrayOf(
                        TestsPartsFile.path,
                        VirtualFileSystemService().addDirectory(TestsPartsFile),
                        ""
                )
                ,
                arrayOf(
                        TestsPartsFile.path,
                        VirtualFileSystemService(),
                        ""
                )
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideFilter(testsPartsParamValue: String?, fileSystem: FileSystemService, expecedFilter: String) {
        // Given
        every { _parametersService.tryGetParameter(ParameterType.System, SplittedTestsFilterProvider.TestsPartsFileParam) } returns testsPartsParamValue
        val provider = createInstance(fileSystem)

        // When
        val actulFilter = provider.filterExpression;

        // Then
        Assert.assertEquals(actulFilter, expecedFilter)
    }

    private fun createInstance(fileSystemService: FileSystemService) = SplittedTestsFilterProvider(_parametersService, fileSystemService)

    private fun pack(input: String) = ByteArrayInputStream(compress(input))

    fun compress(str: String): ByteArray {
        val obj = ByteArrayOutputStream()
        val gzip = GZIPOutputStream(obj)
        gzip.write(str.toByteArray(charset("UTF-8")))
        gzip.flush()
        gzip.close()
        return obj.toByteArray()
    }

    companion object {
        private val TestsPartsFile = File((File(File("tmp"), "splitTests")), "testsGroup.txt")
    }
}