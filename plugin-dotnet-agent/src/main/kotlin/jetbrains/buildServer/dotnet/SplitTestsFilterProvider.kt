package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class SplitTestsFilterProvider(
    private val _parametersService: ParametersService,
    private val _fileSystem: FileSystemService)
    : TestsFilterProvider, SplittedTestsFilterSettings {
    override val IsActive: Boolean
        get() = ExcludesFile != null && IncludesFile != null

    private val ExcludesFile: File?
        get() = _parametersService.tryGetParameter(ParameterType.System, ExcludesFileParam)?.let { File(it) }
    private val IncludesFile: File?
        get() = _parametersService.tryGetParameter(ParameterType.System, IncludesFileParam)?.let { File(it) }
    private val FilterType: SplittedTestsFilterType
        get() = if (_parametersService.tryGetParameter(ParameterType.Configuration, CurrentBatch) == "1") {
            SplittedTestsFilterType.Excludes
        } else {
            SplittedTestsFilterType.Includes
        }
    private val CurrentFile: File?
        get() = if (FilterType == SplittedTestsFilterType.Excludes) {
            ExcludesFile
        } else {
            IncludesFile
        }

    override val filterExpression: String
        get() =
            CurrentFile?.let { testsPartsFile ->
                LOG.debug("Tests group file is \"$testsPartsFile\".")
                if (!_fileSystem.isExists(testsPartsFile) || !_fileSystem.isFile(testsPartsFile)) {
                    LOG.warn("Cannot find file \"$testsPartsFile\".")
                    return@let null
                }

                var filter = testsPartsFile
                    .readLinesFromFile()
                    .filterOutRedundantLines()
                    .buildFilter(FilterType)

                LOG.debug("Tests group file filter: \"$filter\".")
                filter
            } ?: ""

    private fun File.readLinesFromFile(): List<String> =
        _fileSystem
            .read(this) { input ->
                BufferedReader(InputStreamReader(input)).use { reader ->
                    val tests: MutableList<String> = ArrayList()
                    while (reader.ready()) {
                        tests += reader.readLine()
                    }
                    tests
                }
            }

    companion object {
        internal const val ExcludesFileParam = "teamcity.build.parallelTests.excludesFile"
        internal const val IncludesFileParam = "teamcity.build.parallelTests.includesFile"
        internal const val CurrentBatch = "teamcity.build.parallelTests.currentBatch"

        private val LOG = Logger.getLogger(SplitTestsFilterProvider::class.java)

        private const val FilterExressionChunkSize = 1000;

        private fun List<String>.filterOutRedundantLines() = this
            .filter { !it.startsWith("#") }
            .map { it.trim() }
            .filter { it.length > 2 }

        private fun List<String>.buildFilter(filterType: SplittedTestsFilterType): String {
            // https://docs.microsoft.com/en-us/dotnet/core/testing/selective-unit-tests
            val filterProperty = "FullyQualifiedName"
            val (filterOperation, filterCombineOperator) = when (filterType == SplittedTestsFilterType.Includes) {
                true -> Pair("~", " | ")
                false -> Pair("!~", " & ")
            }

            return this
                .map { filterValue -> "${filterProperty}${filterOperation}${filterValue}." }
                .let { testsList ->
                    if (testsList.size > FilterExressionChunkSize)
                        // chunks in parentheses '(', ')' are necessery to avoid stack overflow in VSTest filter validator
                        // https://youtrack.jetbrains.com/issue/TW-76381
                        testsList.chunked(FilterExressionChunkSize) { "(${it.joinToString(filterCombineOperator)})" }
                    else
                        testsList
                }
                .joinToString(filterCombineOperator)
        }
    }
}