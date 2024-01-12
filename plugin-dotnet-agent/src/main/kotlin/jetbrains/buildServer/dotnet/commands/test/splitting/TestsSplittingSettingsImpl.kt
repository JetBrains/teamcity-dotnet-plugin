

package jetbrains.buildServer.dotnet.commands.test.splitting

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.utils.getBufferedReader
import java.io.BufferedReader
import java.io.File

class TestsSplittingSettingsImpl(
    private val _parametersService: ParametersService,
    private val _fileSystem: FileSystemService,
) : TestsSplittingSettings {
    override val filterType: TestsSplittingFilterType get() =
        when (_parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_PARALLEL_TESTS_CURRENT_BATCH)) {
            "1" -> TestsSplittingFilterType.Excludes
            else -> TestsSplittingFilterType.Includes
        }

    override val testClasses: Sequence<String> get() = sequence {
        testClassesFileReader
                ?.use { reader ->
                    while (reader.ready())
                        yield(reader.readLine())
                }
        }
            .map { it.trim() }
            .filter { !it.startsWith("#") }
            .map { it.trim() }
            .filter { it.length > 2 }

    override val hasEnoughTestClassesToActivateSuppression: Boolean get() =
        testClassesFileReader?.use { reader ->
            var lineCount = 0
            var currentChar: Int
            while (reader.read().also { currentChar = it } != -1) {
                if (currentChar == '\n'.code) {
                    if (++lineCount >= suppressionTestClassesThreshold) {
                        return true
                    }
                }
            }
            return false
        } ?: false

    override val exactMatchFilterSize: Int get() =
        _parametersService
            .tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_PARALLEL_TESTS_EXACT_MATCH_FILTER_SIZE)
            .let { runCatching { it?.trim()?.toInt() ?: DefaultExactMatchTestsChunkSize } }
            .getOrDefault(DefaultExactMatchTestsChunkSize)

    override val testClassParametersProcessingMode: TestClassParametersProcessingMode get() =
        _parametersService
            .tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_PARALLEL_TESTS_CLASS_PARAMETERS_PROCESSING_MODE)
            .toTestClassParametersProcessingMode()

    override val testsClassesFilePath: String? get() =
        filterType
            .let {
                when (it) {
                    TestsSplittingFilterType.Excludes -> DotnetConstants.PARAM_PARALLEL_TESTS_EXCLUDES_FILE
                    TestsSplittingFilterType.Includes -> DotnetConstants.PARAM_PARALLEL_TESTS_INCLUDES_FILE
                }
            }
            .let { _parametersService.tryGetParameter(ParameterType.System, it) }

    private val suppressionTestClassesThreshold get() =
        _parametersService
            .tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_PARALLEL_TESTS_SUPPRESSION_TEST_CLASSES_THRESHOLD)
            ?.trim()
            ?.toInt()
            ?: DefaultSuppressionTestClassesThreshold

    private val testsClassesFile: Result<File> get() =
        testsClassesFilePath
            ?.let {
                LOG.debug("Tests classes file path in parameters is \"$it\"")
                _fileSystem.getExistingFile(it)
            }
            ?: Result.failure(Error("Cannot find split tests filter file path in parameter"))

    private val testClassesFileReader: BufferedReader? get() =
        testsClassesFile
            .onFailure {
                LOG.warn("Cannot read tests classes file")
                LOG.warn(it)
            }
            .getOrNull()
            ?.getBufferedReader()


    companion object {
        private val LOG = Logger.getLogger(TestsSplittingSettingsImpl::class.java)
        private const val DefaultExactMatchTestsChunkSize = 10_000
        private const val DefaultSuppressionTestClassesThreshold = 1_000
    }
}