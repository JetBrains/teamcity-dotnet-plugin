

package jetbrains.buildServer.agent

import java.io.File

interface PEReader {
    /**
     * Reads the PE product version. Tool discovery should use this value first.
     */
    fun tryGetVersion(file: File): Version

    /**
     * Reads the PE file version. Use it as a fallback when the product version is [Version.Empty].
     */
    fun tryGetFileVersion(file: File): Version
}
