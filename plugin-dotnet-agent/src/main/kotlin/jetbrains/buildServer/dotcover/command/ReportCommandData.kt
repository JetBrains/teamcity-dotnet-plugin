package jetbrains.buildServer.dotcover.command

import java.io.File

data class ReportCommandData(
    val sourceFile: File,
    val outputFile: File
)