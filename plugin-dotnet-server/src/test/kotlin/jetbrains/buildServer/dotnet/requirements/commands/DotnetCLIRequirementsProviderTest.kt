package jetbrains.buildServer.dotnet.requirements.commands

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.MonoConstants
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetCLIRequirementsProviderTest {
    @DataProvider
    fun testRequirementsData() = arrayOf(
        sequenceOf(Requirement(DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH, null, RequirementType.EXISTS)),
    )

    @Test(dataProvider = "testRequirementsData")
    fun shouldProvideRequirements(expectedRequirements: Sequence<Requirement>) {
        // arrange
        val instance = TestCommandDotnetCLIRequirementsProvider()

        // act
        val actualRequirements = instance.getRequirements(emptyMap())

        // assert
        Assert.assertEquals(actualRequirements.toList(), expectedRequirements.toList())
    }

    private class TestCommandDotnetCLIRequirementsProvider : DotnetCLIRequirementsProvider()
}