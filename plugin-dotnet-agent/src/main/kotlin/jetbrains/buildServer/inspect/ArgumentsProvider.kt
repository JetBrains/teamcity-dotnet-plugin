package jetbrains.buildServer.inspect

interface ArgumentsProvider {
    fun getArguments(tool: InspectionTool): InspectionArguments
}