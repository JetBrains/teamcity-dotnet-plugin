package jetbrains.buildServer.dotCover

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.RunTypeExtension
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.util.positioning.PositionAware
import jetbrains.buildServer.web.functions.InternalProperties
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.testng.Assert.*
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverRunnerRunTypeTest {
    @MockK private lateinit var _runTypeRegistryMock: RunTypeRegistry
    @MockK private lateinit var _pluginDescriptorMock: PluginDescriptor
    @MockK private lateinit var _dotCoverRequirementsProviderMock: DotCoverRequirementsProvider
    private lateinit var _instance: DotCoverRunnerRunType;

    @BeforeMethod
    fun setUp() {
        clearAllMocks()
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic(InternalProperties::class)
        every { InternalProperties.getBooleanOrTrue(any()) } returns false
        _instance = DotCoverRunnerRunType(
            _runTypeRegistryMock,
            _pluginDescriptorMock,
            _dotCoverRequirementsProviderMock,
        )
    }

    @AfterMethod
    fun tearDown() {
        unmockkStatic(InternalProperties::class)
    }

    @DataProvider
    fun `register run type data provider`() = arrayOf(
        arrayOf(true, 1),
        arrayOf(false, 0),
        arrayOf(null, 1),
    )
    @Test(dataProvider = "register run type data provider")
    fun `should register run type in registy when feature toggle is enabled`(isFeatureFlagEnabled: Boolean?, registrationCount: Int) {
        // assert
        every { InternalProperties.getBooleanOrTrue(any()) } returns isFeatureFlagEnabled

        // act
        _instance = DotCoverRunnerRunType(
            _runTypeRegistryMock,
            _pluginDescriptorMock,
            _dotCoverRequirementsProviderMock,
        )

        // assert
        verify(exactly = registrationCount) { _runTypeRegistryMock.registerRunType(_instance) }
    }

    @Test
    fun `should always provide runner type`() {
        // act
        val type = _instance.type

        // assert
        assertEquals(type, CoverageConstants.DOTCOVER_RUNNER_TYPE)
    }

    @Test
    fun `should always provide display name`() {
        // act
        val displayName = _instance.displayName

        // assert
        assertEquals(displayName, "dotCover")
    }

    @Test
    fun `should always provide description`() {
        // act
        val description = _instance.description

        // assert
        assertEquals(description, ".NET code coverage tool")
    }

    @Test
    fun `should always provide JSPs and icon paths from plugin descriptor`() {
        // assert
        _pluginDescriptorMock.also {
            every { it.getPluginResourcesPath(any()) } returns "NOT_BLANK"
        }

        // act
        val editParamsJSPPath = _instance.editRunnerParamsJspFilePath
        val viewParamsJSPPath = _instance.viewRunnerParamsJspFilePath
        val iconUrl = _instance.iconUrl

        // assert
        assertTrue(editParamsJSPPath.isNotBlank())
        assertTrue(viewParamsJSPPath.isNotBlank())
        assertTrue(iconUrl.isNotBlank())
        _pluginDescriptorMock.also {
            verify(exactly = 1) { it.getPluginResourcesPath("editDotCoverRunnerParameters.jsp") }
            verify(exactly = 1) { it.getPluginResourcesPath("viewDotCoverRunnerParameters.jsp") }
            verify(exactly = 1) { it.getPluginResourcesPath("dotcover.svg") }
        }
    }

    @Test
    fun `should always provide non empty set of tags`() {
        // act
        val tags = _instance.tags

        // assert
        assertTrue(tags.isNotEmpty())
    }

    @Test
    fun `should always provide default runner properties`() {
        // act
        val properties = _instance.defaultRunnerProperties

        // assert
        assertEquals(properties.size, 1)
        assertEquals(properties.get(CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT), "true")
    }

    @Test
    fun `should support container wrapper extension`() {
        // arrange
        val containerWrapperExtensionMock = mockk<RunTypeExtension>(moreInterfaces = arrayOf(PositionAware::class)).also {
            every { (it as PositionAware).orderId } returns "dockerWrapper"
        }

        // act
        val isContainerWrapperSupported = _instance.supports(containerWrapperExtensionMock)

        // assert
        assertTrue(isContainerWrapperSupported)
    }

    @DataProvider
    fun `invalid dotCover home properties`() = arrayOf(
        arrayOf(mapOf(CoverageConstants.PARAM_DOTCOVER_HOME to null)),
        arrayOf(mapOf(CoverageConstants.PARAM_DOTCOVER_HOME to "")),
        arrayOf(mapOf(CoverageConstants.PARAM_DOTCOVER_HOME to "  ")),
    )
    @Test(dataProvider = "invalid dotCover home properties")
    fun `should validate dotCover home property`(properties: Map<String, String>) {
        // arrange
        val propertiesProcessor = _instance.runnerPropertiesProcessor

        // act
        val invalidProperties = propertiesProcessor.process(properties)

        // assert
        assertEquals(invalidProperties.size, 1)
        invalidProperties.first().let {
            assertEquals(it.propertyName, DotnetConstants.PARAM_COMMAND)
            assertTrue(it.invalidReason?.isNotBlank() ?: false)
        }
    }

    @DataProvider
    fun `selected options properties validation`() = arrayOf(
        arrayOf(
            mapOf(
                CoverageConstants.PARAM_DOTCOVER_HOME to "NOT_BLANK",
                CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_EXECUTABLE to "",
                CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT to "false",
                CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS to "  "
            ),
            false
        ),
        arrayOf(
            mapOf(
                CoverageConstants.PARAM_DOTCOVER_HOME to "NOT_BLANK",
                CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_EXECUTABLE to null,
                CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT to null,
                CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS to null
            ),
            false
        ),
        arrayOf(
            mapOf(
                CoverageConstants.PARAM_DOTCOVER_HOME to "NOT_BLANK",
                CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_EXECUTABLE to "NOT_BLANK",
                CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT to null,
                CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS to null
            ),
            true
        ),
        arrayOf(
            mapOf(
                CoverageConstants.PARAM_DOTCOVER_HOME to "NOT_BLANK",
                CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_EXECUTABLE to null,
                CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT to "true",
                CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS to null
            ),
            true
        ),
        arrayOf(
            mapOf(
                CoverageConstants.PARAM_DOTCOVER_HOME to "NOT_BLANK",
                CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_EXECUTABLE to null,
                CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT to null,
                CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS to "NOT_BLANK"
            ),
            true
        ),
        arrayOf(
            mapOf(
                CoverageConstants.PARAM_DOTCOVER_HOME to "NOT_BLANK",
                CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_EXECUTABLE to "NOT_BLANK",
                CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT to null,
                CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS to null
            ),
            true
        ),
    )
    @Test(dataProvider = "selected options properties validation")
    fun `should validate if no option seleted`(properties: Map<String, String>, isValid: Boolean) {
        // arrange
        val propertiesProcessor = _instance.runnerPropertiesProcessor

        // act
        val invalidProperties = propertiesProcessor.process(properties)

        // assert
        assertEquals(invalidProperties.size, if (isValid) 0 else 3)
        assertEquals(isValid, !invalidProperties.containsWithNonBlankReason(CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_EXECUTABLE))
        assertEquals(isValid, !invalidProperties.containsWithNonBlankReason(CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT))
        assertEquals(isValid, !invalidProperties.containsWithNonBlankReason(CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS))
    }

    @DataProvider
    fun `selected options properties description`() = arrayOf(
        arrayOf(
            mapOf(CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_EXECUTABLE to "abc"),
            "Cover process: abc \n"
        ),
        arrayOf(
            mapOf(
                CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_EXECUTABLE to "abc",
                CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_ARGUMENTS to "a b c"
            ),
            "Cover process: abc a b c\n"
        ),
        arrayOf(
            mapOf(CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT to "true"),
            "Generate report\n"
        ),
        arrayOf(
            mapOf(CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS to "true"),
            "Include additional dotCover snapshots to the report\n"
        ),
        arrayOf(
            mapOf(DotnetConstants.PARAM_DOCKER_IMAGE to "dotnet"),
            "Container image: dotnet\n"
        ),
    )
    @Test(dataProvider = "selected options properties description")
    fun `should describe selected options`(properties: Map<String, String>, expectedDescription: String) {
        // act
        val actualDescription = _instance.describeParameters(properties)

        // assert
        assertEquals(actualDescription, expectedDescription)
    }

    @Test
    fun `should provide runner specific requirements`() {
        // arrange
        val parameters = mockk<Map<String, String>>()
        val expectedRequirements = sequenceOf<Requirement>(mockk(), mockk(), mockk())
        _dotCoverRequirementsProviderMock.also {
            every { it.getRequirements(any()) } returns expectedRequirements
        }

        // act
        val actualRequirements = _instance.getRunnerSpecificRequirements(parameters)

        // assert
        assertEquals(actualRequirements, expectedRequirements.toList())
        verify(exactly = 1) { _dotCoverRequirementsProviderMock.getRequirements(parameters) }
    }

    companion object {
        private fun Collection<InvalidProperty>.containsWithNonBlankReason(name: String) =
            this.singleOrNull { it.propertyName == name && (it.invalidReason?.isNotBlank() ?: false) }
                .let { when (it) {
                    null -> false
                    else -> true
                }}
    }
}