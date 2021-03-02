package jetbrains.buildServer.dotnet.test

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.dotnet.SdkType
import jetbrains.buildServer.dotnet.SdkTypeResolver
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
                arrayOf(Framework("net6.1"), emptyList<Property>(), "*net6.1"),
                arrayOf(Framework("net6.0"), emptyList<Property>(), "*net6"),
                arrayOf(Framework("net5.1"), emptyList<Property>(), "net6 *net5.1"),
                arrayOf(Framework("net5.0"), emptyList<Property>(), "net6 *net5"),

                arrayOf(Framework("netcoreapp3.1"), emptyList<Property>(), "net6 net5 *core3.1"),
                arrayOf(Framework("netcoreapp3.0"), emptyList<Property>(), "net6 net5 *core3"),
                arrayOf(Framework("netcoreapp2.2"), emptyList<Property>(), "net6 net5 core3 *core2.2"),
                arrayOf(Framework("netcoreapp2.1"), emptyList<Property>(), "net6 net5 core3 core2.2 *core2.1"),
                arrayOf(Framework("netcoreapp2.0"), emptyList<Property>(), "net6 net5 core3 *core2"),
                arrayOf(Framework("netcoreapp1.1"), emptyList<Property>(), "net6 net5 core3 core2 *core1.1"),
                arrayOf(Framework("netcoreapp1.0"), emptyList<Property>(), "net6 net5 core3 core2 *core1"),

                arrayOf(Framework("netstandard2.1"), emptyList<Property>(), "net6 net5 *core3"),
                arrayOf(Framework("netstandard2.0"), emptyList<Property>(), "pack4.8 pack4.7 pack4.6.2 pack4.6.1 net6 net5 core3 *core2"),
                arrayOf(Framework("netstandard1.6"), emptyList<Property>(), "pack4.8 pack4.7 pack4.6.2 pack4.6.1 net6 net5 core3 core2 *core1"),
                arrayOf(Framework("netstandard1.5"), emptyList<Property>(), "pack4.8 pack4.7 pack4.6.2 pack4.6.1 net6 net5 core3 core2 *core1"),
                arrayOf(Framework("netstandard1.4"), emptyList<Property>(), "pack4.8 pack4.7 pack4.6.2 pack4.6.1 net6 net5 core3 core2 *core1"),
                arrayOf(Framework("netstandard1.3"), emptyList<Property>(), "pack4.8 pack4.7 pack4.6 net6 net5 core3 core2 *core1"),
                arrayOf(Framework("netstandard1.2"), emptyList<Property>(), "pack4.8 pack4.7 pack4.6 pack4.5.2 pack4.5.1 net6 net5 core3 core2 *core1"),
                arrayOf(Framework("netstandard1.1"), emptyList<Property>(), "pack4.8 pack4.7 pack4.6 pack4.5 net6 net5 core3 core2 *core1"),
                arrayOf(Framework("netstandard1.0"), emptyList<Property>(), "pack4.8 pack4.7 pack4.6 pack4.5 net6 net5 core3 core2 *core1"),

                arrayOf(Framework("net48"), emptyList<Property>(), "*pack4.8"),
                arrayOf(Framework("net472"), emptyList<Property>(), "*pack4.7.2"),
                arrayOf(Framework("net471"), emptyList<Property>(), "*pack4.7.1"),
                arrayOf(Framework("net47"), emptyList<Property>(), "*pack4.7"),
                arrayOf(Framework("net462"), emptyList<Property>(), "*pack4.6.2"),
                arrayOf(Framework("net461"), emptyList<Property>(), "*pack4.6.1"),
                arrayOf(Framework("net46"), emptyList<Property>(), "*pack4.6"),
                arrayOf(Framework("net452"), emptyList<Property>(), "*pack4.5.2"),
                arrayOf(Framework("net451"), emptyList<Property>(), "*pack4.5.1"),
                arrayOf(Framework("net45"), emptyList<Property>(), "*pack4.5"),
                arrayOf(Framework("net403"), emptyList<Property>(), "*pack4.0.3"),
                arrayOf(Framework("net40"), emptyList<Property>(), "*pack4.0"),
                arrayOf(Framework("net35"), emptyList<Property>(), "*pack3.5"),
                arrayOf(Framework("net20"), emptyList<Property>(), ""),
                arrayOf(Framework("net11"), emptyList<Property>(), "")
        )
    }

    @Test(dataProvider = "resolveData")
    fun shouldResolveSdkVersions(framework: Framework, propeties: Collection<Property>, expectedSdkVersions: String) {
        // Given
        val sdkTypeResolver = mockk<SdkTypeResolver>()
        val resolver = SdkResolverImpl(sdkTypeResolver)

        // When
        val actualSdkVersions = resolver.resolveSdkVersions(framework, propeties).map { it.toString() }.joinToString(" ")

        // Then
        Assert.assertEquals(actualSdkVersions, expectedSdkVersions)
    }
}