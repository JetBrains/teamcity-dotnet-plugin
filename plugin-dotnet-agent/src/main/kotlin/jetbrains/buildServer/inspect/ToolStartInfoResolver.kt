

package jetbrains.buildServer.inspect

interface ToolStartInfoResolver {
    fun resolve(tool: InspectionTool): ToolStartInfo
}