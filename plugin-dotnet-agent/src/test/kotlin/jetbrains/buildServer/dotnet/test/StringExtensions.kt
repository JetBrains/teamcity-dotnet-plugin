package jetbrains.buildServer.dotnet.test

import java.io.File

internal object StringExtensions {
    private const val WINDOWS_SEPARATOR = '\\'
    private const val UNIX_SEPARATOR = '/'
    private const val WINDOWS_DRIVE_C = 'C'

    // Handles string as path and normalizes it in an OS-agnostic way.
    fun String.toPlatformPath(drive: Char = WINDOWS_DRIVE_C) = when {
        File.separatorChar == UNIX_SEPARATOR -> this.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR)
        else -> this.replace(UNIX_SEPARATOR, WINDOWS_SEPARATOR).let { windowsPath ->
            if (this.startsWith(UNIX_SEPARATOR.toString())) "$drive:$windowsPath" else windowsPath
        }
    }
}
