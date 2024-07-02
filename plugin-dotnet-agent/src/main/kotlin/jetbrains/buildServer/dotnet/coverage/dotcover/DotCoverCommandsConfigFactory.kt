package jetbrains.buildServer.dotnet.coverage.dotcover

import org.jdom2.Document
import java.io.File

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
interface DotCoverCommandsConfigFactory {

    fun createMergeCommandConfig(sources: Collection<File>, output: File): Document

    fun createReportCommandConfig(source: File, output: File): Document

    fun createZipCommandConfig(source: File, output: File): Document

    fun createDeleteCommandConfig(sources: Collection<File>): Document
}
