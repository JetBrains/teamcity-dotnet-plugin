package jetbrains.buildServer.dotnet.requirements.commands

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.MonoConstants
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MSBuildRequirementsProviderTest {
    @DataProvider
    fun testRequirementsData() = arrayOf(
        arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuildMono.id), sequenceOf(Requirement(MonoConstants.CONFIG_PATH, null, RequirementType.EXISTS))),
        arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild12WindowsX64.id), sequenceOf(Requirement("MSBuildTools12.0_x64_Path", null, RequirementType.EXISTS), windowsReq)),
        arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild14WindowsX86.id), sequenceOf(Requirement("MSBuildTools14.0_x86_Path", null, RequirementType.EXISTS), windowsReq)),
        arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX64.id), sequenceOf(Requirement("MSBuildTools15.0_x64_Path", null, RequirementType.EXISTS), windowsReq)),
        arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX86.id), sequenceOf(Requirement("MSBuildTools15.0_x86_Path", null, RequirementType.EXISTS), windowsReq)),
        arrayOf(emptyMap<String, String>(), sequenceOf(Requirement(DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH, null, RequirementType.EXISTS))),
        arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuildCrossPlatform.id), sequenceOf(Requirement(DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH, null, RequirementType.EXISTS)))
    )

    @Test(dataProvider = "testRequirementsData")
    fun shouldProvideRequirements(parameters: Map<String, String>, expectedRequirements: Sequence<Requirement>) {
        // arrange
        val instance = MSBuildRequirementsProvider()

        // act
        val actualRequirements = instance.getRequirements(parameters)

        // assert
        Assert.assertEquals(actualRequirements.toList(), expectedRequirements.toList())
    }

    companion object {
        val windowsReq = Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH)
    }
}