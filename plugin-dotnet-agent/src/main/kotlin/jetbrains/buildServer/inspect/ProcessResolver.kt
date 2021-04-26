package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.Path

interface ProcessResolver {
    fun resolve(tool: InspectionTool): InspectionProcess
}