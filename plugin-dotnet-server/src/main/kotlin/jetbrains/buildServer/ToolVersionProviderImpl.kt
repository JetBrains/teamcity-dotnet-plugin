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

package jetbrains.buildServer

import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.inspect.CltConstants
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.tools.ServerToolManager

class ToolVersionProviderImpl(
        private val _projectManager: ProjectManager,
        private val _toolManager: ServerToolManager) : ToolVersionProvider {
    override fun getVersion(toolPath: String?, toolTypeName: String) =
            toolPath?.let { path ->
                _toolManager.findToolType(toolTypeName)?.let { toolType ->
                    _toolManager.resolveToolVersionReference(toolType, path, _projectManager.rootProject)?.let { tool ->
                        Version.tryParse(tool.version)
                    }
                }
            } ?: Version(0)
}