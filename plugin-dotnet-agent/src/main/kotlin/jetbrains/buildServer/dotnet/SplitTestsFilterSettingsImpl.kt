package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathsService
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class SplitTestsFilterSettingsImpl(
    private val _parametersService: ParametersService,
    private val _fileSystem: FileSystemService,
    private val _pathsService: PathsService,
) : SplitTestsFilterSettings {
    override val isActive: Boolean get() = testsClassesFile != null

    override val filterType: SplittedTestsFilterType get() =
        when (_parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_PARALLEL_TESTS_CURRENT_BATCH)) {
            "1" -> SplittedTestsFilterType.Excludes
            else -> SplittedTestsFilterType.Includes
        }

    override val testsClassesFile: File? get() =
        filterType
            .let {
                when (it) {
                    SplittedTestsFilterType.Excludes -> DotnetConstants.PARAM_PARALLEL_TESTS_EXCLUDES_FILE
                    SplittedTestsFilterType.Includes -> DotnetConstants.PARAM_PARALLEL_TESTS_INCLUDES_FILE
                }
            }
            .let { _parametersService.tryGetParameter(ParameterType.System, it) }
            ?.let { File(it) }

    override val testClasses: List<String> get() =
        testsClassesFile
            ?.readLinesFromFile()
            ?.filter { !it.startsWith("#") }
            ?.map { it.trim() }
            ?.filter { it.length > 2 }
            ?: emptyList()

    override val useExactMatchFilter: Boolean get() =
        _parametersService
            .tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_PARALLEL_TESTS_USE_EXACT_MATCH_FILTER)
            ?.trim()
            ?.let { it.equals("true", true) }
            ?: false

    override val exactMatchFilterSize: Int get() =
        _parametersService
            .tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_PARALLEL_TESTS_EXACT_MATCH_FILTER_SIZE)
            .let { runCatching { if (it != null) it.trim().toInt() else DefaultExactMatchTestsChunkSize } }
            .getOrDefault(DefaultExactMatchTestsChunkSize)

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
        private const val DefaultExactMatchTestsChunkSize = 5000
    }
}