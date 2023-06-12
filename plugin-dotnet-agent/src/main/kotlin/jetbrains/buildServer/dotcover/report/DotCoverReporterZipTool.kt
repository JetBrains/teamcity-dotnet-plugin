package jetbrains.buildServer.dotcover.report

import java.io.File
import java.io.IOException

interface DotCoverReporterZipTool {

    @Throws(IOException::class)
    fun runZipTask(snapshotHolderFile: File, destFile: File)
}
