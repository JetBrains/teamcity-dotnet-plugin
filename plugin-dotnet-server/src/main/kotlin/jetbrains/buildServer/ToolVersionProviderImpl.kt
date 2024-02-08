

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