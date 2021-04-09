package jetbrains.buildServer.agent

import jetbrains.buildServer.util.PEReader.PEVersion
import java.io.File

interface PEReader {
    fun tryGetVersion(file: File): Version
}