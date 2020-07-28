package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.Platform
import java.io.File

data class ToolInstance(
        val toolType: ToolInstanceType,
        val installationPath: File,
        // 16.6.3
        val detailedVersion: Version,
        // 2019
        val baseVersion: Version,
        val platform: Platform) {
    override fun toString() = "${toolType.name} $baseVersion($detailedVersion) ${platform.id} at \"$installationPath\""
}