package jetbrains.buildServer.dotnet.commands.test.splitTests

import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.SplittedTestsFilterSettings
import jetbrains.buildServer.dotnet.SplittedTestsFilterType
import java.io.BufferedWriter
import java.io.File
import java.util.*
import java.util.regex.Pattern

class SplitTestsNamesManager(
    private val _settings: SplittedTestsFilterSettings,
    private val _pathsService: PathsService,
) : SplitTestsNamesSessionManager, SplitTestsNamesSession, SplitTestsNamesSaver, SplitTestsNamesReader {
    private val _files: Queue<File> = LinkedList<File>()
    private val _consideringTestsClasses = mutableSetOf<String>()
    private val _whitespacePattern = Pattern.compile("\\s+")
    private var _testsCounter = 0
    private var _isTestsOutputStarted = false
    private var _testsListFileWriter: BufferedWriter? = null
    private var _currentChunk = 0

    override fun startSession(): SplitTestsNamesSession {
        // load split test file with test classes names to set to make a fast search by test names prefixes
        // to understand is a specific test should be included/excluded from result filter
        _settings.testClasses.forEach { _consideringTestsClasses.add(it) }

        LOG.debug(
            "Loaded ${_consideringTestsClasses.size} test classes names " +
            "(filter type is ${_settings.filterType}) to make exact match filter for split tests"
        )

        return this
    }

    override val chunksCount: Int get() =
        Pair(_testsCounter / _settings.exactMatchFilterSize, _testsCounter % _settings.exactMatchFilterSize)
            .let { (div, rest) ->
                when {
                    rest == 0 -> div
                    div < 1 -> 1
                    else -> div + 1
                }
            }


    override fun getSaver(): SplitTestsNamesSaver = Saver(this)

    private class Saver(private val _manager: SplitTestsNamesManager): SplitTestsNamesSaver by _manager {
        override fun dispose() {
            // to dispose current file writer
            _manager._testsListFileWriter?.close()
            _manager._testsListFileWriter = null
            _manager._isTestsOutputStarted = false
        }
    }

    override fun tryToSave(testName: String) {
        if (!testName.contains('.')) {
            LOG.warn(
                "Test name \"$testName\" doen't contain '.' symbol. " +
                        "Tests FQN (fully qualified names) always should contain FQN of test class separated by '.'. This string will be skipped"
            )
            return
        }

        // take only test names from includes/excludes test classes file
        val purposedToSave =
            when (_settings.filterType) {
                SplittedTestsFilterType.Includes -> containsInTestClassesList(testName)
                SplittedTestsFilterType.Excludes -> containsInTestClassesList(testName).not()
            }

        if (!purposedToSave) {
            return
        }

        // split to chunks and write in dedicated files
        fileWriter.appendLine(testName)

        _testsCounter++
    }

    override fun read() = sequence {
        _files.poll()
            ?.bufferedReader()?.use { reader ->
                while (reader.ready())
                    yield(reader.readLine())
            }
    }

    private fun containsInTestClassesList(testName: String) =
        _consideringTestsClasses.contains(testName.substringBeforeLast('.'))

    private val fileWriter get(): BufferedWriter {
        if (_currentChunk < chunksCount || _testsListFileWriter == null) {
            _testsListFileWriter?.close()
            _testsListFileWriter = newFileWriter()
            _currentChunk++
        }

        return _testsListFileWriter as BufferedWriter
    }

    private fun newFileWriter(): BufferedWriter {
        val file = _pathsService.getTempFileName(".tests")
        _files.add(file)
        return file.bufferedWriter()
    }

    // to close session
    override fun dispose() {
        _files.clear()
        _consideringTestsClasses.clear()
    }

    companion object {
        private const val TestsListOutputMarker = "The following Tests are available:"
        private val LOG = Logger.getLogger(SplitTestsNamesManager::class.java)
    }
}