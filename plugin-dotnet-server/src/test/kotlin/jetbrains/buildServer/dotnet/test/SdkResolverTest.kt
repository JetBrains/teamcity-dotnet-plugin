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
                arrayOf(Framework("net6.1"), emptyList<Property>(), listOf(Version(6, 1))),
                arrayOf(Framework("net6.0"), emptyList<Property>(), listOf(Version(6))),
                arrayOf(Framework("net5.1"), emptyList<Property>(), listOf(Version(5, 1))),
                arrayOf(Framework("net5.0"), emptyList<Property>(), listOf(Version(5))),

                arrayOf(Framework("netcoreapp3.1"), emptyList<Property>(), listOf(Version(5), Version(3, 1))),
                arrayOf(Framework("netcoreapp3.0"), emptyList<Property>(), listOf(Version(5), Version(3))),
                arrayOf(Framework("netcoreapp2.2"), emptyList<Property>(), listOf(Version(5), Version(3), Version(2, 2))),
                arrayOf(Framework("netcoreapp2.1"), emptyList<Property>(), listOf(Version(5), Version(3), Version(2, 2), Version(2, 1))),
                arrayOf(Framework("netcoreapp2.0"), emptyList<Property>(), listOf(Version(5), Version(3), Version(2))),
                arrayOf(Framework("netcoreapp1.1"), emptyList<Property>(), listOf(Version(5), Version(3), Version(2), Version(1, 1))),
                arrayOf(Framework("netcoreapp1.0"), emptyList<Property>(), listOf(Version(5), Version(3), Version(2), Version(1))),

                arrayOf(Framework("netstandard2.1"), emptyList<Property>(), listOf(Version(5), Version(3))),
                arrayOf(Framework("netstandard2.0"), emptyList<Property>(), listOf(Version(5), Version(3), Version(2), Version(4, 8), Version(4, 7), Version(4, 6, 2), Version(4, 6, 1))),
                arrayOf(Framework("netstandard1.6"), emptyList<Property>(), listOf(Version(5), Version(3), Version(2), Version(1), Version(4, 8), Version(4, 7), Version(4, 6, 2), Version(4, 6, 1))),
                arrayOf(Framework("netstandard1.5"), emptyList<Property>(), listOf(Version(5), Version(3), Version(2), Version(1), Version(4, 8), Version(4, 7), Version(4, 6, 2), Version(4, 6, 1))),
                arrayOf(Framework("netstandard1.4"), emptyList<Property>(), listOf(Version(5), Version(3), Version(2), Version(1), Version(4, 8), Version(4, 7), Version(4, 6, 2), Version(4, 6, 1))),
                arrayOf(Framework("netstandard1.3"), emptyList<Property>(), listOf(Version(5), Version(3), Version(2), Version(1), Version(4, 8), Version(4, 7), Version(4, 6))),
                arrayOf(Framework("netstandard1.2"), emptyList<Property>(), listOf(Version(5), Version(3), Version(2), Version(1), Version(4, 8), Version(4, 7), Version(4, 6), Version(4, 5, 2), Version(4, 5, 1))),
                arrayOf(Framework("netstandard1.1"), emptyList<Property>(), listOf(Version(5), Version(3), Version(2), Version(1), Version(4, 8), Version(4, 7), Version(4, 6), Version(4, 5))),
                arrayOf(Framework("netstandard1.0"), emptyList<Property>(), listOf(Version(5), Version(3), Version(2), Version(1), Version(4, 8), Version(4, 7), Version(4, 6), Version(4, 5))),

                arrayOf(Framework("net48"), emptyList<Property>(), listOf(Version(4, 8))),
                arrayOf(Framework("net472"), emptyList<Property>(), listOf(Version(4, 8), Version(4, 7, 2))),
                arrayOf(Framework("net471"), emptyList<Property>(), listOf(Version(4, 8), Version(4, 7, 2), Version(4, 7, 1))),
                arrayOf(Framework("net47"), emptyList<Property>(), listOf(Version(4, 8), Version(4, 7))),
                arrayOf(Framework("net462"), emptyList<Property>(), listOf(Version(4, 8), Version(4, 7), Version(4, 6, 2))),
                arrayOf(Framework("net461"), emptyList<Property>(), listOf(Version(4, 8), Version(4, 7), Version(4, 6, 2), Version(4, 6, 1))),
                arrayOf(Framework("net46"), emptyList<Property>(), listOf(Version(4, 8), Version(4, 7), Version(4, 6))),
                arrayOf(Framework("net452"), emptyList<Property>(), listOf(Version(4, 8), Version(4, 7), Version(4, 6), Version(4, 5, 2))),
                arrayOf(Framework("net451"), emptyList<Property>(), listOf(Version(4, 8), Version(4, 7), Version(4, 6), Version(4, 5, 2), Version(4, 5, 1))),
                arrayOf(Framework("net45"), emptyList<Property>(), listOf(Version(4, 8), Version(4, 7), Version(4, 6), Version(4, 5))),
                arrayOf(Framework("net403"), emptyList<Property>(), listOf(Version(4, 0, 3))),
                arrayOf(Framework("net40"), emptyList<Property>(), listOf(Version(4, 0))),
                arrayOf(Framework("net35"), emptyList<Property>(), listOf(Version(3, 5))),
                arrayOf(Framework("net20"), emptyList<Property>(), emptyList<Version>()),
                arrayOf(Framework("net11"), emptyList<Property>(), emptyList<Version>())
        )
    }

    @Test(dataProvider = "resolveData")
    fun shouldResolveSdkVersions(framework: Framework, propeties: Collection<Property>, expectedSdkVersions: List<Version>) {
        // Given
        val resolver = SdkResolverImpl()

        // When
        val actualSdkVersions = resolver.resolveSdkVersions(framework, propeties).toList()

        // Then
        Assert.assertEquals(actualSdkVersions, expectedSdkVersions)
    }

    @Test(dataProvider = "resolveData")
    fun shouldResolveTrimmedSdkVersions(framework: Framework, propeties: Collection<Property>, expectedSdkVersions: List<Version>) {
        // Given
        val resolver = SdkResolverImpl()

        // When
        val actualSdkVersions = resolver.resolveSdkVersions(framework, propeties).toList()
        val actualTrimmedSdkVersions = actualSdkVersions.map { it.trim() }.toList()

        // Then
        Assert.assertEquals(actualSdkVersions, actualTrimmedSdkVersions)
    }
}