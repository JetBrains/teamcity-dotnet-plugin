package jetbrains.buildServer.nunit

import jetbrains.buildServer.nunit.NUnitRunnerConstants.NUNIT_PACKAGE_ID
import jetbrains.buildServer.nunit.NUnitRunnerConstants.NUNIT_TOOL_TYPE_NAME
import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class NUnitToolVersionTest {
    data class TestCase(val name: String, val expectedIsValid: Boolean, val expectedVersion: String?)

    @DataProvider(name = "supportingNUnit3Cases")
    fun getCommandLineArgumentsTryInitializeCases() = arrayOf(
        TestCase("NUnit.Console-3.6.1.34.zip", true, "3.6.1.34"),
        TestCase("NUnit.Console-3.6.1.zip", true, "3.6.1"),
        TestCase("NUnit.Console-3.6.zip", true, "3.6"),
        TestCase("NUnit.Console-3.zip", true, "3"),
        TestCase("NUnit.ConsoleRunner.10.20.300.nupkg", true, "10.20.300"),
        TestCase("NUnit.Console-3.6.1-beta.zip", false, ""),
        TestCase("nunit.console-3.6.1.zip", true, "3.6.1"),
        TestCase("NUNIT.CoNsolE-3.6.1.ZiP", true, "3.6.1"),
        TestCase("NUnit.Console-.zip", false, null),
        TestCase("NUnit-3.6.1.zip", false, null),
        TestCase("NUnit.Console-3.6.nupkg", false, null),
        TestCase("", false, null),
    )

    @Test(dataProvider = "supportingNUnit3Cases")
    fun `should parse nunit version name`(testCase: TestCase) {
        // arrange, act
        val toolVersion = NUnitToolVersion(testCase.name, "http://abc.com")

        // assert
        assertEquals(testCase.expectedIsValid, toolVersion.isValid)
        if (toolVersion.isValid) {
            assertEquals(toolVersion.id, "$NUNIT_PACKAGE_ID.${testCase.expectedVersion}")
            assertEquals(toolVersion.version, testCase.expectedVersion)
            assertEquals(toolVersion.destinationFileName, testCase.name)
            assertEquals(toolVersion.displayName, "$NUNIT_TOOL_TYPE_NAME ${testCase.expectedVersion}")
        }
    }
}