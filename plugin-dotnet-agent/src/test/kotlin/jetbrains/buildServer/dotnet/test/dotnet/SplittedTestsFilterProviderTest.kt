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
                                            #current_batch=1
                                            #total_batches=2
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
                                            #current_batch=1
                                            #total_batches=2
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
                                            #current_batch=1
                                            #total_batches=1
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
        obj.write(str.toByteArray(charset("UTF-8")))
        obj.flush()
        obj.close()
        return obj.toByteArray()
    }

    companion object {
        private val TestsPartsFile = File((File(File("tmp"), "splitTests")), "testsGroup.txt")
    }
}