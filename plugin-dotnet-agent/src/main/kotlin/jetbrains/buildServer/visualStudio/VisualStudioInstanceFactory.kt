package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.Version
import java.io.File

interface VisualStudioInstanceFactory {
    fun tryCreate(path: File, version: Version): VisualStudioInstance?
}