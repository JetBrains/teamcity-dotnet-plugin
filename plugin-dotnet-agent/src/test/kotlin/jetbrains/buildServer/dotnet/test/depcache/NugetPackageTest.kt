package jetbrains.buildServer.dotnet.test.depcache

import jetbrains.buildServer.depcache.Package
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class NugetPackageTest {

    @DataProvider
    fun getPackageTestData(): Array<Array<Any?>> {
        return arrayOf(
            arrayOf(
                "Serilog.Sinks.Console",
                "6.0.0",
                "Serilog.Sinks.Console:6.0.0"
            ),
            arrayOf(
                "Serilog.Sinks.Console",
                null,
                null
            ),
            arrayOf(
                null,
                "6.0.0",
                null
            )
        )
    }

    @Test(dataProvider = "getPackageTestData")
    fun `should compose package name`(packageId: String?, packageResolvedVersion: String?, expected: String?) {
        // arrange
        val nugetPackage = Package(id = packageId, resolvedVersion = packageResolvedVersion)

        // act
        val result = nugetPackage.packageCompositeName

        // assert
        Assert.assertEquals(result, expected)
    }
}