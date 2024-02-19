package jetbrains.buildServer.inspect

import io.mockk.*
import jetbrains.buildServer.ToolVersionProvider
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.MonoConstants
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.dotnet.requirements.RequiredDotnetSDKRequirementsProvider
import jetbrains.buildServer.dotnet.requirements.SDKBasedRequirementFactory
import jetbrains.buildServer.dotnet.requirements.commands.MSBuildRequirementsProviderTest
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import org.testng.Assert
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ReSharperRequirementsProviderTest {
    @Test
    fun shouldProvideRequirements() {
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