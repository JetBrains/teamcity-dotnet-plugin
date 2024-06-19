package jetbrains.buildServer.dotnet.test.nunit.nUnitProject

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.nunit.arguments.NUnitTestingAssembliesProvider
import jetbrains.buildServer.nunit.nUnitProject.NUnitProject
import jetbrains.buildServer.nunit.nUnitProject.NUnitProjectGenerator
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class NUnitProjectGeneratorTest {
    @MockK
    private lateinit var _testingAssembliesProvider: NUnitTestingAssembliesProvider

    private val assembly1 = File("my1.dll");
    private val assembly2 = File("my2.dll");
    private val assembly3 = File(File("abc"), "my3.dll");
    private val assembly4 = File(File("abc"), "my4.dll");
    private val assembly5 = File(File("abc"), "my1.dll");

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    data class TestCase(
        val expectedProjects: List<NUnitProject>,
        val assemblies: List<File>
    )

    @DataProvider(name = "testCases")
    fun getCases(): Array<TestCase> = arrayOf(
        TestCase(
            expectedProjects = listOf(
                NUnitProject(
                    appBase = assembly1.absoluteFile.parentFile,
                    testingAssemblies = listOf(assembly1.absoluteFile)
                )
            ),
            assemblies = listOf(assembly1)
        ),
        TestCase(
            expectedProjects = listOf(
                NUnitProject(
                    appBase = assembly1.absoluteFile.parentFile,
                    testingAssemblies = listOf(
                        assembly1.absoluteFile,
                        assembly2.absoluteFile
                    )
                )
            ),
            assemblies = listOf(
                assembly1,
                assembly2
            )
        ),
        TestCase(
            expectedProjects = listOf(
                NUnitProject(
                    appBase = assembly3.absoluteFile.parentFile,
                    testingAssemblies = listOf(
                        assembly3.absoluteFile,
                        assembly4.absoluteFile
                    )
                )
            ),
            assemblies = listOf(
                assembly3,
                assembly4
            )
        ),
        TestCase(
            expectedProjects = listOf(
                NUnitProject(
                    appBase = assembly1.absoluteFile.parentFile,
                    testingAssemblies = listOf(assembly1.absoluteFile)
                ),
                NUnitProject(
                    appBase = assembly3.absoluteFile.parentFile,
                    testingAssemblies = listOf(assembly3.absoluteFile)
                )
            ),
            assemblies = listOf(
                assembly1,
                assembly3
            )
        ),
        TestCase(
            expectedProjects = listOf(
                NUnitProject(
                    appBase = assembly1.absoluteFile.parentFile,
                    testingAssemblies = listOf(assembly1.absoluteFile)
                ),
                NUnitProject(
                    appBase = assembly3.absoluteFile.parentFile,
                    testingAssemblies = listOf(
                        assembly3.absoluteFile,
                        assembly5.absoluteFile
                    )
                )
            ),
            assemblies = listOf(
                assembly1,
                assembly3,
                assembly5
            )
        )
    )

    @Test(dataProvider = "testCases")
    fun `should generate nunit projects based on assembly parent directory`(testCase: TestCase) {
        // arrange
        every { _testingAssembliesProvider.assemblies } returns testCase.assemblies

        val generator = NUnitProjectGenerator(_testingAssembliesProvider)

        // act
        val projects = generator.generate()

        // assert
        Assert.assertEquals(projects, testCase.expectedProjects)
    }
}