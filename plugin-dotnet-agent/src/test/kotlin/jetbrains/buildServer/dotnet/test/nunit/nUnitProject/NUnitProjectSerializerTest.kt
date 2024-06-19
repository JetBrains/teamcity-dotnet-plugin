package jetbrains.buildServer.dotnet.test.nunit.nUnitProject

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.XmlDocumentServiceImpl
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.nunit.*
import jetbrains.buildServer.nunit.nUnitProject.NUnitProject
import jetbrains.buildServer.nunit.nUnitProject.NUnitProjectSerializer
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

class NUnitProjectSerializerTest {
    @MockK
    private lateinit var _nUnitSettings: NUnitSettings

    @MockK
    private lateinit var _pathsService: PathsService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    data class TestCase(
        val appConfig: String?
    )

    @DataProvider(name = "testCases")
    fun getCases(): Array<TestCase> = arrayOf(
        TestCase(appConfig = "app.config"),
        TestCase(appConfig = null),
    )

    @Test(dataProvider = "testCases")
    fun `should generate nunit project file`(testCase: TestCase) {
        // arrange
        every { _nUnitSettings.appConfigFile } returns testCase.appConfig

        val appConfigPath = testCase.appConfig?.let(::Path)
        if (appConfigPath != null) {
            every { _pathsService.resolvePath(PathType.Checkout, testCase.appConfig) } returns appConfigPath
        }

        val serializer = NUnitProjectSerializer(_nUnitSettings, XmlDocumentServiceImpl(), _pathsService)

        // act
        val outputStream = ByteArrayOutputStream()
        serializer.create(
            NUnitProject(
                File("appBasePath"),
                listOf(
                    File("assembly1.dll"),
                    File("assembly2.dll"),
                    File("assembly3.dll")
                )
            ),
            outputStream
        )
        val project = outputStream.toString()
        // assert
        val configFileAttribute = appConfigPath?.let { """configfile="${it.absolutePathString()}" """ } ?: ""
        val expectedXml = """
            <NUnitProject>
                <Settings activeconfig="active" appbase="appBasePath"/>
                <Config ${configFileAttribute}name="active">
                    <assembly path="assembly1.dll"/>
                    <assembly path="assembly2.dll"/>
                    <assembly path="assembly3.dll"/>
                </Config>
            </NUnitProject>

        """.trimIndent()
        Assert.assertEquals(removeWhitespace(project), removeWhitespace(expectedXml))
    }

    private fun removeWhitespace(input: String) = input.replace("\\s+".toRegex(), "")
}