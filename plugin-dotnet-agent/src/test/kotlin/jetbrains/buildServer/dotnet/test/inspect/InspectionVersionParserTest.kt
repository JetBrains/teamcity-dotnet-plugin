

package jetbrains.buildServer.dotnet.test.inspect

import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.inspect.InspectionVersionParser
import org.testng.Assert.*
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class InspectionVersionParserTest {
    private val parser = InspectionVersionParser()

    @DataProvider(name = "cases")
    fun getCases(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                listOf(
                    "JetBrains Inspect Code 2021.2.3",
                    "Running on PROCESSOR_ARCHITECTURE_ARM64 in 64-bit mode, .NET runtime 6.0.19 under Unix 13.3.0",
                    "##teamcity[message text='Version: 2021.2.3' status='NORMAL' timestamp='2023-08-02T12:46:48.367+0000' flowId='4194304']"
                ),
                Version(2021, 2, 3)
            ),
            arrayOf(
                listOf(
                    "JetBrains Inspect Code 2021.2.3",
                    "Running on PROCESSOR_ARCHITECTURE_ARM64 in 64-bit mode, .NET runtime 6.0.19 under Unix 13.3.0",
                    "Version: 2021.2.3"
                ),
                Version(2021, 2, 3)
            ),
            arrayOf(
                // called without --version flag
                listOf(
                    "JetBrains Inspect Code 2021.2.3\n",
                    "Running on PROCESSOR_ARCHITECTURE_ARM64 in 64-bit mode, .NET runtime 6.0.19 under Unix 13.3.0\n",
                    "Solution file is not specified\n",
                    "Usage: inspectcode [options] [solution or project file]\n",
                    "Show help: inspectcode --help"
                ),
                Version(0, 0, 0)
            ),
            arrayOf(
                listOf(
                    "Start of output",
                    "Version: 1.1.1",
                    "End of output"
                ),
                Version(1, 1, 1)
            ),
            arrayOf(
                listOf(
                    "Start of output",
                    "Version: 1.1.1"
                ),
                Version(1, 1, 1)
            ),
            arrayOf(
                listOf(
                    "Version: 1.1.1",
                    "End of output"
                ),
                Version(1, 1, 1)
            ),
            arrayOf(
                listOf(
                    "Start of output",
                    "End of output"
                ),
                Version.Empty
            ),
            arrayOf(
                listOf(
                    "Version:  \t  1.1.1"
                ),
                Version(1, 1, 1)
            ),
            arrayOf(
                listOf(
                    "Version:\n1.1.1"
                ),
                Version.Empty
            ),
            arrayOf(
                listOf(
                    "Version:\r1.1.1"
                ),
                Version.Empty
            ),
            arrayOf(
                listOf(
                    "\nVersion: 1.1.1"
                ),
                Version.Empty
            ),
            arrayOf(
                listOf(
                    " Version: 1.1.1"
                ),
                Version.Empty
            ),
            arrayOf(
                listOf(
                    "SomeVersion: 1.1.1"
                ),
                Version.Empty
            )
        )
    }

    @Test(dataProvider = "cases")
    fun `should parse version when version line exists and its format is correct`(versionLines: List<String>, expectedVersion: Version) {
        // arrange

        // act
        val parsedVersion = parser.parse(versionLines)

        // assert
        assertEquals(parsedVersion, expectedVersion)
    }
}