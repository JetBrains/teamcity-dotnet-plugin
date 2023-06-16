package jetbrains.buildServer.dotcover.report

import java.io.File
import java.io.IOException

interface DotCoverReporterTool {

    @Throws(IOException::class)
    fun runDeleteTask(files: Collection<File>)

    @Throws(IOException::class)
    fun runReportTask(reportFile: File): File

    @Throws(IOException::class)
    fun runMergeTask(reportFiles: Collection<File>): File
}
