package jetbrains.buildServer.dotnet.test

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.dotnet.SdkType
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
                        false,
                        sequenceOf(
                                Project("net.csproj", frameworks = listOf(Framework("net5.0")))
                        ),
                        "*net5.0 net5.1 net5.1.3"),
                arrayOf(
                        true,
                        sequenceOf(
                                Project("net.csproj", frameworks = listOf(Framework("net5.0")))
                        ),
                        "*net5.0"),
                arrayOf(
                        false,
                        sequenceOf(
                                Project("netcore.csproj", frameworks = listOf(Framework("netcoreapp3.1"))),
                                Project("net.csproj", frameworks = listOf(Framework("net5.0")))
                        ),
                        "*core3.1 *net5.0 net5.1 net5.1.3"),
                arrayOf(
                        true,
                        sequenceOf(
                                Project("netcore.csproj", frameworks = listOf(Framework("netcoreapp3.1"))),
                                Project("net.csproj", frameworks = listOf(Framework("net5.0")))
                        ),
                        "*net5.0"),
                arrayOf(
                        false,
                        sequenceOf(
                                Project("netstandard.csproj", frameworks = listOf(Framework("netstandard2.0"))),
                                Project("netcore.csproj", frameworks = listOf(Framework("netcoreapp3.1"), Framework("netcoreapp2.0"))),
                                Project("net.csproj", frameworks = listOf(Framework("net6.0")))
                        ),
                        "*core2.0 *core3.1 *net6.0 pack4.7.2 net5.0"),
                arrayOf(
                        true,
                        sequenceOf(
                                Project("netstandard.csproj", frameworks = listOf(Framework("netstandard2.0"))),
                                Project("netcore.csproj", frameworks = listOf(Framework("netcoreapp3.1"), Framework("netcoreapp2.0"))),
                                Project("net.csproj", frameworks = listOf(Framework("net6.0")))
                        ),
                        "*net6.0"),
                arrayOf(
                        false,
                        sequenceOf(
                                Project("net.csproj", frameworks = listOf(Framework("netcoreapp3.1"), Framework("net5.0")))
                        ),
                        "*core3.1 *net5.0 net5.1 net5.1.3"),
                arrayOf(
                        true,
                        sequenceOf(
                                Project("net.csproj", frameworks = listOf(Framework("netcoreapp3.1"), Framework("net5.0")))
                        ),
                        "*net5.0")
        )
    }

    @Test(dataProvider = "resolveData")
    fun shouldResolveSdkVersions(compactMode: Boolean, projects: Sequence<Project>, expectedSdkVersions: String) {
        // Given
        val sdkResolver = mockk<SdkResolver>()
        every { sdkResolver.resolveSdkVersions(Framework("net6.0"), any()) } returns sequenceOf(SdkVersion(Version(6, 0), SdkType.Dotnet, SdkVersionType.Default))
        every { sdkResolver.resolveSdkVersions(Framework("net5.0"), any()) } returns sequenceOf(SdkVersion(Version(5, 0), SdkType.Dotnet, SdkVersionType.Default), SdkVersion(Version(5, 0), SdkType.Dotnet, SdkVersionType.Compatible), SdkVersion(Version(5, 1), SdkType.Dotnet, SdkVersionType.Compatible), SdkVersion(Version(5, 1, 3), SdkType.Dotnet, SdkVersionType.Compatible))
        every { sdkResolver.resolveSdkVersions(Framework("netcoreapp3.1"), any()) } returns sequenceOf(SdkVersion(Version(5, 0), SdkType.Dotnet, SdkVersionType.Compatible), SdkVersion(Version(3, 1), SdkType.DotnetCore, SdkVersionType.Default))
        every { sdkResolver.resolveSdkVersions(Framework("netcoreapp2.0"), any()) } returns sequenceOf(SdkVersion(Version(5, 0), SdkType.Dotnet, SdkVersionType.Compatible), SdkVersion(Version(3, 1), SdkType.DotnetCore, SdkVersionType.Compatible), SdkVersion(Version(2, 0), SdkType.DotnetCore, SdkVersionType.Default))
        every { sdkResolver.resolveSdkVersions(Framework("netstandard2.0"), any()) } returns sequenceOf(SdkVersion(Version(4, 7, 2), SdkType.FullDotnetTargetingPack, SdkVersionType.Compatible), SdkVersion(Version(5, 0), SdkType.Dotnet, SdkVersionType.Compatible), SdkVersion(Version(3, 1), SdkType.DotnetCore, SdkVersionType.Default))

        val wizard = SdkWizardImpl(sdkResolver)

        // When
        val actualSdkVersions = wizard.suggestSdks(projects, compactMode).map { it.toString() }.joinToString(" ")

        // Then
        Assert.assertEquals(actualSdkVersions, expectedSdkVersions)
    }
}