package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.NuGetPackageVersion
import jetbrains.buildServer.dotnet.NuGetPackageVersionParser
import jetbrains.buildServer.dotnet.NuGetPackageVersionParserImpl
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class NuGetPackageVersionParserTest {
    @DataProvider
    fun getProjectFiles(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf<Any?>("3.2.4", NuGetPackageVersion(3, 2, 4)),
                arrayOf<Any?>("11133.42.444", NuGetPackageVersion(11133, 42, 444)),
                arrayOf<Any?>("3.2.4-Beta", NuGetPackageVersion(3, 2, 4, "Beta")),
                arrayOf<Any?>("333.222.54-beTa", NuGetPackageVersion(333, 222, 54, "beTa")),
                arrayOf<Any?>("3.2.4-rc", NuGetPackageVersion(3, 2, 4, "rc")),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.123.0.18-BEta.nupkg", NuGetPackageVersion(123, 0, 18, "BEta")),
                arrayOf<Any?>("path\\TeamCity.Dotnet.Integration.123.0.18-BEta.nupkg", null),
                arrayOf<Any?>("path/TeamCity.Dotnet.Integration.123.0.18-BEta.nupkg", null),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.41.30.18.nupkg", NuGetPackageVersion(41, 30, 18)),
                arrayOf<Any?>("Integration.41.30.18.nupkg", NuGetPackageVersion(41, 30, 18)),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.41.30.18.33.nupkg", null),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.41.30.18.33-rc.nupkg", null),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.41.30.nupkg", null),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.41.30-Beta.nupkg", null),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.41.nupkg", null),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.41-aa.nupkg", null),
                arrayOf<Any?>("3.2.4#rc", null),
                arrayOf<Any?>("3.2.4-", null),
                arrayOf<Any?>("3.2.4- abc ", null),
                arrayOf<Any?>("3.2.4-a b", null),
                arrayOf<Any?>("", null),
                arrayOf<Any?>("abc", null),
                arrayOf<Any?>("3", null),
                arrayOf<Any?>("3-abc", null),
                arrayOf<Any?>("3.4", null),
                arrayOf<Any?>("3.4-ddd", null),
                arrayOf<Any?>("3.2.4.6", null)
        )
    }

    @Test(dataProvider = "getProjectFiles")
    fun shouldParsePackageVersion(versionString: String, expectedPackageVersion: NuGetPackageVersion?) {
        // Given
        var parser = CreateInstance()

        // When
        val actualPackageVersion = parser.tryParse(versionString)

        // Then
        Assert.assertEquals(actualPackageVersion, expectedPackageVersion)
    }

    fun CreateInstance() : NuGetPackageVersionParser {
        return NuGetPackageVersionParserImpl()
    }
}