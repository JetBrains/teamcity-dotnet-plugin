package jetbrains.buildServer.dotnet.coverage.dotcover

import java.io.File
import java.io.IOException

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
interface DotCoverReporterZipTool {

    @Throws(IOException::class)
    fun runZipTask(snapshotHolderFile: File, destFile: File)
}
