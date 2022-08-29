package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.SplitTestsFilterProvider
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

class SplitTestsFilterProviderTest {
    @MockK private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    public class TestData(
        val currentBatch: String,
        val excludesFileName: String?,
        val includesFileName: String?,
        val fileSystem: FileSystemService,
        val expectedFilter: String,
    ) {
        companion object {
            fun generateTestsNames(count: Int): List<String> =
                MutableList<String>(count, { UUID.randomUUID().toString() })

            fun generateTestsListFileContent(currentBatch: Int, totalBatches: Int, tests: List<String>) =  buildString {
                appendLine("#version=1")
                appendLine("#algorithm=test")
                appendLine("#current_batch=${currentBatch}")
                appendLine("#total_batches=${totalBatches}")
                tests.forEach(::appendLine)
            }

            fun generateTestsFilter(isIncludeFilter: Boolean, testNames: List<String>) = buildString {
                var testsCount = testNames.size
                var (operator, combiner) = if (isIncludeFilter) Pair("~", " | ") else Pair("!~", " & ")

                append(
                    testNames
                        .map { "FullyQualifiedName${operator}${it}." }
                        .let { filterParts ->
                            if (testsCount > 1000)
                                filterParts.chunked(1000) { "(${it.joinToString(combiner)})" }
                            else
                                filterParts
                        }
                        .joinToString(combiner)
                )
            }
        }
    }

    @DataProvider(name = "testData")
    fun testData() =
        arrayOf(
            TestData(
                currentBatch = "1",
                excludesFileName = ExcludesFile.path,
                includesFileName = IncludesFile.path,
                fileSystem = VirtualFileSystemService()
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
                            )
                    )
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
                expectedFilter = "FullyQualifiedName!~Abc."
            ),
            TestData(
                currentBatch = "2",
                excludesFileName = ExcludesFile.path,
                includesFileName = IncludesFile.path,
                fileSystem = VirtualFileSystemService()
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
                            )
                    )
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
                expectedFilter = "FullyQualifiedName~Cba."
            ),
            TestData(
                currentBatch = "1",
                excludesFileName = ExcludesFile.path,
                includesFileName = IncludesFile.path,
                fileSystem = VirtualFileSystemService()
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
                            )
                    )
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
                            )
                    ),
                expectedFilter = "FullyQualifiedName!~Cba. & FullyQualifiedName!~Zyx."
            ),
            TestData(
                currentBatch = "2",
                excludesFileName = ExcludesFile.path,
                includesFileName = IncludesFile.path,
                fileSystem = VirtualFileSystemService()
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
                            )
                    )
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
                            )
                    ),
                expectedFilter = "FullyQualifiedName~Abc. | FullyQualifiedName~Xyz."
            ),
            TestData(
                currentBatch = "2",
                excludesFileName = ExcludesFile.path,
                includesFileName = IncludesFile.path,
                fileSystem = VirtualFileSystemService()
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
                        )
                    )
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
                        )
                    ),
                expectedFilter = "FullyQualifiedName~Abc. | FullyQualifiedName~Xyz."
            ),
            TestData(
                currentBatch = "1",
                excludesFileName = ExcludesFile.path,
                includesFileName = IncludesFile.path,
                fileSystem = VirtualFileSystemService()
                    .addFile(
                            ExcludesFile,
                            VirtualFileSystemService.Attributes(),
                            write(" ")
                    )
                    .addFile(
                            IncludesFile,
                            VirtualFileSystemService.Attributes(),
                            write(" ")
                    ),
                expectedFilter = ""
            ),
            TestData(
                currentBatch = "2",
                excludesFileName = ExcludesFile.path,
                includesFileName = IncludesFile.path,
                fileSystem = VirtualFileSystemService().addDirectory(ExcludesFile),
                expectedFilter = ""
            ),
            TestData(
                currentBatch = "1",
                excludesFileName = ExcludesFile.path,
                includesFileName = IncludesFile.path,
                fileSystem = VirtualFileSystemService(),
                expectedFilter = ""
            ),
            TestData.generateTestsNames(999).let { testsNames ->
                TestData(
                    currentBatch = "1",
                    excludesFileName = ExcludesFile.path,
                    includesFileName = IncludesFile.path,
                    fileSystem =
                        VirtualFileSystemService()
                            .addFile(
                                ExcludesFile,
                                VirtualFileSystemService.Attributes(),
                                write(TestData.generateTestsListFileContent(1, 4, testsNames))
                            ),
                    expectedFilter = TestData.generateTestsFilter(false, testsNames)
                )
            },
            TestData.generateTestsNames(1001).let { testsNames ->
                TestData(
                    currentBatch = "2",
                    excludesFileName = ExcludesFile.path,
                    includesFileName = IncludesFile.path,
                    fileSystem =
                    VirtualFileSystemService()
                        .addFile(
                            IncludesFile,
                            VirtualFileSystemService.Attributes(),
                            write(TestData.generateTestsListFileContent(1, 4, testsNames))
                        ),
                    expectedFilter = TestData.generateTestsFilter(true, testsNames)
                )
            },
            TestData.generateTestsNames(10_000).let { testsNames ->
                TestData(
                    currentBatch = "4",
                    excludesFileName = ExcludesFile.path,
                    includesFileName = IncludesFile.path,
                    fileSystem =
                    VirtualFileSystemService()
                        .addFile(
                            IncludesFile,
                            VirtualFileSystemService.Attributes(),
                            write(TestData.generateTestsListFileContent(1, 4, testsNames))
                        ),
                    expectedFilter = TestData.generateTestsFilter(true, testsNames)
                )
            },
        )

//    @Test(dataProvider = "testData")
//    fun shouldProvideFilter(testData: TestData) {
//        // Given
//        every { _parametersService.tryGetParameter(ParameterType.System, SplitTestsFilterProvider.ExcludesFileParam) } returns testData.excludesFileName
//        every { _parametersService.tryGetParameter(ParameterType.System, SplitTestsFilterProvider.IncludesFileParam) } returns testData.includesFileName
//        every { _parametersService.tryGetParameter(ParameterType.Configuration, SplitTestsFilterProvider.CurrentBatch) } returns testData.currentBatch
//        val provider = createInstance(testData.fileSystem)
//
//        // When
//        val actulFilter = provider.filterExpression;
//
//        // Then
//        Assert.assertEquals(actulFilter, testData.expectedFilter)
//    }

//    private fun createInstance(fileSystemService: FileSystemService) = SplitTestsFilterProvider(_parametersService, fileSystemService)

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