package jetbrains.buildServer.dotnet.commands.test.splitTests

import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.SplittedTestsFilterSettings
import jetbrains.buildServer.dotnet.SplittedTestsFilterType
import java.util.*

class SplitTestsNamesManager(
    private val _settings: SplittedTestsFilterSettings,
    private val _testListFactory: TestsListFactory,
    private val _testNameValidator: TestNameValidator,
) : SplitTestsNamesSessionManager, SplitTestsNamesSession, SplitTestsNamesSaver, SplitTestsNamesReader {
    private val _testsLists: Queue<TestsList> = LinkedList<TestsList>()
    private val _consideringTestsClasses = mutableSetOf<String>()

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

    override fun <T> forEveryTestsNamesChunk(handleChunk: () -> T) = sequence<T> {
        while (!_testsLists.isEmpty()) {
            yield(handleChunk())
            _testsLists.remove()
        }
    }

    override fun tryToSave(testName: String) {
        if (!_testNameValidator.isValid(testName)) {
            LOG.debug("String \"$testName\" is not valid test name. This string will be skipped")
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

        save(testName)
    }

    override fun read() =
        _testsLists.peek()
            ?.let { testsList ->
                val tests = testsList.tests
                testsList.dispose()
                tests
            }
            ?: emptySequence()

    private fun containsInTestClassesList(testName: String) =
        _consideringTestsClasses.contains(testName.substringBeforeLast('.'))

    private fun save(testName: String) {
        val testList = when {
            // split to different lists and write in dedicated files
            shouldCreateNewList -> {
                val testList = _testListFactory.new()
                _testsLists.add(testList)
                testList
            }

            else -> _testsLists.last()
        }
        testList.add(testName)
    }

    private val shouldCreateNewList: Boolean get() =
        _testsLists
            .fold(0) { acc, list -> acc + list.testsCount }
            .let { testsCount -> Pair(testsCount / _settings.exactMatchFilterSize, testsCount % _settings.exactMatchFilterSize) }
            .let { (div, rest) ->
                when {
                    div == 0 && rest == 0 -> 0       // 0 / 42 == 0      and     0 % 42 == 0    -->     0 lists
                    div == 0 && rest != 0 -> 1       // 41 / 42 == 0     and     41 % 42 == 41  -->     1 list
                    div != 0 && rest == 0 -> div     // 84 / 42 == 2     and     84 % 42 == 0   -->     2 lists
                    else -> div + 1                  // 85 / 42 == 2     and     85 % 42 == 1   -->     3 lists
                }
            }
            .let { _testsLists.size == 0 || _testsLists.size < it }

    // to close session
    override fun dispose() {
        _testsLists.forEach { it.dispose() }
        _testsLists.clear()
        _consideringTestsClasses.clear()
    }

    companion object {
        private val LOG = Logger.getLogger(SplitTestsNamesManager::class.java)
    }
}
