package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.SplittedTestsFilterProvider
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

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
                        "1",
                        ExcludesFile.path,
                        IncludesFile.path,
                        VirtualFileSystemService()
                                .addFile(
                                        ExcludesFile,
                                        VirtualFileSystemService.Attributes(),
                                        write("""
                                            #version=1
                                            #algorithm=test
                                            #current_batch=1
                                            #total_batches=2
                                            Abc
                                        """.trimIndent()
                                        ))
                                .addFile(
                                        IncludesFile,
                                        VirtualFileSystemService.Attributes(),
                                        write("""
                                            #version=1
                                            #algorithm=test
                                            #current_batch=1
                                            #total_batches=2
                                            Cba
                                        """.trimIndent()
                                        )
                                ),
                        "FullyQualifiedName!~Abc."
                ),
                arrayOf(
                        "2",
                        ExcludesFile.path,
                        IncludesFile.path,
                        VirtualFileSystemService()
                                .addFile(
                                        ExcludesFile,
                                        VirtualFileSystemService.Attributes(),
                                        write("""
                                            #version=1
                                            #algorithm=test
                                            #current_batch=2
                                            #total_batches=2
                                            Abc
                                        """.trimIndent()
                                        ))
                                .addFile(
                                        IncludesFile,
                                        VirtualFileSystemService.Attributes(),
                                        write("""
                                            #version=1
                                            #algorithm=test
                                            #current_batch=2
                                            #total_batches=2
                                            Cba
                                        """.trimIndent()
                                        )
                                ),
                        "FullyQualifiedName~Cba."
                ),
                arrayOf(
                        "1",
                        ExcludesFile.path,
                        IncludesFile.path,
                        VirtualFileSystemService()
                                .addFile(
                                        ExcludesFile,
                                        VirtualFileSystemService.Attributes(),
                                        write("""
                                            #version=1
                                            #algorithm=test
                                            #current_batch=1
                                            #total_batches=2
                                            #suite=suite1
                                            Cba
                                            #suite=suite2
                                            Zyx
                                        """.trimIndent()
                                        ))
                                .addFile(
                                        IncludesFile,
                                        VirtualFileSystemService.Attributes(),
                                        write("""
                                            #version=1
                                            #algorithm=test
                                            #current_batch=1
                                            #total_batches=2
                                            #suite=suite1
                                            Abc
                                            #suite=suite1
                                            Xyz
                                        """.trimIndent()
                                        )),
                        "FullyQualifiedName!~Cba. & FullyQualifiedName!~Zyx."
                ),
                arrayOf(
                        "2",
                        ExcludesFile.path,
                        IncludesFile.path,
                        VirtualFileSystemService()
                                .addFile(
                                        ExcludesFile,
                                        VirtualFileSystemService.Attributes(),
                                        write("""
                                            #version=1
                                            #algorithm=test
                                            #current_batch=2
                                            #total_batches=2
                                            #suite=suite1
                                            Cba
                                            #suite=suite2
                                            Zyx
                                        """.trimIndent()
                                        ))
                                .addFile(
                                        IncludesFile,
                                        VirtualFileSystemService.Attributes(),
                                        write("""
                                            #version=1
                                            #algorithm=test
                                            #current_batch=2
                                            #total_batches=2
                                            #suite=suite1
                                            Abc
                                            #suite=suite1
                                            Xyz
                                        """.trimIndent()
                                        )),
                        "FullyQualifiedName~Abc. | FullyQualifiedName~Xyz."
                ),
                arrayOf(
                        "1",
                        ExcludesFile.path,
                        IncludesFile.path,
                        VirtualFileSystemService()
                                .addFile(
                                        ExcludesFile,
                                        VirtualFileSystemService.Attributes(),
                                        write(" "))
                                .addFile(
                                        IncludesFile,
                                        VirtualFileSystemService.Attributes(),
                                        write(" ")),
                        ""
                ),
                arrayOf(
                        "2",
                        ExcludesFile.path,
                        IncludesFile.path,
                        VirtualFileSystemService().addDirectory(ExcludesFile),
                        ""
                ),
                arrayOf(
                        "1",
                        ExcludesFile.path,
                        IncludesFile.path,
                        VirtualFileSystemService(),
                        ""
                )
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideFilter(currentBatch: String, excludesFileName: String?, includesFileName: String?, fileSystem: FileSystemService, expecedFilter: String) {
        // Given
        every { _parametersService.tryGetParameter(ParameterType.System, SplittedTestsFilterProvider.ExcludesFileParam) } returns excludesFileName
        every { _parametersService.tryGetParameter(ParameterType.System, SplittedTestsFilterProvider.IncludesFileParam) } returns includesFileName
        every { _parametersService.tryGetParameter(ParameterType.Configuration, SplittedTestsFilterProvider.CurrentBatch) } returns currentBatch
        val provider = createInstance(fileSystem)

        // When
        val actulFilter = provider.filterExpression;

        // Then
        Assert.assertEquals(actulFilter, expecedFilter)
    }

    private fun createInstance(fileSystemService: FileSystemService) = SplittedTestsFilterProvider(_parametersService, fileSystemService)

    private fun write(input: String) = ByteArrayInputStream(compress(input))

    fun compress(str: String): ByteArray {
        val obj = ByteArrayOutputStream()
        obj.write(str.toByteArray(charset("UTF-8")))
        obj.flush()
        obj.close()
        return obj.toByteArray()
    }

    companion object {
        private val ExcludesFile = File((File(File("tmp"), "parallelTests")), "excludesFile.txt")
        private val IncludesFile = File((File(File("tmp"), "parallelTests")), "includesFile.txt")
    }
}