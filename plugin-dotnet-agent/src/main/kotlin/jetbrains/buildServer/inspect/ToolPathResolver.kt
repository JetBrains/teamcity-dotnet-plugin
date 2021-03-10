package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.Path

interface ToolPathResolver {
    fun resolve(tool: InspectionTool): Path
}