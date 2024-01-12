

package jetbrains.buildServer.dotnet.test.dotnet.toolResolvers

import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver
import jetbrains.buildServer.dotnet.ToolPlatform
import jetbrains.buildServer.dotnet.ToolStateWorkflowComposer

class ToolResolverStub(
        override val platform:ToolPlatform,
        override val executable: ToolPath,
        override val isCommandRequired: Boolean,
        override val toolStateWorkflowComposer: ToolStateWorkflowComposer) :
    DotnetToolResolver