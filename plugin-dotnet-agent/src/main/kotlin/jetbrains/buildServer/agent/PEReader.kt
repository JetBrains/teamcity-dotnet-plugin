

package jetbrains.buildServer.agent

import java.io.File

interface PEReader {
    fun tryGetVersion(file: File): Version
}