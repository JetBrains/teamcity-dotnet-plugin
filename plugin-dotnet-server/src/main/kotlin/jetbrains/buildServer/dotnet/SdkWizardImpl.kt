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

import jetbrains.buildServer.dotnet.discovery.*

class SdkWizardImpl(
        private val _sdkResolver: SdkResolver)
    : SdkWizard {
    override fun suggestSdks(projects: Sequence<Project>, compactMode: Boolean): Sequence<SdkVersion> = sequence {
        val sdks = suggestSdks(projects)
        if (!compactMode) {
            yieldAll(sdks)
        } else {
            var maxDotnetSdk: SdkVersion? = null
            for (sdk in sdks) {
                if (sdk.versionType != SdkVersionType.Default) {
                    continue
                }

                if (sdk.sdkType == SdkType.Dotnet || sdk.sdkType == SdkType.DotnetCore) {
                    if (maxDotnetSdk == null || sdk.version > maxDotnetSdk.version) {
                        maxDotnetSdk = sdk;
                    }
                } else {
                  yield(sdk)
                }
            }

                maxDotnetSdk?.let { yield(it)
            }
        }
    }

    private fun suggestSdks(projects: Sequence<Project>) =
            projects
                    .flatMap {
                        it.frameworks.asSequence().map {
                            framework ->
                            SdkData(framework, it.properties)
                        }
                    }
                    .mapNotNull { _sdkResolver.resolveSdkVersions(it.framework, it.properties) }
                    .flatMap { it }
                    .sortedWith(compareBy({it.versionType}, {-(it.sdkType.order)}, {it.version}))
                    .distinctBy { it.version }

    private data class SdkData(val framework: Framework, val properties: Collection<Property>)
}