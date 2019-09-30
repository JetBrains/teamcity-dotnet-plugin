package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.DotnetToolResolver
import jetbrains.buildServer.dotnet.ToolPlatform

class DotnetToolResolverStub(
        override val paltform:ToolPlatform,
        override val executable: ToolPath,
        override val isCommandRequired: Boolean) :
        DotnetToolResolver