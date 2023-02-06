/*
 * Copyright 2000-2023 JetBrains s.r.o.
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

package jetbrains.buildServer.inspect.test

import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FAMEWORK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_PATH
import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.inspect.IspectionToolPlatform
import jetbrains.buildServer.inspect.RequirementsResolverImpl
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementQualifier.EXISTS_QUALIFIER
import jetbrains.buildServer.requirements.RequirementType
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class RequirementsResolverTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        Version(2018, 1),
                        IspectionToolPlatform.WindowsX64,
                        listOf(Requirement(EXISTS_QUALIFIER + "${CONFIG_PREFIX_DOTNET_FAMEWORK}[\\d\\.]+_x64${CONFIG_SUFFIX_PATH}", null, RequirementType.EXISTS))
                ),
                arrayOf(
                        Version(2018, 1),
                        IspectionToolPlatform.WindowsX86,
                        listOf(Requirement(EXISTS_QUALIFIER + "${CONFIG_PREFIX_DOTNET_FAMEWORK}[\\d\\.]+_x86${CONFIG_SUFFIX_PATH}", null, RequirementType.EXISTS))
                ),
                arrayOf(
                        Version(2018, 2),
                        IspectionToolPlatform.WindowsX64,
                        listOf(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(${CONFIG_PREFIX_DOTNET_FAMEWORK}4\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_x64${CONFIG_SUFFIX_PATH})", null, RequirementType.EXISTS))
                ),
                arrayOf(
                        Version(2018, 2),
                        IspectionToolPlatform.WindowsX86,
                        listOf(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(${CONFIG_PREFIX_DOTNET_FAMEWORK}4\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_x86${CONFIG_SUFFIX_PATH})", null, RequirementType.EXISTS))
                ),
                arrayOf(
                        Version(2018, 2),
                        IspectionToolPlatform.WindowsX86,
                        listOf(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(${CONFIG_PREFIX_DOTNET_FAMEWORK}4\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_x86${CONFIG_SUFFIX_PATH})", null, RequirementType.EXISTS))
                )
        )
    }

    @Test(dataProvider = "testData")
    fun shouldResolve(version: Version, platform: IspectionToolPlatform, expectedRequierements: Collection<Requirement>) {
        // Given
        val resolver = RequirementsResolverImpl()

        // When
        val actualRequirements = resolver.resolve(version, platform).toList()

        // Then
        Assert.assertEquals(actualRequirements, expectedRequierements)
    }
}