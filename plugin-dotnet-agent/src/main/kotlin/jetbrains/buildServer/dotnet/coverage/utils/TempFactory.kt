package jetbrains.buildServer.dotnet.coverage.utils

import java.io.File
import java.io.IOException

interface TempFactory {

    @Throws(IOException::class)
    fun createTempFile(path: File,
                       prefix: String,
                       extension: String,
                       maxAttempts: Int): File

    fun createTempDirectory(path: File,
                            maxAttempts: Int): File
}

