package jetbrains.buildServer.dotnet.test.dotnet.commands.test.splitTests

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.SplitTestsFilterSettings
import jetbrains.buildServer.dotnet.SplittedTestsFilterType
import jetbrains.buildServer.dotnet.commands.test.splitTests.LangIdentifierValidator
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsNamesManager
import jetbrains.buildServer.dotnet.commands.test.splitTests.TestsList
import jetbrains.buildServer.dotnet.commands.test.splitTests.TestsListFactory
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class SplitTestsNamesManagerTests {
    @MockK
    private lateinit var _settingsMock: SplitTestsFilterSettings

    @MockK
    private lateinit var _testListFactoryMock: TestsListFactory

    @MockK
    private lateinit var _langIdentifierValidatorMock: LangIdentifierValidator

    @MockK
    private lateinit var _loggerMock: Logger

    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
        mockkObject(Logger)
        every { Logger.getLogger(any()) } returns _loggerMock
    }

    @Test
    fun `should start session for no test classes`() {
        // arrange
        val filterType = SplittedTestsFilterType.Includes
        every { _settingsMock.filterType } answers { filterType }
        every { _settingsMock.testClasses } answers { emptyList() }
        every { _settingsMock.filterType } answers { SplittedTestsFilterType.Includes }
        justRun { _loggerMock.debug(any<String>()) }
        val manager = create()

        // act
        val session = manager.startSession()

        // assert
        Assert.assertNotNull(session)
        verify (exactly = 1) {
            _loggerMock.debug(
                match<String> { msg ->
                    msg.contains("0 test classes")
                        && msg.contains("0 namespaces")
                        && msg.contains("$filterType")
                }
            )
        }
    }

    @Test
    fun `should start session and test classes loaded to session`() {
        // arrange
        val filterType = SplittedTestsFilterType.Includes
        every { _settingsMock.filterType } answers { filterType }
        val namespace = "Namespace"
        val testClasses = listOf("$namespace.TestClass0", "$namespace.TestClass1", namespace)
        every { _settingsMock.testClasses } answers { testClasses }
        every { _settingsMock.filterType } answers { SplittedTestsFilterType.Includes }
        justRun { _loggerMock.debug(any<String>()) }
        val manager = create()

        // act
        val session = manager.startSession()

        // assert
        Assert.assertNotNull(session)
        verify (exactly = 1) {
            _loggerMock.debug(
                match<String> { msg ->
                    msg.contains("${testClasses.size} test classes")
                        && msg.contains("1 namespaces")
                        && msg.contains("$filterType")
                }
            )
        }
    }

    @Test
    fun `should not save invalid test name`() {
        // arrange
        val filterType = SplittedTestsFilterType.Includes
        every { _settingsMock.filterType } answers { filterType }
        val testClasses = listOf("Namespace.TestClass0", "Namespace.TestClass1")
        every { _settingsMock.testClasses } answers { testClasses }
        every { _settingsMock.filterType } answers { filterType }
        every { _settingsMock.exactMatchFilterSize } answers { 42 }

        justRun { _loggerMock.debug(any<String>()) }

        val testListMock = mockk<TestsList>()
        every { _testListFactoryMock.new() } answers { testListMock }
        justRun { testListMock.add(any()) }

        every { _langIdentifierValidatorMock.isValid(any()) } answers { false }

        val manager = create()
        val session = manager.startSession()
        val invalidTestName = "${testClasses[0]}._%_INVALID"

        // act
        session.tryToSave(invalidTestName)

        // assert
        verify (exactly = 0) { testListMock.add(any()) }
        verify (exactly = 1) { _loggerMock.debug(match<String> { it.contains(invalidTestName) }) }
    }

    @Test
    fun `should save valid test name with includes filter session`() {
        // arrange
        val filterType = SplittedTestsFilterType.Includes
        every { _settingsMock.filterType } answers { filterType }
        val testClasses = listOf("Namespace.TestClass0", "Namespace.TestClass1")
        every { _settingsMock.testClasses } answers { testClasses }
        every { _settingsMock.filterType } answers { filterType }
        every { _settingsMock.exactMatchFilterSize } answers { 42 }

        justRun { _loggerMock.debug(any<String>()) }

        val testListMock = mockk<TestsList>()
        every { _testListFactoryMock.new() } answers { testListMock }
        justRun { testListMock.add(any()) }

        every { _langIdentifierValidatorMock.isValid(any()) } answers { true }

        val manager = create()
        val session = manager.startSession()
        val validTestName = "${testClasses[0]}.TestName0"

        // act
        session.tryToSave(validTestName)

        // assert
        verify (exactly = 1) { testListMock.add(validTestName) }
    }

    @Test
    fun `should save valid test name with excludes filter session`() {
        // arrange
        val filterType = SplittedTestsFilterType.Excludes
        every { _settingsMock.filterType } answers { filterType }
        val testClasses = listOf("Namespace.TestClass0", "Namespace.TestClass1")
        every { _settingsMock.testClasses } answers { testClasses }
        every { _settingsMock.filterType } answers { filterType}
        every { _settingsMock.exactMatchFilterSize } answers { 42 }

        justRun { _loggerMock.debug(any<String>()) }

        val testListMock = mockk<TestsList>()
        every { _testListFactoryMock.new() } answers { testListMock }
        justRun { testListMock.add(any()) }

        every { _langIdentifierValidatorMock.isValid(any()) } answers { true }

        val manager = create()
        val session = manager.startSession()
        val validTestName = "Namespace.TestClass5555555.TestName0"

        // act
        session.tryToSave(validTestName)

        // assert
        verify (exactly = 1) { testListMock.add(validTestName) }
    }

    @Test
    fun `should not save valid but excluded test name with include filter session`() {
        // arrange
        val filterType = SplittedTestsFilterType.Includes
        every { _settingsMock.filterType } answers { filterType }
        val testClasses = listOf("Namespace.TestClass0", "Namespace.TestClass1")
        every { _settingsMock.testClasses } answers { testClasses }
        every { _settingsMock.filterType } answers { filterType}
        every { _settingsMock.exactMatchFilterSize } answers { 42 }

        justRun { _loggerMock.debug(any<String>()) }

        val testListMock = mockk<TestsList>()
        every { _testListFactoryMock.new() } answers { testListMock }
        justRun { testListMock.add(any()) }

        every { _langIdentifierValidatorMock.isValid(any()) } answers { true }

        val manager = create()
        val session = manager.startSession()
        val validTestName = "Namespace.TestClass55555.TestName0"

        // act
        session.tryToSave(validTestName)

        // assert
        verify (exactly = 0) { testListMock.add(any()) }
    }

    @Test
    fun `should not save valid but included test name with excludes filter session`() {
        // arrange
        val filterType = SplittedTestsFilterType.Excludes
        every { _settingsMock.filterType } answers { filterType }
        val testClasses = listOf("Namespace.TestClass0", "Namespace.TestClass1")
        every { _settingsMock.testClasses } answers { testClasses }
        every { _settingsMock.filterType } answers { filterType}
        every { _settingsMock.exactMatchFilterSize } answers { 42 }

        justRun { _loggerMock.debug(any<String>()) }

        val testListMock = mockk<TestsList>()
        every { _testListFactoryMock.new() } answers { testListMock }
        justRun { testListMock.add(any()) }

        every { _langIdentifierValidatorMock.isValid(any()) } answers { true }

        val manager = create()
        val session = manager.startSession()
        val validTestName = "${testClasses[0]}.TestName0"

        // act
        session.tryToSave(validTestName)

        // assert
        verify (exactly = 0) { testListMock.add(any()) }
    }

    @Test
    fun `should save tests names in different test lists`() {
        // arrange
        val filterType = SplittedTestsFilterType.Includes
        every { _settingsMock.filterType } answers { filterType }
        val testClasses = listOf("Namespace.TestClass0", "Namespace.TestClass1")
        every { _settingsMock.testClasses } answers { testClasses }
        every { _settingsMock.filterType } answers { filterType }
        val listSize = 3
        every { _settingsMock.exactMatchFilterSize } answers { listSize }

        justRun { _loggerMock.debug(any<String>()) }

        val testListMockList = listOf<TestsList>(mockk(), mockk(), mockk())
        every {
            _testListFactoryMock.new()
        } answers { testListMockList[0] } andThenAnswer { testListMockList[1] } andThenAnswer { testListMockList[2] }

        testListMockList.forEach { list ->
            var count = 0
            every { list.testsCount } answers { count }
            every { list.add(any()) } answers { count++; Unit }
        }

        every { _langIdentifierValidatorMock.isValid(any()) } answers { true }

        val manager = create()
        val session = manager.startSession()

        val testClassFQN = testClasses[0]

        // act
        var index = 0
        repeat (listSize * testListMockList.size) {
            session.tryToSave("$testClassFQN.TestName$index")
            index++
        }

        // assert
        verify (exactly = 3) { _testListFactoryMock.new() }
        index = 0
        for (list in testListMockList) {
            verify (exactly = 1) { list.add("$testClassFQN.TestName$index") }
            index++
            verify (exactly = 1) { list.add("$testClassFQN.TestName$index") }
            index++
            verify (exactly = 1) { list.add("$testClassFQN.TestName$index") }
            index++
        }
    }

    @Test
    fun `should read all saved tests names from different test lists`() {
        // arrange
        val filterType = SplittedTestsFilterType.Includes
        every { _settingsMock.filterType } answers { filterType }
        val testClasses = listOf("Namespace.TestClass0", "Namespace.TestClass1")
        every { _settingsMock.testClasses } answers { testClasses }
        every { _settingsMock.filterType } answers { filterType }
        val listSize = 3
        every { _settingsMock.exactMatchFilterSize } answers { listSize }

        justRun { _loggerMock.debug(any<String>()) }

        val testListMockList = listOf<TestsList>(mockk(), mockk(), mockk())
        every {
            _testListFactoryMock.new()
        } answers { testListMockList[0] } andThenAnswer { testListMockList[1] } andThenAnswer { testListMockList[2] }

        testListMockList.forEach { list ->
            val testsNames = mutableListOf<String>()
            every { list.testsCount } answers { testsNames.size }
            justRun { list.add(capture(testsNames)) }
            justRun { list.dispose() }
            every { list.tests } answers { sequence { yieldAll(testsNames) } }
        }

        every { _langIdentifierValidatorMock.isValid(any()) } answers { true }

        val manager = create()
        val session = manager.startSession()

        val testClassFQN = testClasses[0]
        val times = listSize * testListMockList.size

        for (index in 0 until times) {
            session.tryToSave("$testClassFQN.TestName$index")
        }

        // act
        val result = session.read().toList()

        // assert
        for (index in 0 until 3) {
            Assert.assertTrue(result.contains("$testClassFQN.TestName$index"))
        }
        verify (exactly = 1) { testListMockList[0].tests }
        verify (exactly = 1) { testListMockList[0].dispose() }
        verify (exactly = 0) { testListMockList[1].tests }
        verify (exactly = 0) { testListMockList[1].dispose() }
        verify (exactly = 0) { testListMockList[2].tests }
        verify (exactly = 0) { testListMockList[2].dispose() }
    }

    @Test
    fun `should iterates over chunks and read all saved tests names from different test lists`() {
        // arrange
        val filterType = SplittedTestsFilterType.Includes
        every { _settingsMock.filterType } answers { filterType }
        val testClasses = listOf("Namespace.TestClass0", "Namespace.TestClass1")
        every { _settingsMock.testClasses } answers { testClasses }
        every { _settingsMock.filterType } answers { filterType }
        val listSize = 3
        every { _settingsMock.exactMatchFilterSize } answers { listSize }

        justRun { _loggerMock.debug(any<String>()) }

        val testListMockList = listOf<TestsList>(mockk(), mockk(), mockk())
        every {
            _testListFactoryMock.new()
        } answers { testListMockList[0] } andThenAnswer { testListMockList[1] } andThenAnswer { testListMockList[2] }

        testListMockList.forEach { list ->
            val testsNames = mutableListOf<String>()
            every { list.testsCount } answers { testsNames.size }
            justRun { list.add(capture(testsNames)) }
            justRun { list.dispose() }
            every { list.tests } answers { sequence { yieldAll(testsNames) } }
        }

        every { _langIdentifierValidatorMock.isValid(any()) } answers { true }

        val manager = create()
        val session = manager.startSession()

        val testClassFQN = testClasses[0]
        val times = listSize * testListMockList.size

        for (index in 0 until times) {
            session.tryToSave("$testClassFQN.TestName$index")
        }

        // act
        val result = mutableListOf<List<String>>()
        session.forEveryTestsNamesChunk {
            result += session.read().toList()
        }.toList()

        // assert
        Assert.assertEquals(result.size, 3)
        for (index in 0 until 3) {
            Assert.assertTrue(result[0].contains("$testClassFQN.TestName$index"))
        }
        for (index in 3 until 6) {
            Assert.assertTrue(result[1].contains("$testClassFQN.TestName$index"))
        }
        for (index in 6 until 9) {
            Assert.assertTrue(result[2].contains("$testClassFQN.TestName$index"))
        }
        verify (exactly = 1) { testListMockList[0].tests }
        verify (exactly = 1) { testListMockList[0].dispose() }
        verify (exactly = 1) { testListMockList[1].tests }
        verify (exactly = 1) { testListMockList[1].dispose() }
        verify (exactly = 1) { testListMockList[2].tests }
        verify (exactly = 1) { testListMockList[2].dispose() }
    }

    @Test
    fun `should dispose session and clear test lists`() {
        // arrange
        val filterType = SplittedTestsFilterType.Includes
        every { _settingsMock.filterType } answers { filterType }
        val testClasses = listOf("Namespace.TestClass0", "Namespace.TestClass1")
        every { _settingsMock.testClasses } answers { testClasses }
        every { _settingsMock.filterType } answers { filterType }
        every { _settingsMock.exactMatchFilterSize } answers { 42 }

        justRun { _loggerMock.debug(any<String>()) }

        val testListMock = mockk<TestsList>()
        every { _testListFactoryMock.new() } answers { testListMock }
        justRun { testListMock.add(any()) }
        justRun { testListMock.dispose() }

        every { _langIdentifierValidatorMock.isValid(any()) } answers { true }

        val manager = create()
        val session = manager.startSession()
        session.tryToSave("${testClasses[0]}.TestName0")

        // act
        session.dispose()

        // assert
        verify (exactly = 1) { testListMock.dispose() }
    }

    private fun create() = SplitTestsNamesManager(_settingsMock, _testListFactoryMock, _langIdentifierValidatorMock)
}