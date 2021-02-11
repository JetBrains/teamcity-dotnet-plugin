package jetbrains.buildServer.dotnet.test

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.dotnet.SdkType
import jetbrains.buildServer.dotnet.SdkTypeResolver
import jetbrains.buildServer.dotnet.SdkWizardImpl
import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.dotnet.discovery.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class SdkWizardTest {
    @DataProvider
    fun resolveData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        sequenceOf(
                                Project("net.csproj", frameworks = listOf(Framework("net5.0")))
                        ),
                        "5.0"),
                arrayOf(
                        sequenceOf(
                                Project("netcore.csproj", frameworks = listOf(Framework("netcoreapp3.1"))),
                                Project("net.csproj", frameworks = listOf(Framework("net5.0")))
                        ),
                        "5.0 3.1"),
                arrayOf(
                        sequenceOf(
                                Project("netstandard.csproj", frameworks = listOf(Framework("netstandard2.0"))),
                                Project("netcore.csproj", frameworks = listOf(Framework("netcoreapp3.1"))),
                                Project("net.csproj", frameworks = listOf(Framework("net5.0")))
                        ),
                        "5.0 3.1 4.7.2"),
                arrayOf(
                        sequenceOf(
                                Project("net.csproj", frameworks = listOf(Framework("netcoreapp3.1"), Framework("net5.0")))
                        ),
                        "5.0 3.1")
        )
    }

    @Test(dataProvider = "resolveData")
    fun shouldResolveSdkVersions(projects: Sequence<Project>, expectedSdkVersions: String) {
        // Given
        val sdkResolver = mockk<SdkResolver>()
        every { sdkResolver.resolveSdkVersions(Framework("net5.0"), any()) } returns sequenceOf(Version(5, 0))
        every { sdkResolver.resolveSdkVersions(Framework("netcoreapp3.1"), any()) } returns sequenceOf(Version(5, 0), Version(3, 1))
        every { sdkResolver.resolveSdkVersions(Framework("netstandard2.0"), any()) } returns sequenceOf(Version(5, 0), Version(3, 1), Version(4, 7, 2))

        val sdkTypeResolver = mockk<SdkTypeResolver>()
        every { sdkTypeResolver.tryResolve(Version(5, 0)) } returns SdkType.Dotnet
        every { sdkTypeResolver.tryResolve(Version(3, 1)) } returns SdkType.DotnetCore
        every { sdkTypeResolver.tryResolve(Version(4, 7, 2)) } returns SdkType.FullDotnetTargetingPack

        val wizard = SdkWizardImpl(sdkResolver, sdkTypeResolver)

        // When
        val actualSdkVersions = wizard.suggestSdks(projects).map { it.toString() }.joinToString(" ")

        // Then
        Assert.assertEquals(actualSdkVersions, expectedSdkVersions)
    }
}