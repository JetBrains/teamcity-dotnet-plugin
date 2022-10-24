package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.E
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.inspect.InspectCodeConstants.CONFIG_PARAMETER_DISABLE_SOLUTION_WIDE_ANALYSIS
import jetbrains.buildServer.inspect.InspectCodeConstants.CONFIG_PARAMETER_SUPRESS_BUILD_IN_SETTINGS
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_CUSTOM_SETTINGS_PROFILE_PATH
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_PROJECT_FILTER
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_SOLUTION_PATH
import jetbrains.buildServer.inspect.InspectionConfigurationFile
import jetbrains.buildServer.inspect.XmlWriter
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream

class InspectionConfigurationFileTest {
    @MockK private lateinit var _xmlWriter: XmlWriter

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider(name = "createCases")
    fun getCreateCases(): Array<TestCase> {
        val solutionPathWithSpaces = arrayOf("My.sln ", " My.sln", " My.sln ", "My.sln\n")
            .map {
                TestCase(
                    Path("output.xml"),
                    null,
                    false,
                    ParametersServiceStub(
                        mapOf(
                            RUNNER_SETTING_SOLUTION_PATH to it
                        )
                    ),
                    E("InspectCodeOptions",
                        E("OutputFile", "output.xml"),
                        E("SolutionFile", "My.sln")
                    )
                )
            }

        return arrayOf(
            TestCase(
                Path("output.xml"),
                Path("cache"),
                true,
                ParametersServiceStub(
                    mapOf(
                        RUNNER_SETTING_PROJECT_FILTER to "Abc\nxyz",
                        RUNNER_SETTING_SOLUTION_PATH to "My.sln",
                        CONFIG_PARAMETER_SUPRESS_BUILD_IN_SETTINGS to "true",
                        CONFIG_PARAMETER_DISABLE_SOLUTION_WIDE_ANALYSIS to "true",
                        RUNNER_SETTING_CUSTOM_SETTINGS_PROFILE_PATH to "sln.DotSettings"
                    )
                ),
                E("InspectCodeOptions",
                    E("Debug", "true"),
                    E("IncludedProjects",
                        E("IncludedProjects", "Abc"),
                        E("IncludedProjects", "xyz")
                    ),
                    E("OutputFile", "output.xml"),
                    E("SolutionFile", "My.sln"),
                    E("CachesHomeDirectory", "cache"),
                    E("CustomSettingsProfile", "sln.DotSettings"),
                    E("SupressBuildInSettings", "true"),
                    E("NoSolutionWideAnalysis", "true")
                )
            ),

            TestCase(
                Path("output.xml"),
                null,
                true,
                ParametersServiceStub(
                    mapOf(
                        RUNNER_SETTING_PROJECT_FILTER to "Abc\nxyz",
                        RUNNER_SETTING_SOLUTION_PATH to "My.sln",
                        CONFIG_PARAMETER_SUPRESS_BUILD_IN_SETTINGS to "true",
                        CONFIG_PARAMETER_DISABLE_SOLUTION_WIDE_ANALYSIS to "true"
                    )
                ),
                E("InspectCodeOptions",
                    E("Debug", "true"),
                    E("IncludedProjects",
                        E("IncludedProjects", "Abc"),
                        E("IncludedProjects", "xyz")
                    ),
                    E("OutputFile", "output.xml"),
                    E("SolutionFile", "My.sln"),
                    E("SupressBuildInSettings", "true"),
                    E("NoSolutionWideAnalysis", "true")
                )
            ),

            TestCase(
                Path("output.xml"),
                Path("cache"),
                true,
                ParametersServiceStub(
                    mapOf(
                        RUNNER_SETTING_PROJECT_FILTER to "Abc\nxyz",
                        RUNNER_SETTING_SOLUTION_PATH to "My.sln",
                        CONFIG_PARAMETER_SUPRESS_BUILD_IN_SETTINGS to "True",
                        CONFIG_PARAMETER_DISABLE_SOLUTION_WIDE_ANALYSIS to "True"
                    )
                ),
                E("InspectCodeOptions",
                    E("Debug", "true"),
                    E("IncludedProjects",
                        E("IncludedProjects", "Abc"),
                        E("IncludedProjects", "xyz")
                    ),
                    E("OutputFile", "output.xml"),
                    E("SolutionFile", "My.sln"),
                    E("CachesHomeDirectory", "cache"),
                    E("SupressBuildInSettings", "true"),
                    E("NoSolutionWideAnalysis", "true")
                )
            ),

            TestCase(
                Path("output.xml"),
                Path("cache"),
                true,
                ParametersServiceStub(
                    mapOf(
                        RUNNER_SETTING_PROJECT_FILTER to "Abc\nxyz",
                        RUNNER_SETTING_SOLUTION_PATH to "My.sln"
                    )
                ),
                E("InspectCodeOptions",
                    E("Debug", true.toString()),
                    E("IncludedProjects",
                        E("IncludedProjects", "Abc"),
                        E("IncludedProjects", "xyz")
                    ),
                    E("OutputFile", "output.xml"),
                    E("SolutionFile", "My.sln"),
                    E("CachesHomeDirectory", "cache")
                )
            ),

            TestCase(
                Path("output.xml"),
                null,
                false,
                ParametersServiceStub(
                    mapOf(
                        RUNNER_SETTING_PROJECT_FILTER to "Abc\nxyz",
                        RUNNER_SETTING_SOLUTION_PATH to "My.sln"
                    )
                ),
                E("InspectCodeOptions",
                    E("IncludedProjects",
                        E("IncludedProjects", "Abc"),
                        E("IncludedProjects", "xyz")
                    ),
                    E("OutputFile", "output.xml"),
                    E("SolutionFile", "My.sln")
                )
            ),

            TestCase(
                Path("Output.xml"),
                null,
                false,
                ParametersServiceStub(emptyMap()),
                E("InspectCodeOptions",
                    E("OutputFile", "Output.xml")
                )
            ),

            *solutionPathWithSpaces.toTypedArray()
        )
    }

    @Test(dataProvider = "createCases")
    fun shouldCreate(case: TestCase) {
        // arrange
        val configFile = createInstance(case.parametersService)
        val stream = ByteArrayOutputStream()
        var actualXml: E? = null
        every { _xmlWriter.write(any(), stream) } answers { actualXml = arg<E>(0) }

        // act
        configFile.create(stream, case.outputFile, case.cachesHomeDirectory, case.debug)

        // assert
        Assert.assertEquals(actualXml.toString(), case.expectedXml.toString())
    }

    private fun createInstance(parametersService: ParametersService) =
        InspectionConfigurationFile(
            parametersService,
            _xmlWriter
        )

    data class TestCase(
        val outputFile: Path,
        val cachesHomeDirectory: Path?,
        val debug: Boolean,
        val parametersService: ParametersService,
        val expectedXml: E?,
    )
}