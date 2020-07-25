package jetbrains.buildServer.agent

import jetbrains.buildServer.util.PEReader.PEVersion
import java.io.File

interface PEReader {
    fun tryGetProductVersion(file: File): Version
}