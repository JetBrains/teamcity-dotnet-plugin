package jetbrains.buildServer.dotnet.coverage.dotcover

import java.io.File
import java.io.IOException

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
interface DotCoverReporterTool {

    @Throws(IOException::class)
    fun runDeleteTask(files: Collection<File>)

    @Throws(IOException::class)
    fun runReportTask(reportFile: File): File

    @Throws(IOException::class)
    fun runMergeTask(reportFiles: Collection<File>): File
}
