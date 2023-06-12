package jetbrains.buildServer.dotnet.coverage

import java.io.File

open class DotnetCoverageGenerationResult(
    val mergedResultFile: File?,
    val multipleResults: Collection<File>,
    val htmlReport: File?) {

    private var myPublishReportFiles = true
    private val myFilesToPublish: MutableMap<String, File> = HashMap()

    fun publishReportFiles(): Boolean {
        return myPublishReportFiles
    }

    fun setPublishReportFiles(publishReportFiles: Boolean) {
        myPublishReportFiles = publishReportFiles
    }

    /**
     * Returns additional artifact files or directories that will be published at end.
     * Every directory is zipped and published as a file with name.
     * @return file name to file map. File names will be relative to .teamcity/.NETCoverage folder
     */
    fun getFilesToPublish(): Map<String, File> {
        return myFilesToPublish
    }

    /**
     * Adds additional file for artifact publising under .teamcity/.NETCoverage folder
     * <br></br>
     * Adds additional directory for artifact publishing as zip archive file.
     * @param name relative path under .teamcity/.NETCoverage folder
     * @param file file to be uploaded
     */
    fun addFileToPublish(name: String, file: File) {
        myFilesToPublish[name] = file
    }
}

