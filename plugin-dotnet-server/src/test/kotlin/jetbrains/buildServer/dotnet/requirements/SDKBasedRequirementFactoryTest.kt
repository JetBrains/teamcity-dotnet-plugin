package jetbrains.buildServer.dotnet.requirements

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.discovery.SdkResolver
import jetbrains.buildServer.dotnet.discovery.SdkVersion
import jetbrains.buildServer.dotnet.discovery.SdkVersionType
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class SDKBasedRequirementFactoryTest {
    @DataProvider
    fun testData()= arrayOf(
        // net5.X
        arrayOf("5.0", sequenceOf(SdkVersion(Version(5, 0), SdkType.Dotnet, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK5.0[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("5.0", sequenceOf(SdkVersion(Version(5, 0), SdkType.Dotnet, SdkVersionType.Default), SdkVersion(Version(6, 0), SdkType.Dotnet, SdkVersionType.Compatible)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK(5.0|6.0)[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("5", sequenceOf(SdkVersion(Version(5), SdkType.Dotnet, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK5\\.[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("5.0.100", sequenceOf(SdkVersion(Version(5, 0, 100), SdkType.Dotnet, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK5.0.100[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("5.1", sequenceOf(SdkVersion(Version(5, 1), SdkType.Dotnet, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK5.1[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("6.0", sequenceOf(SdkVersion(Version(6, 0), SdkType.Dotnet, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK6.0[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("10.21.345", sequenceOf(SdkVersion(Version(10, 21, 345), SdkType.Dotnet, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK10.21.345[\\.\\d]*_Path)", null, RequirementType.EXISTS)),

        //netcoreappX.X
        arrayOf("1.0", sequenceOf(SdkVersion(Version(1, 0), SdkType.DotnetCore, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK1.0[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("1.0", sequenceOf(SdkVersion(Version(1, 0), SdkType.DotnetCore, SdkVersionType.Default), SdkVersion(Version(3, 1), SdkType.DotnetCore, SdkVersionType.Compatible), SdkVersion(Version(5), SdkType.DotnetCore, SdkVersionType.Compatible)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK(1.0|3.1|5\\.)[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("1", sequenceOf(SdkVersion(Version(1), SdkType.DotnetCore, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK1\\.[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("1.2.105", sequenceOf(SdkVersion(Version(1, 2, 105), SdkType.DotnetCore, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK1.2.105[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("1.1", sequenceOf(SdkVersion(Version(1, 1), SdkType.DotnetCore, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK1.1[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("2.0", sequenceOf(SdkVersion(Version(2, 0), SdkType.DotnetCore, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK2.0[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("2.1", sequenceOf(SdkVersion(Version(2, 1), SdkType.DotnetCore, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK2.1[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("2.2", sequenceOf(SdkVersion(Version(2, 2), SdkType.DotnetCore, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK2.2[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("3.0", sequenceOf(SdkVersion(Version(3, 0), SdkType.DotnetCore, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK3.0[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("3.1", sequenceOf(SdkVersion(Version(3, 1), SdkType.DotnetCore, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK3.1[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
        arrayOf("3.1.402", sequenceOf(SdkVersion(Version(3, 1, 402), SdkType.DotnetCore, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK3.1.402[\\.\\d]*_Path)", null, RequirementType.EXISTS)),

        // .NET Framework
        arrayOf("3.5", sequenceOf(SdkVersion(Version(3, 5), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack3.5_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.0", sequenceOf(SdkVersion(Version(4, 0), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.0_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.0.3", sequenceOf(SdkVersion(Version(4, 0, 3), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.0.3_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.5", sequenceOf(SdkVersion(Version(4, 5), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.5_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.5.1", sequenceOf(SdkVersion(Version(4, 5, 1), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.5.1_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.5.2", sequenceOf(SdkVersion(Version(4, 5, 2), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.5.2_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.6", sequenceOf(SdkVersion(Version(4, 6), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.6_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.6.1", sequenceOf(SdkVersion(Version(4, 6, 1), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.6.1_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.6.2", sequenceOf(SdkVersion(Version(4, 6, 2), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.6.2_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.7", sequenceOf(SdkVersion(Version(4, 7), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.7_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.7.1", sequenceOf(SdkVersion(Version(4, 7, 1), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.7.1_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.7.2", sequenceOf(SdkVersion(Version(4, 7, 2), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.7.2_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.8", sequenceOf(SdkVersion(Version(4, 8), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.8_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.8.1", sequenceOf(SdkVersion(Version(4, 8, 1), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.8.1_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.8.2", sequenceOf(SdkVersion(Version(4, 8, 2), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.8.2_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.9", sequenceOf(SdkVersion(Version(4, 9), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.9_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.9.1", sequenceOf(SdkVersion(Version(4, 9, 1), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.9.1_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.9.2", sequenceOf(SdkVersion(Version(4, 9, 2), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.9.2_Path)", null, RequirementType.EXISTS)),
        arrayOf("4", sequenceOf(SdkVersion(Version(4), SdkType.DotnetFramework, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFramework4[\\.\\d]*_x[\\d]{2})", null, RequirementType.EXISTS)),
        arrayOf("4.7", sequenceOf(SdkVersion(Version(4, 7), SdkType.FullDotnetTargetingPack, SdkVersionType.Default), SdkVersion(Version(4, 8), SdkType.FullDotnetTargetingPack, SdkVersionType.Compatible)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.7_Path)", null, RequirementType.EXISTS)),
        arrayOf("4.7", sequenceOf(SdkVersion(Version(4, 7), SdkType.FullDotnetTargetingPack, SdkVersionType.Default), SdkVersion(Version(4, 8), SdkType.FullDotnetTargetingPack, SdkVersionType.Default)), Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.7_Path)", null, RequirementType.EXISTS)),

        arrayOf("4.7.55555", emptySequence<SdkVersion>(), null)
    )

    @Test(dataProvider = "testData")
    fun shouldCreateRequirement(targetFrameworkMoniker: String, compatibleVersions: Sequence<SdkVersion>, expectedRequierement: Requirement?) {
        // arrange
        val sdkResolver = mockk<SdkResolver>()
        every { sdkResolver.getCompatibleVersions(Version.tryParse(targetFrameworkMoniker)!!) } returns compatibleVersions
        val requirementFactory = SDKBasedRequirementFactoryImpl(sdkResolver)

        // act
        val actualRequirement = requirementFactory.tryCreate(targetFrameworkMoniker)

        // assert
        Assert.assertEquals(actualRequirement, expectedRequierement)
    }
}