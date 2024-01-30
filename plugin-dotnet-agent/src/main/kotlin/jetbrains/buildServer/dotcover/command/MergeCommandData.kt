package jetbrains.buildServer.dotcover.command

import java.io.File

data class MergeCommandData(
    val sourceFiles: List<File>,
    val outputFile: File
)
