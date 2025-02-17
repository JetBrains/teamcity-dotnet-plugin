package jetbrains.buildServer.inspect

import io.mockk.*
import jetbrains.buildServer.ToolVersionProvider
import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.TeamCityProperties
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class ReSharperRequirementsProviderTest {

    @BeforeMethod
    fun setUp() {
        mockkStatic(TeamCityProperties::class)
    }

    @AfterMethod
    fun tearDown() {
        unmockkStatic(TeamCityProperties::class)
    }

    @Test
    fun `should provide requirements`() {
        // arrange
        val cltPath = "cltPath"
        val platform = InspectionToolPlatform.CrossPlatform
        val parameters = mapOf(
            CltConstants.CLT_PATH_PARAMETER to cltPath,
            CltConstants.RUNNER_SETTING_CLT_PLATFORM to platform.id
        )
        val toolVersionMock = mockk<Version>()
        val toolVersionProviderMock = mockk<ToolVersionProvider>() {
            every { getVersion(any(), any()) } returns toolVersionMock
        }
        val resolvedRequirements = sequenceOf(mockk<Requirement>())
        val requirementsResolverMock = mockk<RequirementsResolver>() {
            every { resolve(any(), any()) } returns resolvedRequirements
        }
        val instance = ReSharperRequirementsProvider(toolVersionProviderMock, requirementsResolverMock)

        // act
        val actualRequirements = instance.getRequirements(parameters)

        // assert
        assertEquals(actualRequirements.toList(), resolvedRequirements.toList())
        verify(exactly = 1) { toolVersionProviderMock.getVersion(cltPath, CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID) }
        verify(exactly = 1) { requirementsResolverMock.resolve(toolVersionMock, platform) }
    }

    @Test
    fun `should not provide requirements when disabled`() {
        // arrange
        every { TeamCityProperties.getBooleanOrTrue(eq(CltConstants.CLT_AGENT_REQUIREMENTS_ENABLED)) } returns false
        val parameters = emptyMap<String, String>()
        val toolVersionProviderMock = mockk<ToolVersionProvider>(relaxed = true)
        val requirementsResolverMock = mockk<RequirementsResolver>(relaxed = true)
        val instance = ReSharperRequirementsProvider(toolVersionProviderMock, requirementsResolverMock)

        // act
        val actualRequirements = instance.getRequirements(parameters)

        // assert
        assertTrue(actualRequirements.toList().isEmpty())
        verify(exactly = 0) { requirementsResolverMock.resolve(any(), any()) }
    }

    @Test
    fun `should use Windows x64 if platform hasn't been passed`() {
        // arrange
        val parameters = mapOf(CltConstants.CLT_PATH_PARAMETER to "")
        val toolVersionProviderMock = mockk<ToolVersionProvider>(relaxed = true)
        val requirementsResolverMock = mockk<RequirementsResolver>(relaxed = true)
        val instance = ReSharperRequirementsProvider(toolVersionProviderMock, requirementsResolverMock)

        // act
        instance.getRequirements(parameters)

        // assert
        verify(exactly = 1) { requirementsResolverMock.resolve(any(), InspectionToolPlatform.WindowsX64) }
    }
}