package jetbrains.buildServer.script

import jetbrains.buildServer.agent.Version
import java.io.File

data class CsiTool(val path: File, val runtimeVersion: Version)
