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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_CORE_SDK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FAMEWORK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FRAMEWORK_TARGETING_PACK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_PATH
import jetbrains.buildServer.dotnet.discovery.Framework
import jetbrains.buildServer.dotnet.discovery.SdkResolver
import jetbrains.buildServer.dotnet.discovery.SdkVersionType
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier.EXISTS_QUALIFIER
import jetbrains.buildServer.requirements.RequirementType

class RequirementFactoryImpl(
        private val _sdkResolver: SdkResolver)
    : RequirementFactory {
    override fun tryCreate(sdkVersion: String) =
        Version.tryParse(sdkVersion)?.let {
            version ->
            val versions = _sdkResolver.getCompatibleVersions(version).toList()
            val dotNetVersions = versions.filter { it.sdkType == SdkType.DotnetCore || it.sdkType == SdkType.Dotnet }.toList()
            if (dotNetVersions.any()) {
                createRequirement(
                    when (dotNetVersions.size) {
                        1 -> "$CONFIG_PREFIX_CORE_SDK${versionToRegex(dotNetVersions[0].version)}$AnyVersion$CONFIG_SUFFIX_PATH"
                        else -> {
                            val versionsStr = dotNetVersions.map { versionToRegex(it.version) }.joinToString("|")
                            "$CONFIG_PREFIX_CORE_SDK($versionsStr)$AnyVersion$CONFIG_SUFFIX_PATH"
                        }
                    }
                )
            }
            else {
                versions
                        .firstOrNull { it.versionType == SdkVersionType.Default && (it.sdkType == SdkType.FullDotnetTargetingPack || it.sdkType == SdkType.DotnetFramework) }
                        ?.let {
                            createRequirement(
                                when (it.sdkType) {
                                    SdkType.FullDotnetTargetingPack -> "$CONFIG_PREFIX_DOTNET_FRAMEWORK_TARGETING_PACK${it.version}$CONFIG_SUFFIX_PATH"
                                    else ->  "$CONFIG_PREFIX_DOTNET_FAMEWORK${it.version}[\\.\\d]*_x[\\d]{2}"
                                }
                            )
                        }
            }
        }

    companion object {
        private const val AnyVersion = "[\\.\\d]*"

        private fun versionToRegex(version: Version): String
        {
            if (version.versions.size < 2) {
               return "$version\\."
            }

            return version.toString()
        }

        private fun createRequirement(regex: String) =
                Requirement("$EXISTS_QUALIFIER($regex)", null, RequirementType.EXISTS)
    }
}