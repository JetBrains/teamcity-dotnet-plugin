package jetbrains.buildServer.dotCover

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.tools.ServerToolManager
import jetbrains.buildServer.tools.ToolVersion
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverRequirementsProviderTest {
    @MockK private lateinit var _projectManagerMock: ProjectManager
    @MockK private lateinit var _toolManagerMock: ServerToolManager
    private lateinit var _instance: DotCoverRequirementsProvider;

    @BeforeMethod
    fun setUp() {
        clearAllMocks()
        MockKAnnotations.init(this, relaxed = true)
        _instance = DotCoverRequirementsProvider(_projectManagerMock, _toolManagerMock)
    }

    @Test
    fun `should not provide requirements when docker image parameter is present`() {
        // act
        val actualRequirements = _instance.getRequirements(mapOf(DotnetConstants.PARAM_DOCKER_IMAGE to "abc"))

        // assert
        assertEquals(0, actualRequirements.toList().size)
    }

    @Test
    fun `should not provide requirements when dotCover home is not present`() {
        // act
        val actualRequirements = _instance.getRequirements(mapOf())

        // assert
        assertEquals(0, actualRequirements.toList().size)
    }

    @Test
    fun `should not provide requirements when tool has not been found`() {
        // arrange
        val parameters = mapOf(CoverageConstants.PARAM_DOTCOVER_HOME to "/path/to/dotCover")
        every { _toolManagerMock.findToolType(any()) } returns null

        // act
        val actualRequirements = _instance.getRequirements(parameters)

        // assert
        assertEquals(0, actualRequirements.toList().size)
    }

    @Test
    fun `should not provide requirements when tool version has not been resolved`() {
        // arrange
        val parameters = mapOf(CoverageConstants.PARAM_DOTCOVER_HOME to "/path/to/dotCover")
        _toolManagerMock.also {
            every { it.findToolType(any()) } returns mockk()
            every { it.resolveToolVersionReference(any(), any(), any()) } returns null
        }

        // act
        val actualRequirements = _instance.getRequirements(parameters)

        // assert
        assertEquals(0, actualRequirements.toList().size)
    }

    @DataProvider
    fun `tool version types and corresponding requirements`() = arrayOf(
        arrayOf(DotCoverToolVersionType.UsingBundledRuntime, emptyList<Requirement>()),
        arrayOf(DotCoverToolVersionType.UsingAgentRuntime, emptyList<Requirement>()),
        arrayOf(DotCoverToolVersionType.UsingDotNetFramework472, listOf<Requirement>(
            Requirement("Exists=>(DotNetFramework4\\.(7\\.([2-9]|[\\d]{2,})|[8-9]|[\\d]{2,})[\\d\\.]*_.+)", null, RequirementType.EXISTS)
        )),
        arrayOf(DotCoverToolVersionType.UsingDotNetFramework461, listOf<Requirement>(
            Requirement("Exists=>(DotNetFramework4\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_.+)", null, RequirementType.EXISTS)
        )),
        arrayOf(DotCoverToolVersionType.UsingDotNetFramework40, listOf<Requirement>(
            Requirement("Exists=>(DotNetFramework3\\.5_.+|DotNetFramework4\\.[\\d\\.]+_.+)", null, RequirementType.EXISTS)
        )),
        arrayOf(DotCoverToolVersionType.Unknown, emptyList<Requirement>()),
    )
    @Test(dataProvider = "tool version types and corresponding requirements")
    fun `should provide requirements according to the tool version type`(toolVersionType: DotCoverToolVersionType, expecedRequirements: List<Requirement>) {
        // arrange
        val parameters = mapOf(CoverageConstants.PARAM_DOTCOVER_HOME to "/path/to/dotCover")
        _toolManagerMock.also {
            every { it.findToolType(any()) } returns mockk()
            every { it.resolveToolVersionReference(any(), any(), any()) } returns mockk<ToolVersion>(relaxed = true)
        }
        mockkObject(DotCoverToolVersionType.Companion)
        every { DotCoverToolVersionType.determine(any()) } returns toolVersionType

        // act
        val actualRequirements = _instance.getRequirements(parameters)

        // assert
        assertEquals(expecedRequirements, actualRequirements.toList())
    }

    @DataProvider
    fun `requirement patterns and examples`() = arrayOf(
        // DotNetFramework4\.(7\.(?!0)|[7-9]|[\d]{2,})[\d\.]*_.+
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework2.0_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework2.0_x64"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework2.0_x86"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework2.0_x64_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework2.0_x86_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework2.0.50727_x64_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework2.0.50727_x86_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework3.0_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework3.5_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework4.0_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework4.5_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework4.5.1_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework4.5.2_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework4.6_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework4.6.1_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework4.6.1_x64_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework4.6.1_x86_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework4.6.2_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework4.7_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, false, "DotNetFramework4.7.1_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, true, "DotNetFramework4.7.2_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, true, "DotNetFramework4.7.2_x64_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, true, "DotNetFramework4.7.2_x86_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, true, "DotNetFramework4.7.10_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, true, "DotNetFramework4.8_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, true, "DotNetFramework4.8.1_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, true, "DotNetFramework4.8.2_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, true, "DotNetFramework4.8.10_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, true, "DotNetFramework4.9_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, true, "DotNetFramework4.9.1_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, true, "DotNetFramework4.10_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_472_PATTERN, true, "DotNetFramework4.10.1_x"),

        // DotNetFramework4\.(6\.(?!0)|[7-9]|[\d]{2,})[\d\.]*_.+
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, false, "DotNetFramework2.0_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, false, "DotNetFramework2.0_x64"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, false, "DotNetFramework2.0_x86"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, false, "DotNetFramework2.0_x64_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, false, "DotNetFramework2.0_x86_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, false, "DotNetFramework2.0.50727_x64_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, false, "DotNetFramework2.0.50727_x86_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, false, "DotNetFramework3.0_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, false, "DotNetFramework3.5_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, false, "DotNetFramework4.0_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, false, "DotNetFramework4.5_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, false, "DotNetFramework4.5.1_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, false, "DotNetFramework4.5.2_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, false, "DotNetFramework4.6_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.6.1_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.6.1_x64_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.6.1_x86_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.6.2_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.7_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.7.1_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.7.2_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.7.2_x64_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.7.2_x86_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.7.10_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.8_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.8.1_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.8.2_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.8.10_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.9_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.9.1_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.10_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_461_PATTERN, true, "DotNetFramework4.10.1_x"),

        // DotNetFramework3\.5_.+|DotNetFramework4\.[\d\.]+_.+
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, false, "DotNetFramework2.0_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, false, "DotNetFramework2.0_x64"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, false, "DotNetFramework2.0_x86"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, false, "DotNetFramework2.0_x64_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, false, "DotNetFramework2.0_x86_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, false, "DotNetFramework2.0.50727_x64_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, false, "DotNetFramework2.0.50727_x86_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, false, "DotNetFramework3.0_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework3.5_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.0_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.5_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.5.1_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.5.2_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.6_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.6.1_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.6.1_x64_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.6.1_x86_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.6.2_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.7_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.7.1_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.7.2_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.7.2_x64_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.7.2_x86_Path"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.7.10_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.8_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.8.1_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.8.2_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.8.10_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.9_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.9.1_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.10_x"),
        arrayOf(DotCoverRequirementsProvider.DOTNET_FRAMEWORK_35_OR_40_PATTERN, true, "DotNetFramework4.10.1_x"),
    )
    @Test(dataProvider = "requirement patterns and examples")
    fun `should provide valid requirement patterns`(pattern: String, isMatch: Boolean, example: String) {
        // act, assert
        assertEquals(isMatch, pattern.toRegex().matches(example))
    }
}