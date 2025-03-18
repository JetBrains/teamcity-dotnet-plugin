package jetbrains.buildServer.script

import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

@Test
class CSharpScriptRunTypeTest {

    @DataProvider
    fun matchingRuntimeVersionsTestData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                CSharpScriptRunType.DotnetCoreRuntime6Regex,
                listOf(
                    // .NET 6
                    "DotNetCoreRuntime6.0_Path",
                    "DotNetCoreRuntime6.3.0_Path",
                    "DotNetCoreRuntime6.123.12-preview.6.23329.7_Path",
                    "DotNetCoreRuntime6.0.6-rc.2.24473.5_Path",
                )
            ),
            arrayOf(
                CSharpScriptRunType.DotnetCoreRuntime6And7Regex,
                listOf(
                    // .NET 6
                    "DotNetCoreRuntime6.0_Path",
                    "DotNetCoreRuntime6.3.0_Path",
                    "DotNetCoreRuntime6.123.12-preview.6.23329.7_Path",
                    "DotNetCoreRuntime6.0.6-rc.2.24473.5_Path",

                    // .NET 7
                    "DotNetCoreRuntime7.0_Path",
                    "DotNetCoreRuntime7.3.0_Path",
                    "DotNetCoreRuntime7.123.12-preview.6.23329.7_Path",
                    "DotNetCoreRuntime7.0.6-rc.2.24473.5_Path",
                )
            ),
            arrayOf(
                CSharpScriptRunType.DotnetCoreRuntime6AndAboveRegex,
                listOf(
                    // .NET 6
                    "DotNetCoreRuntime6.0_Path",
                    "DotNetCoreRuntime6.3.0_Path",
                    "DotNetCoreRuntime6.123.12-preview.6.23329.7_Path",
                    "DotNetCoreRuntime6.0.6-rc.2.24473.5_Path",

                    // .NET 7
                    "DotNetCoreRuntime7.0_Path",
                    "DotNetCoreRuntime7.3.0_Path",
                    "DotNetCoreRuntime7.123.12-preview.6.23329.7_Path",
                    "DotNetCoreRuntime7.0.6-rc.2.24473.5_Path",

                    // .NET 8
                    "DotNetCoreRuntime8.0_Path",
                    "DotNetCoreRuntime8.3.0_Path",
                    "DotNetCoreRuntime8.123.12-preview.6.23329.7_Path",
                    "DotNetCoreRuntime8.0.6-rc.2.24473.5_Path",

                    // .NET 10
                    "DotNetCoreRuntime10.0_Path",
                    "DotNetCoreRuntime10.3.0_Path",
                    "DotNetCoreRuntime10.123.12-preview.6.23329.7_Path",
                    "DotNetCoreRuntime10.0.6-rc.2.24473.5_Path",
                )
            ),
        )
    }

    @Test(dataProvider = "matchingRuntimeVersionsTestData")
    fun `should match runtime versions when they correspond to regex`(regex: String, matchingStrings: List<String>) {
        // act, assert
        matchingStrings.forEach { Assert.assertTrue(Regex(regex).matches(it)) }
    }

    @DataProvider
    fun nonMatchingRuntimeVersionsTestData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                CSharpScriptRunType.DotnetCoreRuntime6Regex,
                listOf(
                    // .NET 5
                    "DotNetCoreRuntime5.15_Path",
                    "DotNetCoreRuntime5.15.19_Path",
                    "DotNetCoreRuntime5.15.19-preview.6.23329.7_Path",
                    "DotNetCoreRuntime5.15.19-rc.2.24473.5_Path",

                    // .NET 7
                    "DotNetCoreRuntime7.0_Path",
                    "DotNetCoreRuntime7.3.0_Path",
                    "DotNetCoreRuntime7.123.12-preview.6.23329.7_Path",
                    "DotNetCoreRuntime7.0.6-rc.2.24473.5_Path",

                    // .NET 8
                    "DotNetCoreRuntime8.0_Path",
                    "DotNetCoreRuntime8.3.0_Path",
                    "DotNetCoreRuntime8.123.12-preview.6.23329.7_Path",
                    "DotNetCoreRuntime8.0.6-rc.2.24473.5_Path",

                    // .NET 10
                    "DotNetCoreRuntime10.0_Path",
                    "DotNetCoreRuntime10.3.0_Path",
                    "DotNetCoreRuntime10.123.12-preview.6.23329.7_Path",
                    "DotNetCoreRuntime10.0.6-rc.2.24473.5_Path",

                    // --- UNIVERSALLY NON-MATCHING VERSIONS ---
                    // w/o minor
                    "DotNetCoreRuntime6_Path",

                    // no path suffux
                    "DotNetCoreRuntime6.1",

                    // not a runtime
                    "DotNetCoreSDK6.1_Path",

                    // invalid versions
                    "DotNetCoreRuntime.9_Path",
                    "DotNetCoreRuntime9._Path",
                    "DotNetCoreRuntime05.1_Path",
                    "DotNetCoreRuntime9..0_Path",
                    "DotNetCoreRuntime9..0-preview.6.23329.7_Path",
                    "DotNetCoreRuntime9-preview.6.23329.7_Path",
                    "DotNetCoreRuntime9..0-rc.2.24473.5_Path",
                    "DotNetCoreRuntime9-rc.2.24473.5_Path",
                )
            ),
            arrayOf(
                CSharpScriptRunType.DotnetCoreRuntime6And7Regex,
                listOf(
                    // .NET 5
                    "DotNetCoreRuntime5.15_Path",
                    "DotNetCoreRuntime5.15.19_Path",
                    "DotNetCoreRuntime5.15.19-preview.6.23329.7_Path",
                    "DotNetCoreRuntime5.15.19-rc.2.24473.5_Path",

                    // .NET 8
                    "DotNetCoreRuntime8.0_Path",
                    "DotNetCoreRuntime8.3.0_Path",
                    "DotNetCoreRuntime8.123.12-preview.6.23329.7_Path",
                    "DotNetCoreRuntime8.0.6-rc.2.24473.5_Path",

                    // .NET 10
                    "DotNetCoreRuntime10.0_Path",
                    "DotNetCoreRuntime10.3.0_Path",
                    "DotNetCoreRuntime10.123.12-preview.6.23329.7_Path",
                    "DotNetCoreRuntime10.0.6-rc.2.24473.5_Path",

                    // --- UNIVERSALLY NON-MATCHING VERSIONS ---
                    // w/o minor
                    "DotNetCoreRuntime6_Path",

                    // no path suffux
                    "DotNetCoreRuntime6.1",

                    // not a runtime
                    "DotNetCoreSDK6.1_Path",

                    // invalid versions
                    "DotNetCoreRuntime.9_Path",
                    "DotNetCoreRuntime9._Path",
                    "DotNetCoreRuntime05.1_Path",
                    "DotNetCoreRuntime9..0_Path",
                    "DotNetCoreRuntime9..0-preview.6.23329.7_Path",
                    "DotNetCoreRuntime9-preview.6.23329.7_Path",
                    "DotNetCoreRuntime9..0-rc.2.24473.5_Path",
                    "DotNetCoreRuntime9-rc.2.24473.5_Path",
                )
            ),
            arrayOf(
                CSharpScriptRunType.DotnetCoreRuntime6AndAboveRegex,
                listOf(
                    // .NET 5
                    "DotNetCoreRuntime5.15_Path",
                    "DotNetCoreRuntime5.15.19_Path",
                    "DotNetCoreRuntime5.15.19-preview.6.23329.7_Path",
                    "DotNetCoreRuntime5.15.19-rc.2.24473.5_Path",

                    // --- UNIVERSALLY NON-MATCHING VERSIONS ---
                    // w/o minor
                    "DotNetCoreRuntime6_Path",

                    // no path suffux
                    "DotNetCoreRuntime6.1",

                    // not a runtime
                    "DotNetCoreSDK6.1_Path",

                    // invalid versions
                    "DotNetCoreRuntime.9_Path",
                    "DotNetCoreRuntime9._Path",
                    "DotNetCoreRuntime05.1_Path",
                    "DotNetCoreRuntime9..0_Path",
                    "DotNetCoreRuntime9..0-preview.6.23329.7_Path",
                    "DotNetCoreRuntime9-preview.6.23329.7_Path",
                    "DotNetCoreRuntime9..0-rc.2.24473.5_Path",
                    "DotNetCoreRuntime9-rc.2.24473.5_Path",
                )
            )
        )
    }


    @Test(dataProvider = "nonMatchingRuntimeVersionsTestData")
    fun `should no match runtime versions when they don't correspond to regex`(regex: String, nonMatchingStrings: List<String>) {
        // act, assert
        nonMatchingStrings.forEach { Assert.assertFalse(Regex(regex).matches(it)) }
    }
}