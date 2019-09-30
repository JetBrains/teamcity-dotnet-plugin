package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.dotnet.DotnetToolResolver
import jetbrains.buildServer.dotnet.ToolPlatform
import java.io.File

class DotnetToolResolverStub(
        override val paltform:ToolPlatform,
        override val executableFile: Path,
        override val isCommandRequired: Boolean) :
        DotnetToolResolver