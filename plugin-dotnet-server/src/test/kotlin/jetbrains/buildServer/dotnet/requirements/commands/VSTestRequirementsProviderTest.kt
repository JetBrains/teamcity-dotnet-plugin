package jetbrains.buildServer.dotnet.requirements.commands

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class VSTestRequirementsProviderTest {
    @DataProvider
    fun testRequirementsData() = arrayOf(
        arrayOf(mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest12Windows.id), sequenceOf(Requirement("teamcity.dotnet.vstest.12.0", null, RequirementType.EXISTS), windowsReq)),
        arrayOf(mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest14Windows.id), sequenceOf(Requirement("teamcity.dotnet.vstest.14.0", null, RequirementType.EXISTS), windowsReq)),
        arrayOf(mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest15Windows.id), sequenceOf(Requirement("teamcity.dotnet.vstest.15.0", null, RequirementType.EXISTS), windowsReq)),
        arrayOf(emptyMap<String, String>(), sequenceOf(Requirement(DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH, null, RequirementType.EXISTS))),
        arrayOf(mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTestCrossPlatform.id), sequenceOf(Requirement(DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH, null, RequirementType.EXISTS)))
    )

    @Test(dataProvider = "testRequirementsData")
    fun shouldProvideRequirements(parameters: Map<String, String>, expectedRequirements: Sequence<Requirement>) {
        // arrange
        val instance = VSTestRequirementsProvider()

        // act
        val actualRequirements = instance.getRequirements(parameters)

        // assert
        assertEquals(actualRequirements.toList(), expectedRequirements.toList())
    }

    companion object {
        val windowsReq = Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH)
    }
}