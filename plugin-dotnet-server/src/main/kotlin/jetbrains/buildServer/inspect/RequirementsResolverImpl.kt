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

package jetbrains.buildServer.inspect

import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_CORE_RUNTIME
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FAMEWORK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_PATH
import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType

class RequirementsResolverImpl : RequirementsResolver {
    override fun resolve(version: Version, platform: InspectionToolPlatform) = sequence {
        when {
            version >= CrossPlatformVersion && platform == InspectionToolPlatform.CrossPlatform -> yield(DotnetCore3)
            version >= RequiresNet461Version && platform == InspectionToolPlatform.WindowsX86 -> yield(FullDotnet461X86)
            version >= RequiresNet461Version && platform == InspectionToolPlatform.WindowsX64 -> yield(FullDotnet461X64)
            platform == InspectionToolPlatform.WindowsX86 -> yield(MinimalRequirementX86)
            platform == InspectionToolPlatform.WindowsX64 -> yield(MinimalRequirementX64)
            else -> emptySequence<Requirement>()
        }
    }

    companion object {
        private val MinimalRequirementX64 = Requirement(RequirementQualifier.EXISTS_QUALIFIER + "${CONFIG_PREFIX_DOTNET_FAMEWORK}[\\d\\.]+_x64${CONFIG_SUFFIX_PATH}", null, RequirementType.EXISTS)
        private val MinimalRequirementX86 = Requirement(RequirementQualifier.EXISTS_QUALIFIER + "${CONFIG_PREFIX_DOTNET_FAMEWORK}[\\d\\.]+_x86${CONFIG_SUFFIX_PATH}", null, RequirementType.EXISTS)
        private val FullDotnet461X64 = Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(${CONFIG_PREFIX_DOTNET_FAMEWORK}4\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_x64${CONFIG_SUFFIX_PATH})", null, RequirementType.EXISTS)
        private val FullDotnet461X86 = Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(${CONFIG_PREFIX_DOTNET_FAMEWORK}4\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_x86${CONFIG_SUFFIX_PATH})", null, RequirementType.EXISTS)
        private val DotnetCore3 = Requirement(RequirementQualifier.EXISTS_QUALIFIER + "${CONFIG_PREFIX_CORE_RUNTIME}3\\.[\\d\\.]+${CONFIG_SUFFIX_PATH}", null, RequirementType.EXISTS)

        private val RequiresNet461Version = Version(2018, 2)
        internal val CrossPlatformVersion = Version(2020, 2, 1)
        internal val LastVersionWithDupFinder = Version(2021, 2, 3)
    }
}
