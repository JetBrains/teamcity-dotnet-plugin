

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.ToolPath

interface ToolResolver {
    val platform: ToolPlatform

    val executable: ToolPath

    val isCommandRequired: Boolean

    val toolStateWorkflowComposer: ToolStateWorkflowComposer
}