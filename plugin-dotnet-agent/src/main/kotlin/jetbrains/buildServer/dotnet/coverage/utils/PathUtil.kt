package jetbrains.buildServer.dotnet.coverage.utils

import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.util.FileUtil
import java.io.File

object PathUtil {

    fun resolvePath(build: AgentRunningBuild,
                    path: String?): File? {
        return if (path == null) {
            null
        } else FileUtil.resolvePath(build.checkoutDirectory, path)
    }

    fun resolvePathToTool(build: AgentRunningBuild,
                          path: String?,
                          toolName: String): File? {
        if (path == null) {
            return null
        }
        val file = resolvePath(build, path) ?: return null
        if (file.exists()) {
            if (file.isFile) {
                return file
            } else if (file.isDirectory) {
                val newFile = File(file, toolName)
                if (newFile.exists() && newFile.isFile) {
                    return newFile
                }
            }
        }
        return null
    }
}
