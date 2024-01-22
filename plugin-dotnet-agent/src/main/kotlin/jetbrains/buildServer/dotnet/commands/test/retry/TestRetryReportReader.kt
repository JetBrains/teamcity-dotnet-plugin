package jetbrains.buildServer.dotnet.commands.test.retry

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.utils.getBufferedReader
import java.io.File

class TestRetryReportReader(
    private val _testRetrySettings: TestRetrySettings,
    private val _fileSystem: FileSystemService
) {
    fun readFailedTestNames(): List<String> = runCatching {
        getFailedTestNamesSequence().distinct().toList()
    }.getOrElse { error ->
        LOG.error("Failed to read failed test reports", error)
        emptyList()
    }

    fun cleanup() {
        try {
            getReportDirectory()?.let {
                if (!_fileSystem.remove(it)) {
                    LOG.warn("Cannot delete test retry report folder")
                }
            }
        } catch (e: Exception) {
            LOG.error("Cannot delete test retry report folder", e)
        }
    }

    private fun getReportDirectory(): File? = File(_testRetrySettings.reportPath)
        .takeIf { _fileSystem.isExists(it) && _fileSystem.isDirectory(it) }

    private fun getReportFiles(): Sequence<File> = getReportDirectory()
        ?.let { _fileSystem.list(it) }
        ?.filter { _fileSystem.isFile(it) } ?: emptySequence()

    private fun getFailedTestNamesSequence(): Sequence<String> = sequence {
        var readLines = 0
        getReportFiles().forEach filesMap@{ file ->
            LOG.debug("Reading test retry report file $file")
            file.getBufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (readLines++ >= _testRetrySettings.maxFailures) {
                        LOG.debug("Reached maximum amount of tests to retry, stopped reading reports")
                        return@filesMap
                    }
                    yield(line)
                }
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(TestRetryReportReader::class.java)
    }
}