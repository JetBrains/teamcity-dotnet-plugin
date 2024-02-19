package jetbrains.buildServer.dotnet.requirements.commands

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class VisualStudioRequirementsProviderTest {
    @DataProvider
    fun testRequirementsData() = arrayOf(
        arrayOf(mapOf(DotnetConstants.PARAM_VISUAL_STUDIO_VERSION to Tool.VisualStudio2013.id), sequenceOf(Requirement("VS2013_Path", null, RequirementType.EXISTS), windowsReq)),
        arrayOf(mapOf(DotnetConstants.PARAM_VISUAL_STUDIO_VERSION to Tool.VisualStudio2017.id), sequenceOf(Requirement("VS2017_Path", null, RequirementType.EXISTS), windowsReq)),
        arrayOf(mapOf(DotnetConstants.PARAM_VISUAL_STUDIO_VERSION to "abc"), sequenceOf(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "VS.+_Path", null, RequirementType.EXISTS), windowsReq)),
        arrayOf(mapOf(DotnetConstants.PARAM_VISUAL_STUDIO_VERSION to "  "), sequenceOf(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "VS.+_Path", null, RequirementType.EXISTS), windowsReq)),
        arrayOf(mapOf(DotnetConstants.PARAM_VISUAL_STUDIO_VERSION to ""), sequenceOf(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "VS.+_Path", null, RequirementType.EXISTS), windowsReq)),
        arrayOf(emptyMap<String, String>(), sequenceOf(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "VS.+_Path", null, RequirementType.EXISTS), windowsReq))
    )

    @Test(dataProvider = "testRequirementsData")
    fun shouldProvideRequirements(parameters: Map<String, String>, expectedRequirements: Sequence<Requirement>) {
        // arrange
        val instance = VisualStudioRequirementsProvider()

        // act
        val actualRequirements = instance.getRequirements(parameters)

        // assert
        Assert.assertEquals(actualRequirements.toList(), expectedRequirements.toList())
    }

    companion object {
        val windowsReq = Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH)
    }
}