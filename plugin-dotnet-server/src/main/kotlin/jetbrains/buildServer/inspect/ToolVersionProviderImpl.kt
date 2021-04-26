package jetbrains.buildServer.inspect

import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.tools.ServerToolManager

class ToolVersionProviderImpl(
        private val _projectManager: ProjectManager,
        private val _toolManager: ServerToolManager) : ToolVersionProvider {
    override fun getVersion(parameters: Map<String, String>) =
            parameters[CltConstants.CLT_PATH_PARAMETER]?.let { path ->
                _toolManager.findToolType(CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID)?.let { toolType ->
                    _toolManager.resolveToolVersionReference(toolType, path, _projectManager.rootProject)?.let { tool ->
                        Version.tryParse(tool.version)
                    }
                }
            } ?: Version(0)
}