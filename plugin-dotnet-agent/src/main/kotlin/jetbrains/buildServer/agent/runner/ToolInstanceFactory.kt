package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.Platform
import java.io.File

interface ToolInstanceFactory {
    fun tryCreate(path: File, baseVersion: Version, platform: Platform): ToolInstance?
}