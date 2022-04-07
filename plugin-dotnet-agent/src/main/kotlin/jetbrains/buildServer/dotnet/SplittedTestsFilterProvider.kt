package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

class SplittedTestsFilterProvider(
        private val _parametersService: ParametersService,
        private val _fileSystem: FileSystemService)
    : TestsFilterProvider, SplittedTestsFilterSettings {
    override val IsActive: Boolean
        get() = TestsPartsFile != null

    private val TestsPartsFile: File?
        get() = _parametersService.tryGetParameter(ParameterType.System, TestsPartsFileParam)?.let { File(it) }

    override val filterExpression: String
        get() =
            TestsPartsFile?.let {
                testsPartsFile ->
                LOG.debug("Tests group file is \"$testsPartsFile\".")
                if (!_fileSystem.isExists(testsPartsFile) || !_fileSystem.isFile(testsPartsFile)) {
                    LOG.warn("Cannot find file \"$testsPartsFile\".")
                    return@let null
                }

                var filter: String = ""
                _fileSystem.read(testsPartsFile) {
                    input ->
                    BufferedReader(InputStreamReader(GZIPInputStream(input))).use {
                        reader ->
                        val splittedTestsFilterType = SplittedTestsFilterType.tryParse(reader.readLine())
                        LOG.debug("Tests group file type is $splittedTestsFilterType.")
                        when(splittedTestsFilterType) {
                            SplittedTestsFilterType.ExcludeAll ->
                                filter = ExcludeAllFilter
                            SplittedTestsFilterType.Include ->
                                filter = createClassFiltersFromLines(true, reader.readLines().asSequence()).joinToString(" | ")
                            SplittedTestsFilterType.Exclude ->
                                filter = createClassFiltersFromLines(false, reader.readLines().asSequence()).joinToString(" & ")
                            else ->
                                filter = ""
                        }
                    }
                }

                LOG.debug("Tests group file filter: \"$filter\".")
                filter
            } ?: ""

    private fun createClassFiltersFromLines(include: Boolean, lines: Sequence<String>): Sequence<String> =
            lines
                    .map { it.trim() }
                    .filter { it.length > 2 }
                    .map {
                        var operation = when(include) {
                            true -> "~"
                            false -> "!~"
                        }

                        "FullyQualifiedName${operation}$it"
                    }
                    .filterNotNull()

    companion object {
        internal const val TestsPartsFileParam = "teamcity.build.parallelTests.testsBatch.artifactPath"
        internal const val ExcludeAllFilter = "FullyQualifiedName=04B12786DAFE"
        private val LOG = Logger.getLogger(SplittedTestsFilterProvider::class.java)
    }
}