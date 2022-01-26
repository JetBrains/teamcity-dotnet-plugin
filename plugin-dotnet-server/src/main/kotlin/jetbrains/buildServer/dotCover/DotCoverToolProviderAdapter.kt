/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package jetbrains.buildServer.dotCover

import jetbrains.buildServer.ToolService
import jetbrains.buildServer.dotnet.DotnetConstants.DOTCOVER_PACKAGE_TYPE
import jetbrains.buildServer.dotnet.DotnetConstants.DOTCOVER_WIN_PACKAGE_TYPE
import jetbrains.buildServer.tools.ServerToolProviderAdapter
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersion
import java.io.File

class DotCoverToolProviderAdapter(
        private val _toolService: ToolService,
        private val _toolType: ToolType)
    : ServerToolProviderAdapter() {

    override fun getType() = _toolType

    override fun getAvailableToolVersions(): MutableCollection<out ToolVersion> =
            _toolService.getTools(type, DOTCOVER_PACKAGE_TYPE, DOTCOVER_WIN_PACKAGE_TYPE).toMutableList()

    override fun tryGetPackageVersion(toolPackage: File) =
            _toolService.tryGetPackageVersion(type, toolPackage, DOTCOVER_PACKAGE_TYPE, DOTCOVER_WIN_PACKAGE_TYPE) ?: super.tryGetPackageVersion(toolPackage)

    override fun fetchToolPackage(toolVersion: ToolVersion, targetDirectory: File) =
            _toolService.fetchToolPackage(type, toolVersion, targetDirectory, DOTCOVER_PACKAGE_TYPE, DOTCOVER_WIN_PACKAGE_TYPE)

    override fun unpackToolPackage(toolPackage: File, targetDirectory: File) =
            _toolService.unpackToolPackage(toolPackage, "tools/", targetDirectory, DOTCOVER_PACKAGE_TYPE, DOTCOVER_WIN_PACKAGE_TYPE)

    override fun getDefaultBundledVersionId(): String? = null
}