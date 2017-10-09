package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.DotnetToolResolver
import java.io.File

class DotnetToolResolverStub(
        override val executableFile: File,
        override val isCommandRequired: Boolean):
        DotnetToolResolver