package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.MonoConstants
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.dotnet.commands.MSBuildCommandType
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import org.jmock.Mockery
import org.springframework.beans.factory.BeanFactory
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MSBuildCommandTypeTest {
    @DataProvider
    fun testRequirementsData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Mono.id), sequenceOf(Requirement(MonoConstants.CONFIG_PATH, null, RequirementType.EXISTS))),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild12WindowsX64.id), sequenceOf(Requirement("MSBuildTools12.0_x64_Path", null, RequirementType.EXISTS), windowsReq)),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild14WindowsX86.id), sequenceOf(Requirement("MSBuildTools14.0_x86_Path", null, RequirementType.EXISTS), windowsReq)),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX64.id), sequenceOf(Requirement("MSBuildTools15.0_x64_Path", null, RequirementType.EXISTS), windowsReq)),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX86.id), sequenceOf(Requirement("MSBuildTools15.0_x86_Path", null, RequirementType.EXISTS), windowsReq)),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id), sequenceOf(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "MSBuildTools15\\.0_.+_Path", null, RequirementType.EXISTS), windowsReq)),
                arrayOf(emptyMap<String, String>(), sequenceOf(Requirement(DotnetConstants.CONFIG_PATH, null, RequirementType.EXISTS))),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15DotnetCore.id), sequenceOf(Requirement(DotnetConstants.CONFIG_PATH, null, RequirementType.EXISTS))))
    }

    @Test(dataProvider = "testRequirementsData")
    fun shouldProvideRequirements(
            parameters: Map<String, String>,
            expectedRequirements: Sequence<Requirement>) {
        // Given
        val instance = MSBuildCommandType()
        val ctx = Mockery()

        // When
        val actualRequirements = instance.getRequirements(parameters, ctx.mock(BeanFactory::class.java))

        // Then
        Assert.assertEquals(actualRequirements.toList(), expectedRequirements.toList())
    }

    companion object {
        val windowsReq = Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH)
    }
}