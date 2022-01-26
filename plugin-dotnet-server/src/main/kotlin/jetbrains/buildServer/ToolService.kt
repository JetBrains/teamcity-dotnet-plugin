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

package jetbrains.buildServer

import jetbrains.buildServer.tools.GetPackageVersionResult
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersion
import java.io.File

interface ToolService {
    fun getTools(toolType: ToolType, vararg packageIds: String): List<NuGetTool>

    fun tryGetPackageVersion(toolType: ToolType, toolPackage: File, vararg packageIds: String): GetPackageVersionResult?

    fun fetchToolPackage(toolType: ToolType, toolVersion: ToolVersion, targetDirectory: File, vararg packageIds: String): File

    fun unpackToolPackage(toolPackage: File, nugetPackageDirectory: String, targetDirectory: File, vararg packageIds: String)
}