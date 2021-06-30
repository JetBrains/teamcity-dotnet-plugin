package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.E
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.inspect.InspectCodeConstants
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
    fun getCreateCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        Path("output.xml"),
                        Path("cache"),
                        true,
                        ParametersServiceStub(mapOf(
                                RUNNER_SETTING_PROJECT_FILTER to "Abc\nxyz",
                                RUNNER_SETTING_SOLUTION_PATH to "My.sln",
                                CONFIG_PARAMETER_SUPRESS_BUILD_IN_SETTINGS to "true",
                                CONFIG_PARAMETER_DISABLE_SOLUTION_WIDE_ANALYSIS to "true",
                                RUNNER_SETTING_CUSTOM_SETTINGS_PROFILE_PATH to "sln.DotSettings"
                        )),
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
                arrayOf(
                        Path("output.xml"),
                        null,
                        true,
                        ParametersServiceStub(mapOf(
                                RUNNER_SETTING_PROJECT_FILTER to "Abc\nxyz",
                                RUNNER_SETTING_SOLUTION_PATH to "My.sln",
                                CONFIG_PARAMETER_SUPRESS_BUILD_IN_SETTINGS to "true",
                                CONFIG_PARAMETER_DISABLE_SOLUTION_WIDE_ANALYSIS to "true"
                        )),
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
                arrayOf(
                        Path("output.xml"),
                        Path("cache"),
                        true,
                        ParametersServiceStub(mapOf(
                                RUNNER_SETTING_PROJECT_FILTER to "Abc\nxyz",
                                RUNNER_SETTING_SOLUTION_PATH to "My.sln",
                                CONFIG_PARAMETER_SUPRESS_BUILD_IN_SETTINGS to "True",
                                CONFIG_PARAMETER_DISABLE_SOLUTION_WIDE_ANALYSIS to "True"
                        )),
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
                arrayOf(
                        Path("output.xml"),
                        Path("cache"),
                        true,
                        ParametersServiceStub(mapOf(
                                RUNNER_SETTING_PROJECT_FILTER to "Abc\nxyz",
                                RUNNER_SETTING_SOLUTION_PATH to "My.sln"
                        )),
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
                arrayOf(
                        Path("output.xml"),
                        null,
                        false,
                        ParametersServiceStub(mapOf(
                                RUNNER_SETTING_PROJECT_FILTER to "Abc\nxyz",
                                RUNNER_SETTING_SOLUTION_PATH to "My.sln"
                        )),
                        E("InspectCodeOptions",
                                E("IncludedProjects",
                                        E("IncludedProjects", "Abc"),
                                        E("IncludedProjects", "xyz")
                                ),
                                E("OutputFile", "output.xml"),
                                E("SolutionFile", "My.sln")
                        )
                ),
                arrayOf(
                        Path("Output.xml"),
                        null,
                        false,
                        ParametersServiceStub(emptyMap()),
                        E("InspectCodeOptions",
                                E("OutputFile", "Output.xml")
                        )
                )
        )
    }

    @Test(dataProvider = "createCases")
    fun shouldCreate(
            outputFile: Path,
            cachesHomeDirectory: Path?,
            debug: Boolean,
            parametersService: ParametersService,
            expctedXml: E?) {
        // Given
        val configFile = createInstance(parametersService)
        val stream = ByteArrayOutputStream()
        var actualXml: E? = null;
        every { _xmlWriter.write(any(), stream) } answers { actualXml = arg<E>(0) }

        // When
        configFile.create(stream, outputFile, cachesHomeDirectory, debug)

        // Then
        Assert.assertEquals(actualXml.toString(), expctedXml.toString())
    }

    private fun createInstance(parametersService: ParametersService) =
            InspectionConfigurationFile(
                    parametersService,
                    _xmlWriter)
}