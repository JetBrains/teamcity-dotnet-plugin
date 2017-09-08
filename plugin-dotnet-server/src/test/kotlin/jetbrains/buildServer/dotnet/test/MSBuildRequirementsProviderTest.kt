package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.dotnet.DotCoverConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.dotnet.commands.DotCoverInfoProvider
import jetbrains.buildServer.dotnet.commands.MSBuildRequirementsProvider
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildRequirementsProviderTest {
    @DataProvider
    fun testRequirementsData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild12WindowsX64.id), sequenceOf(Requirement("MSBuildTools12.0_x64_Path", null, RequirementType.EXISTS), windowsReq)),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild14WindowsX86.id), sequenceOf(Requirement("MSBuildTools14.0_x86_Path", null, RequirementType.EXISTS), windowsReq)),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX64.id), sequenceOf(Requirement("MSBuildTools15.0_x64_Path", null, RequirementType.EXISTS), windowsReq)),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX86.id), sequenceOf(Requirement("MSBuildTools15.0_x86_Path", null, RequirementType.EXISTS), windowsReq)),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id), sequenceOf(Requirement("MSBuildTools15\\.0_.+_Path", null, RequirementType.MATCHES), windowsReq)),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15CrossPlatform.id), sequenceOf(Requirement(DotnetConstants.CONFIG_PATH, null, RequirementType.EXISTS))))
    }

    @Test(dataProvider = "testRequirementsData")
    fun shouldProvideRequirements(
            parameters: Map<String, String>,
            expectedRequirements: Sequence<Requirement>) {
        // Given
        val instance = createInstance()

        // When
        val actualRequirements = instance.getRequirements(parameters)

        // Then
        Assert.assertEquals(actualRequirements.toList(), expectedRequirements.toList())
    }

    private fun createInstance(): MSBuildRequirementsProvider {
        return MSBuildRequirementsProvider(DotCoverInfoProvider())
    }

    companion object {
        val windowsReq = Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH)
    }
}