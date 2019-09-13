package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.DotnetToolResolver
import jetbrains.buildServer.dotnet.ToolPlatform
import java.io.File

class DotnetToolResolverStub(
        override val paltform:ToolPlatform,
        override val executableFile: File,
        override val isCommandRequired: Boolean) :
        DotnetToolResolver