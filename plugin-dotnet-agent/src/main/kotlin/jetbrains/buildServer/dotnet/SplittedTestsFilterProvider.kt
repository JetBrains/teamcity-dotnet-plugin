package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class SplittedTestsFilterProvider(
        private val _parametersService: ParametersService,
        private val _fileSystem: FileSystemService)
    : TestsFilterProvider, SplittedTestsFilterSettings {
    override val IsActive: Boolean
        get() = ExcludesFile != null && IncludesFile != null

    private val ExcludesFile: File?
        get() = _parametersService.tryGetParameter(ParameterType.System, ExcludesFileParam)?.let { File(it) }
    private val IncludesFile: File?
        get() = _parametersService.tryGetParameter(ParameterType.System, IncludesFileParam)?.let { File(it) }
    private val Mode: SplittedTestsFilterType
        get() = if (_parametersService.tryGetParameter(ParameterType.Configuration, CurrentBatch) == "1") {
            SplittedTestsFilterType.Excludes
        } else {
            SplittedTestsFilterType.Includes
        }
    private val CurrentFile: File?
        get() = if (Mode == SplittedTestsFilterType.Excludes) {
            ExcludesFile
        } else {
            IncludesFile
        }

    override val filterExpression: String
        get() =
            CurrentFile?.let {
                testsPartsFile ->
                LOG.debug("Tests group file is \"$testsPartsFile\".")
                if (!_fileSystem.isExists(testsPartsFile) || !_fileSystem.isFile(testsPartsFile)) {
                    LOG.warn("Cannot find file \"$testsPartsFile\".")
                    return@let null
                }

                var filter = _fileSystem.read(testsPartsFile) {
                    input ->
                    BufferedReader(InputStreamReader(input)).use {
                        reader ->
                        val tests: MutableList<String> = ArrayList()
                        while (reader.ready()) {
                            val line = reader.readLine()
                            if (!line.startsWith("#")) {
                                tests += line
                            }
                        }
                        return@read if (Mode == SplittedTestsFilterType.Includes) {
                            createClassFiltersFromLines(true, tests.asSequence()).joinToString(" | ")
                        } else {
                            createClassFiltersFromLines(false, tests.asSequence()).joinToString(" & ")
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

                        "FullyQualifiedName${operation}$it."
                    }
                    .filterNotNull()

    companion object {
        internal const val ExcludesFileParam = "teamcity.build.parallelTests.excludesFile"
        internal const val IncludesFileParam = "teamcity.build.parallelTests.includesFile"
        internal const val CurrentBatch = "teamcity.build.parallelTests.currentBatch"
        internal const val ExcludeAllFilter = "FullyQualifiedName=04B12786DAFE"
        private val LOG = Logger.getLogger(SplittedTestsFilterProvider::class.java)
    }
}