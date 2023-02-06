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

import jetbrains.buildServer.excluding
import jetbrains.buildServer.including
import jetbrains.buildServer.to

class SdkTypeResolverImpl : SdkTypeResolver {
    override fun tryResolve(sdkVersion: Version): SdkType? =
            when {
                sdkVersion.versions.size == 1 && sdkVersion == Version(4) -> SdkType.DotnetFramework
                sdkVersion `in` DotnetVersions -> SdkType.Dotnet
                sdkVersion `in` DotnetCoreVersions -> SdkType.DotnetCore
                sdkVersion `in` DotnetFullVersions -> SdkType.FullDotnetTargetingPack
                else -> null
            }

    companion object {
        private val DotnetVersions =
                Version(5).including() to Version(Int.MAX_VALUE).including()

        private val DotnetCoreVersions =
                Version(1).including() to Version(3, 5).excluding()

        private val DotnetFullVersions =
                Version(3, 5).including() to Version(5).excluding()
    }
}