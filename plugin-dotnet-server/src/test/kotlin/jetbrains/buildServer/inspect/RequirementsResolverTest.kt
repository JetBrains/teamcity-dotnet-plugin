package jetbrains.buildServer.inspect

import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.requirements.Requirement
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class RequirementsResolverTest {

    @DataProvider
    fun resolveRequirementsTestData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                Version(2018, 1),
                InspectionToolPlatform.WindowsX64,
                listOf(RequirementsResolverImpl.DotnetFrameworkAnyX64Requirement)
            ),
            arrayOf(
                Version(2018, 1),
                InspectionToolPlatform.WindowsX86,
                listOf(RequirementsResolverImpl.DotnetFrameworkAnyX86Requirement)
            ),
            arrayOf(
                Version(2018, 2),
                InspectionToolPlatform.WindowsX64,
                listOf(RequirementsResolverImpl.DotnetFramework461AndAboveX64Requirement)
            ),
            arrayOf(
                Version(2018, 2),
                InspectionToolPlatform.WindowsX86,
                listOf(RequirementsResolverImpl.DotnetFramework461AndAboveX86Requirement)
            ),
            arrayOf(
                Version(2020, 2, 1),
                InspectionToolPlatform.CrossPlatform,
                listOf(RequirementsResolverImpl.DotnetCoreRuntime31AndAboveRequirement)
            )
        )
    }

    @Test(dataProvider = "resolveRequirementsTestData")
    fun `should resolve requrements based on dotnet version and tool platform`(version: Version, platform: InspectionToolPlatform, expectedRequierements: Collection<Requirement>) {
        // arrange
        val resolver = RequirementsResolverImpl()

        // act
        val actualRequirements = resolver.resolve(version, platform).toList()

        // assert
        Assert.assertEquals(actualRequirements, expectedRequierements)
    }

    @DataProvider
    fun dotnetCoreRuntime31PathTestData(): Array<Array<Any>> {
        return arrayOf(
            // --- MATCHING VERSIONS ---
            // min allowed major, min allowed minor
            arrayOf("DotNetCoreRuntime3.1_Path", true),
            arrayOf("DotNetCoreRuntime3.1.32_Path", true),
            arrayOf("DotNetCoreRuntime3.1.32-preview.6.23329.7_Path", true),
            arrayOf("DotNetCoreRuntime3.1.32-rc.2.24473.5_Path", true),

            // min allowed major, some minor
            arrayOf("DotNetCoreRuntime3.5_Path", true),
            arrayOf("DotNetCoreRuntime3.5.32_Path", true),
            arrayOf("DotNetCoreRuntime3.5.32-preview.6.23329.7_Path", true),
            arrayOf("DotNetCoreRuntime3.5.32-rc.2.24473.5_Path", true),

            // min allowed major > 4, min allowed minor
            arrayOf("DotNetCoreRuntime5.0_Path", true),
            arrayOf("DotNetCoreRuntime5.0.19_Path", true),
            arrayOf("DotNetCoreRuntime5.0.19-preview.6.23329.7_Path", true),
            arrayOf("DotNetCoreRuntime5.0.19-rc.2.24473.5_Path", true),

            // min allowed major > 4, some minor
            arrayOf("DotNetCoreRuntime5.15_Path", true),
            arrayOf("DotNetCoreRuntime5.15.19_Path", true),
            arrayOf("DotNetCoreRuntime5.15.19-preview.6.23329.7_Path", true),
            arrayOf("DotNetCoreRuntime5.15.19-rc.2.24473.5_Path", true),

            // last allowed 1-digit major, min allowed minor
            arrayOf("DotNetCoreRuntime9.0_Path", true),
            arrayOf("DotNetCoreRuntime9.0.0_Path", true),
            arrayOf("DotNetCoreRuntime9.0.0-preview.6.23329.7_Path", true),
            arrayOf("DotNetCoreRuntime9.0.0-rc.2.24473.5_Path", true),

            // last allowed 1-digit major, some minor
            arrayOf("DotNetCoreRuntime9.6_Path", true),
            arrayOf("DotNetCoreRuntime9.6.0_Path", true),
            arrayOf("DotNetCoreRuntime9.6.0-preview.6.23329.7_Path", true),
            arrayOf("DotNetCoreRuntime9.6.0-rc.2.24473.5_Path", true),

            // first allowed 2-digit major, min allowed minor
            arrayOf("DotNetCoreRuntime10.0_Path", true),
            arrayOf("DotNetCoreRuntime10.0.18_Path", true),
            arrayOf("DotNetCoreRuntime10.0.18-preview.6.23329.7_Path", true),
            arrayOf("DotNetCoreRuntime10.0.18-rc.2.24473.5_Path", true),

            // first allowed 2-digit major, some minor
            arrayOf("DotNetCoreRuntime10.3_Path", true),
            arrayOf("DotNetCoreRuntime10.3.18_Path", true),
            arrayOf("DotNetCoreRuntime10.3.18-preview.6.23329.7_Path", true),
            arrayOf("DotNetCoreRuntime10.3.18-rc.2.24473.5_Path", true),


            // --- NON-MATCHING VERSIONS ---
            // one major below allowed
            arrayOf("DotNetCoreRuntime2.9_Path", false),
            arrayOf("DotNetCoreRuntime2.9.0_Path", false),
            arrayOf("DotNetCoreRuntime2.9.0-preview.6.23329.7_Path", false),
            arrayOf("DotNetCoreRuntime2.9.0-rc.2.24473.5_Path", false),

            // allowed major 3 but w/o minor
            arrayOf("DotNetCoreRuntime3_Path", false),

            // one minor below allowed
            arrayOf("DotNetCoreRuntime3.0_Path", false),
            arrayOf("DotNetCoreRuntime3.0.9_Path", false),
            arrayOf("DotNetCoreRuntime3.0.9-preview.6.23329.7_Path", false),
            arrayOf("DotNetCoreRuntime3.0.9-rc.2.24473.5_Path", false),

            // non-allowed major 4
            arrayOf("DotNetCoreRuntime4_Path", false),
            arrayOf("DotNetCoreRuntime4.1.2_Path", false),
            arrayOf("DotNetCoreRuntime4.1.2-preview.6.23329.7_Path", false),
            arrayOf("DotNetCoreRuntime4.1.2-rc.2.24473.5_Path", false),

            // min allowed major > 4, but w/o minor
            arrayOf("DotNetCoreRuntime5_Path", false),

            // last allowed 1-digit major, but w/o minor
            arrayOf("DotNetCoreRuntime9_Path", false),

            // first allowed 2-digit major, but w/o minor
            arrayOf("DotNetCoreRuntime10_Path", false),

            // no path suffux
            arrayOf("DotNetCoreRuntime6.1", false),

            // not a runtime
            arrayOf("DotNetCoreSDK6.1_Path", false),

            // invalid versions
            arrayOf("DotNetCoreRuntime.9_Path", false),
            arrayOf("DotNetCoreRuntime9._Path", false),
            arrayOf("DotNetCoreRuntime05.1_Path", false),
            arrayOf("DotNetCoreRuntime9..0_Path", false),
            arrayOf("DotNetCoreRuntime9..0-preview.6.23329.7_Path", false),
            arrayOf("DotNetCoreRuntime9-preview.6.23329.7_Path", false),
            arrayOf("DotNetCoreRuntime9..0-rc.2.24473.5_Path", false),
            arrayOf("DotNetCoreRuntime9-rc.2.24473.5_Path", false),
        )
    }

    @Test(dataProvider = "dotnetCoreRuntime31PathTestData")
    fun `should match dotnet core runtime path version 3_1 and above except 4_x`(string: String, expectedMatches: Boolean) {
        // act
        val actualMatches = Regex(RequirementsResolverImpl.DotnetCoreRuntime31AndAbovePathRegex).matches(string)

        // assert
        Assert.assertEquals(actualMatches, expectedMatches)
    }
}