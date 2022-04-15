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
                        var expected: SplittedTestsFilterType = SplittedTestsFilterType.IncludeAll
                        var current: SplittedTestsFilterType = SplittedTestsFilterType.IncludeAll
                        val tests: MutableList<String> = ArrayList()
                        while (reader.ready()) {
                            val line = reader.readLine()
                            if (line.startsWith("#")) {
                                if (line.startsWith("#batch_num=")) {
                                    if (line.substring("#batch_num=".length) == "1") {
                                        expected = SplittedTestsFilterType.Excludes
                                    } else {
                                        expected = SplittedTestsFilterType.Includes
                                    }
                                } else if (line.startsWith("#filtering_mode=")) {
                                    current = SplittedTestsFilterType.of(line.substring("#filtering_mode=".length))
                                }
                            } else {
                                if (current == expected) {
                                    tests += line
                                }
                            }
                        }

                        LOG.debug("Tests group file type is $expected.")
                        when(expected) {
                            SplittedTestsFilterType.ExcludeAll ->
                                filter = ExcludeAllFilter
                            SplittedTestsFilterType.Includes ->
                                filter = createClassFiltersFromLines(true, tests.asSequence()).joinToString(" | ")
                            SplittedTestsFilterType.Excludes ->
                                filter = createClassFiltersFromLines(false, tests.asSequence()).joinToString(" & ")
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
                    .filter{ !it.startsWith("#") }
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
        internal const val TestsPartsFileParam = "teamcity.build.parallelTests.file"
        internal const val ExcludeAllFilter = "FullyQualifiedName=04B12786DAFE"
        private val LOG = Logger.getLogger(SplittedTestsFilterProvider::class.java)
    }
}