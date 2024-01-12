

package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.Version

interface ArgumentsProvider {
    fun getArguments(tool: InspectionTool, toolVersion: Version): InspectionArguments
}