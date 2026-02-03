package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.NuGetPackageVersion
import jetbrains.buildServer.dotnet.NuGetPackageVersionParserImpl
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class NuGetPackageVersionParserTest {

    @DataProvider
    fun getProjectFiles() = arrayOf(
        arrayOf("3.2.4", NuGetPackageVersion(3, 2, 4)),
        arrayOf("11133.42.444", NuGetPackageVersion(11133, 42, 444)),
        arrayOf("3.2.4-Beta", NuGetPackageVersion(3, 2, 4, "Beta")),
        arrayOf("333.222.54-beTa", NuGetPackageVersion(333, 222, 54, "beTa")),
        arrayOf("3.2.4-rc", NuGetPackageVersion(3, 2, 4, "rc")),
        arrayOf("2021.2.0-eap01", NuGetPackageVersion(2021, 2, 0, "eap01")),
        arrayOf("2021.2.0-eap0.1", NuGetPackageVersion(2021, 2, 0, "eap0.1")),
        arrayOf("TeamCity.Dotnet.Integration.123.0.18-BEta.nupkg", NuGetPackageVersion(123, 0, 18, "BEta")),
        arrayOf("path\\TeamCity.Dotnet.Integration.123.0.18-BEta.nupkg", null),
        arrayOf("path/TeamCity.Dotnet.Integration.123.0.18-BEta.nupkg", null),
        arrayOf("TeamCity.Dotnet.Integration.41.30.18.nupkg", NuGetPackageVersion(41, 30, 18)),
        arrayOf("Integration.41.30.18.nupkg", NuGetPackageVersion(41, 30, 18)),
        arrayOf("TeamCity.csi.1.23.4-beta1.nupkg", NuGetPackageVersion(1, 23, 4, "beta1")),
        arrayOf("TeamCity.Dotnet.Integration.41.30.18.33.nupkg", NuGetPackageVersion(41, 30, 18, 33)),
        arrayOf("TeamCity.Dotnet.Integration.41.30.18.33-rc.nupkg", NuGetPackageVersion(41, 30, 18, 33, "rc")),
        arrayOf("TeamCity.Dotnet.Integration.41.30.nupkg", null),
        arrayOf("TeamCity.Dotnet.Integration.41.30-Beta.nupkg", null),
        arrayOf("TeamCity.Dotnet.Integration.41.nupkg", null),
        arrayOf("TeamCity.Dotnet.Integration.41-aa.nupkg", null),
        arrayOf("3.2.4#rc", null),
        arrayOf("3.2.4-", null),
        arrayOf("3.2.4- abc ", null),
        arrayOf("3.2.4-a b", null),
        arrayOf("", null),
        arrayOf("abc", null),
        arrayOf("3", null),
        arrayOf("3-abc", null),
        arrayOf("3.4", null),
        arrayOf("3.4-ddd", null),
        arrayOf("3.2.4.6", NuGetPackageVersion(3, 2, 4, 6)),
        arrayOf("1.0.4", NuGetPackageVersion(1, 0, 4)),
        arrayOf("NuGetFallbackFolder", null),
        arrayOf("TeamCity.Dotnet.Integration.2022.10.03-alpha.zip", NuGetPackageVersion(2022, 10, 3, "alpha")),
        arrayOf("TeamCity.Dotnet.Integration.2022.10.3.zip", NuGetPackageVersion(2022, 10, 3)),
        arrayOf("TeamCity.Dotnet.Integration.2022.10.0.zip", NuGetPackageVersion(2022, 10, 0)),
        arrayOf("TeamCity.Dotnet.Integration.2022.10.0", NuGetPackageVersion(2022, 10, 0)),
        arrayOf("TeamCity.Dotnet.Integration.2022.10.1.g4", NuGetPackageVersion(2022, 10, 1)),
        arrayOf("TeamCity.Dotnet.Integration.2022.10.1.a", NuGetPackageVersion(2022, 10, 1)),
        arrayOf("TeamCity.Dotnet.Integration.2022.10.1.0", NuGetPackageVersion(2022, 10, 1, 0)),
        arrayOf("TeamCity.Dotnet.Integration.2022.10.1.", null),
        arrayOf("JetBrains.dotCover.CommandLineTools.2023.1.0", NuGetPackageVersion(2023, 1, 0)),
        arrayOf("JetBrains.dotCover.CommandLineTools.2023.1.0.zip", NuGetPackageVersion(2023, 1, 0)),
        arrayOf("JetBrains.dotCover.CommandLineTools.2023.1.0.tar.gz", NuGetPackageVersion(2023, 1, 0)),
        arrayOf("JetBrains.dotCover.CommandLineTools.2023.2.1-eap03", NuGetPackageVersion(2023, 2, 1, "eap03")),
        arrayOf("JetBrains.dotCover.DotNetCliTool.2022.3.0-eap07", NuGetPackageVersion(2022, 3, 0, "eap07")),
        arrayOf("JetBrains.dotCover.DotNetCliTool.2020.3.3", NuGetPackageVersion(2020, 3, 3)),
    )

    @Test(dataProvider = "getProjectFiles")
    fun `should parse package version`(versionString: String, expectedPackageVersion: NuGetPackageVersion?) {
        // arrange
        val parser = NuGetPackageVersionParserImpl()

        // act
        val actualPackageVersion = parser.tryParse(versionString)

        // assert
        Assert.assertEquals(actualPackageVersion, expectedPackageVersion)
    }
}