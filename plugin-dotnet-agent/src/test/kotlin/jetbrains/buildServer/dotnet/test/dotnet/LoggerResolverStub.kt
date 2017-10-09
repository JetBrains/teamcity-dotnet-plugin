package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.dotnet.LoggerResolver
import jetbrains.buildServer.dotnet.ToolType
import java.io.File

class LoggerResolverStub(
        private val _msBuildLogger: File,
        private val _vstestLogger: File)
    : LoggerResolver {
    override fun resolve(toolType: ToolType) = when (toolType) {
        ToolType.MSBuild -> {
            _msBuildLogger
        }
        ToolType.VSTest -> {
            _vstestLogger
        }
        else -> {
            throw RunBuildException("Unknown tool $toolType")
        }
    }
}