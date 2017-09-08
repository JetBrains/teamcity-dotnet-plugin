package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.dotnet.LoggerResolver
import jetbrains.buildServer.dotnet.ToolType
import java.io.File

class LoggerResolverStub(
        private val _msbuildLogger: File?,
        private val _vstestLogger: File?):
        LoggerResolver {
    override fun resolve(toolType: ToolType): File? {
        when(toolType) {
            ToolType.MSBuild -> {
                return _msbuildLogger;
            }
            ToolType.VSTest -> {
                return _vstestLogger
            }
            else -> {
                throw RunBuildException("Unknown tool ${toolType}")
            }
        }
    }
}