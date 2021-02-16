/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.RequirementFactoryImpl
import jetbrains.buildServer.dotnet.SdkTypeResolverImpl
import jetbrains.buildServer.dotnet.SemanticVersionParser
import jetbrains.buildServer.dotnet.SemanticVersionParserImpl
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class RequirementFactoryTest {
    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                // net5.X
                arrayOf("5.0", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK5.0[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
                arrayOf("5", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK5[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
                arrayOf("5.0.100", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK5.0.100[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
                arrayOf("5.1", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK5.1[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
                arrayOf("6.0", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK6.0[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
                arrayOf("10.21.345", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK10.21.345[\\.\\d]*_Path)", null, RequirementType.EXISTS)),

                //netcoreappX.X
                arrayOf("1.0", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK1.0[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
                arrayOf("1", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK1[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
                arrayOf("1.2.105", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK1.2.105[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
                arrayOf("1.1", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK1.1[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
                arrayOf("2.0", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK2.0[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
                arrayOf("2.1", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK2.1[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
                arrayOf("2.2", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK2.2[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
                arrayOf("3.0", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK3.0[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
                arrayOf("3.1", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK3.1[\\.\\d]*_Path)", null, RequirementType.EXISTS)),
                arrayOf("3.1.402", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetCoreSDK3.1.402[\\.\\d]*_Path)", null, RequirementType.EXISTS)),

                // .NET Framework
                arrayOf("3.5", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack3.5_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.0", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.0_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.0.3", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.0.3_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.5", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.5_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.5.1", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.5.1_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.5.2", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.5.2_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.6", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.6_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.6.1", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.6.1_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.6.2", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.6.2_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.7", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.7_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.7.1", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.7.1_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.7.2", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.7.2_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.8", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.8_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.8.1", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.8.1_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.8.2", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.8.2_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.9", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.9_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.9.1", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.9.1_Path)", null, RequirementType.EXISTS)),
                arrayOf("4.9.2", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFrameworkTargetingPack4.9.2_Path)", null, RequirementType.EXISTS)),
                arrayOf("4", Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFramework4[\\.\\d]*_x[\\d]{2})", null, RequirementType.EXISTS))
        )
    }

    @Test(dataProvider = "testData")
    fun shouldCreateRequirement(targetFrameworkMoniker: String, expectedRequierement: Requirement?) {
        // Given
        val requirementFactory = RequirementFactoryImpl(SdkTypeResolverImpl())

        // When
        val actualRequirement = requirementFactory.tryCreate(targetFrameworkMoniker)

        // Then
        Assert.assertEquals(actualRequirement, expectedRequierement)
    }
}