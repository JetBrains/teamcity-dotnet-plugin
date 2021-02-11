package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.dotnet.discovery.Framework
import jetbrains.buildServer.dotnet.discovery.Property
import jetbrains.buildServer.dotnet.discovery.SdkResolverImpl
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class SdkResolverTest {
    @DataProvider
    fun resolveData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(Framework("net6.1"), emptyList<Property>(), "6.1"),
                arrayOf(Framework("net6.0"), emptyList<Property>(), "6"),
                arrayOf(Framework("net5.1"), emptyList<Property>(), "5.1"),
                arrayOf(Framework("net5.0"), emptyList<Property>(), "5"),

                arrayOf(Framework("netcoreapp3.1"), emptyList<Property>(), "5 3.1"),
                arrayOf(Framework("netcoreapp3.0"), emptyList<Property>(), "5 3"),
                arrayOf(Framework("netcoreapp2.2"), emptyList<Property>(), "5 3 2.2"),
                arrayOf(Framework("netcoreapp2.1"), emptyList<Property>(), "5 3 2.2 2.1"),
                arrayOf(Framework("netcoreapp2.0"), emptyList<Property>(), "5 3 2"),
                arrayOf(Framework("netcoreapp1.1"), emptyList<Property>(), "5 3 2 1.1"),
                arrayOf(Framework("netcoreapp1.0"), emptyList<Property>(), "5 3 2 1"),

                arrayOf(Framework("netstandard2.1"), emptyList<Property>(), "5 3"),
                arrayOf(Framework("netstandard2.0"), emptyList<Property>(), "5 3 2 4.8 4.7 4.6.2 4.6.1"),
                arrayOf(Framework("netstandard1.6"), emptyList<Property>(), "5 3 2 1 4.8 4.7 4.6.2 4.6.1"),
                arrayOf(Framework("netstandard1.5"), emptyList<Property>(), "5 3 2 1 4.8 4.7 4.6.2 4.6.1"),
                arrayOf(Framework("netstandard1.4"), emptyList<Property>(), "5 3 2 1 4.8 4.7 4.6.2 4.6.1"),
                arrayOf(Framework("netstandard1.3"), emptyList<Property>(), "5 3 2 1 4.8 4.7 4.6"),
                arrayOf(Framework("netstandard1.2"), emptyList<Property>(), "5 3 2 1 4.8 4.7 4.6 4.5.2 4.5.1"),
                arrayOf(Framework("netstandard1.1"), emptyList<Property>(), "5 3 2 1 4.8 4.7 4.6 4.5"),
                arrayOf(Framework("netstandard1.0"), emptyList<Property>(), "5 3 2 1 4.8 4.7 4.6 4.5"),

                arrayOf(Framework("net48"), emptyList<Property>(), "4.8"),
                arrayOf(Framework("net472"), emptyList<Property>(), "4.7.2"),
                arrayOf(Framework("net471"), emptyList<Property>(), "4.7.1"),
                arrayOf(Framework("net47"), emptyList<Property>(), "4.7"),
                arrayOf(Framework("net462"), emptyList<Property>(), "4.6.2"),
                arrayOf(Framework("net461"), emptyList<Property>(), "4.6.1"),
                arrayOf(Framework("net46"), emptyList<Property>(), "4.6"),
                arrayOf(Framework("net452"), emptyList<Property>(), "4.5.2"),
                arrayOf(Framework("net451"), emptyList<Property>(), "4.5.1"),
                arrayOf(Framework("net45"), emptyList<Property>(), "4.5"),
                arrayOf(Framework("net403"), emptyList<Property>(), "4.0.3"),
                arrayOf(Framework("net40"), emptyList<Property>(), "4.0"),
                arrayOf(Framework("net35"), emptyList<Property>(), "3.5"),
                arrayOf(Framework("net20"), emptyList<Property>(), ""),
                arrayOf(Framework("net11"), emptyList<Property>(), "")
        )
    }

    @Test(dataProvider = "resolveData")
    fun shouldResolveSdkVersions(framework: Framework, propeties: Collection<Property>, expectedSdkVersions: String) {
        // Given
        val resolver = SdkResolverImpl()

        // When
        val actualSdkVersions = resolver.resolveSdkVersions(framework, propeties).map { it.toString() }.joinToString(" ")

        // Then
        Assert.assertEquals(actualSdkVersions, expectedSdkVersions)
    }
}