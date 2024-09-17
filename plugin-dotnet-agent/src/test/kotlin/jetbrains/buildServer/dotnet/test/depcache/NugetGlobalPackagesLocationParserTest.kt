package jetbrains.buildServer.dotnet.test.depcache

import jetbrains.buildServer.depcache.utils.NugetGlobalPackagesLocationParser
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import kotlin.math.exp

class NugetGlobalPackagesLocationParserTest {

    @DataProvider
    fun getCommandLineOutputTestData(): Array<Array<Any?>> {
        return arrayOf(
            arrayOf(
                "global-packages: /Users/You/.nuget/packages",
                "/Users/You/.nuget/packages"
            ),
            arrayOf(
                "global-packages:/Users/You/.nuget/packages",
                "/Users/You/.nuget/packages"
            ),
            arrayOf(
                "/Users/You/.nuget/packages",
                null
            )
        )
    }

    @Test(dataProvider = "getCommandLineOutputTestData")
    fun `should parse nuget packages location from command line output`(commandLineOutput: String, expected: String?) {
        // arrange, act
        val result = NugetGlobalPackagesLocationParser.fromCommandLineOutput(commandLineOutput)

        // assert
        Assert.assertEquals(result, expected)
    }
}