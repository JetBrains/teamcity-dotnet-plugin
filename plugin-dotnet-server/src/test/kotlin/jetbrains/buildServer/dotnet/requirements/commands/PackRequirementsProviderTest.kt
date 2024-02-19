package jetbrains.buildServer.dotnet.requirements.commands

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class PackRequirementsProviderTest {
    @DataProvider
    fun testRequirementsData() = arrayOf(
        arrayOf(mapOf(DotnetConstants.PARAM_RUNTIME to "NOT_BLANK"), sequenceOf(dotnetCLIPathRequirement, Requirement(DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI, "2.0.0", RequirementType.VER_NO_LESS_THAN))),
        arrayOf(emptyMap<String, String>(), sequenceOf(dotnetCLIPathRequirement)),
    )

    @Test(dataProvider = "testRequirementsData")
    fun shouldProvideRequirements(parameters: Map<String, String>, expectedRequirements: Sequence<Requirement>) {
        // arrange
        val instance = PackRequirementsProvider()

        // act
        val actualRequirements = instance.getRequirements(parameters)

        // assert
        assertEquals(actualRequirements.toList(), expectedRequirements.toList())
    }

    companion object {
        private val dotnetCLIPathRequirement = Requirement(DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH, null, RequirementType.EXISTS)
    }
}