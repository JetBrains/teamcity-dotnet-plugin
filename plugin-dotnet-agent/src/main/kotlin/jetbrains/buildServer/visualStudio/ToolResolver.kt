package jetbrains.buildServer.visualStudio

import java.io.File

interface ToolResolver {
    val executableFile: File
}