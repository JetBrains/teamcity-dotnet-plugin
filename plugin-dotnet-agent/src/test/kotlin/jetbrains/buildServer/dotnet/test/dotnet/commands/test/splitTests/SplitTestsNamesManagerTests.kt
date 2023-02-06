/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet.test.dotnet.commands.test.splitTests

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.commands.test.splitTests.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class SplitTestsNamesManagerTests {
    @MockK
    private lateinit var _settingsMock: SplitTestsFilterSettings

    @MockK
    private lateinit var _testListFactoryMock: TestsListFactory

    @MockK
    private lateinit var _listTestsOutputValueProcessor: ListTestsOutputValueProcessor

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
        val filterType = SplitTestsFilterType.Includes
        _settingsMock.also { it ->
            every { it.filterType } answers { filterType }
            every { it.testClasses } answers { emptySequence()  }
            every { it.exactMatchFilterSize } answers { 42 }
        }
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
        val filterType = SplitTestsFilterType.Includes
        val namespace = "Namespace"
        val testClasses = sequenceOf("$namespace.TestClass0", "$namespace.TestClass1", namespace)
        _settingsMock.also { it ->
            every { it.filterType } answers { filterType }
            every { it.testClasses } answers { testClasses }
            every { it.exactMatchFilterSize } answers { 42 }
        }
        justRun { _loggerMock.debug(any<String>()) }
        val manager = create()

        // act
        val session = manager.startSession()

        // assert
        Assert.assertNotNull(session)
        verify (exactly = 1) {
            _loggerMock.debug(
                match<String> { msg ->
                    msg.contains("${testClasses.count()} test classes")
                        && msg.contains("1 namespaces")
                        && msg.contains("$filterType")
                }
            )
        }
    }

    @Test
    fun `should not save invalid test name`() {
        // arrange
        val filterType = SplitTestsFilterType.Includes
        val testClasses = sequenceOf("Namespace.TestClass0", "Namespace.TestClass1")
        _settingsMock.also { it ->
            every { it.filterType } answers { filterType }
            every { it.testClasses } answers { testClasses }
            every { it.exactMatchFilterSize } answers { 42 }
        }

        justRun { _loggerMock.debug(any<String>()) }

        val testListMock = mockk<TestsList>()
        every { _testListFactoryMock.new() } answers { testListMock }
        justRun { testListMock.add(any()) }

        every { _listTestsOutputValueProcessor.process(any()) } answers { ProcessedListTestsOutput(false, "")}

        val manager = create()
        val session = manager.startSession()
        val invalidTestName = "${testClasses.first()}._%_INVALID"

        // act
        session.tryToSave(invalidTestName)

        // assert
        verify (exactly = 0) { testListMock.add(any()) }
        verify (exactly = 1) { _loggerMock.debug(match<String> { it.contains(invalidTestName) && it.contains("will be skipped") }) }
    }

    @Test
    fun `should save valid test name with includes filter session`() {
        // arrange
        val filterType = SplitTestsFilterType.Includes
        val testClasses = sequenceOf("Namespace.TestClass0", "Namespace.TestClass1")
        _settingsMock.also { it ->
            every { it.filterType } answers { filterType }
            every { it.testClasses } answers { testClasses }
            every { it.exactMatchFilterSize } answers { 42 }
        }
        justRun { _loggerMock.debug(any<String>()) }

        val testListMock = mockk<TestsList>()
        every { _testListFactoryMock.new() } answers { testListMock }
        justRun { testListMock.add(any()) }

        slot<String>().also { testNameSlot ->
            every { _listTestsOutputValueProcessor.process(capture(testNameSlot)) } answers { ProcessedListTestsOutput(true, testNameSlot.captured)}
        }

        val manager = create()
        val session = manager.startSession()
        val validTestName = "${testClasses.first()}.TestName0"

        // act
        session.tryToSave(validTestName)

        // assert
        verify (exactly = 1) { testListMock.add(validTestName) }
        verify (exactly = 1) { _loggerMock.debug(match<String> { it.contains(validTestName) && it.contains("test name will be saved")  }) }
    }

    @Test
    fun `should save valid test name with excludes filter session`() {
        // arrange
        val filterType = SplitTestsFilterType.Excludes
        val testClasses = sequenceOf("Namespace.TestClass0", "Namespace.TestClass1")
        _settingsMock.also { it ->
            every { it.filterType } answers { filterType }
            every { it.testClasses } answers { testClasses }
            every { it.exactMatchFilterSize } answers { 42 }
        }

        justRun { _loggerMock.debug(any<String>()) }

        val testListMock = mockk<TestsList>()
        every { _testListFactoryMock.new() } answers { testListMock }
        justRun { testListMock.add(any()) }

        slot<String>().also { testNameSlot ->
            every { _listTestsOutputValueProcessor.process(capture(testNameSlot)) } answers { ProcessedListTestsOutput(true, testNameSlot.captured)}
        }

        val manager = create()
        val session = manager.startSession()
        val validTestName = "Namespace.TestClass5555555.TestName0"

        // act
        session.tryToSave(validTestName)

        // assert
        verify (exactly = 1) { testListMock.add(validTestName) }
        verify (exactly = 1) { _loggerMock.debug(match<String> { it.contains(validTestName) && it.contains("test name will be saved")  }) }
    }

    @Test
    fun `should not save valid but excluded test name with include filter session`() {
        // arrange
        val filterType = SplitTestsFilterType.Includes
        val testClasses = sequenceOf("Namespace.TestClass0", "Namespace.TestClass1")
        _settingsMock.also { it ->
            every { it.filterType } answers { filterType }
            every { it.testClasses } answers { testClasses }
            every { it.exactMatchFilterSize } answers { 42 }
        }

        justRun { _loggerMock.debug(any<String>()) }

        val testListMock = mockk<TestsList>()
        every { _testListFactoryMock.new() } answers { testListMock }
        justRun { testListMock.add(any()) }

        slot<String>().also { testNameSlot ->
            every { _listTestsOutputValueProcessor.process(capture(testNameSlot)) } answers { ProcessedListTestsOutput(true, testNameSlot.captured)}
        }

        val manager = create()
        val session = manager.startSession()
        val validTestName = "Namespace.TestClass55555.TestName0"

        // act
        session.tryToSave(validTestName)

        // assert
        verify (exactly = 0) { testListMock.add(any()) }
        verify (exactly = 1) { _loggerMock.debug(match<String> { it.contains(validTestName) && it.contains("test name will be skipped") }) }
    }

    @Test
    fun `should not save valid but included test name with excludes filter session`() {
        // arrange
        val filterType = SplitTestsFilterType.Excludes
        val testClasses = sequenceOf("Namespace.TestClass0", "Namespace.TestClass1")
        _settingsMock.also { it ->
            every { it.filterType } answers { filterType }
            every { it.testClasses } answers { testClasses }
            every { it.exactMatchFilterSize } answers { 42 }
        }

        justRun { _loggerMock.debug(any<String>()) }

        val testListMock = mockk<TestsList>()
        every { _testListFactoryMock.new() } answers { testListMock }
        justRun { testListMock.add(any()) }

        slot<String>().also { testNameSlot ->
            every { _listTestsOutputValueProcessor.process(capture(testNameSlot)) } answers { ProcessedListTestsOutput(true, testNameSlot.captured)}
        }

        val manager = create()
        val session = manager.startSession()
        val validTestName = "${testClasses.first()}.TestName0"

        // act
        session.tryToSave(validTestName)

        // assert
        verify (exactly = 0) { testListMock.add(any()) }
        verify (exactly = 1) { _loggerMock.debug(match<String> { it.contains(validTestName) && it.contains("test name will be skipped") }) }
    }

    @Test
    fun `should save tests names in different test lists`() {
        // arrange
        val filterType = SplitTestsFilterType.Includes
        val testClasses = sequenceOf("Namespace.TestClass0", "Namespace.TestClass1")
        val listSize = 3
        _settingsMock.also { it ->
            every { it.filterType } answers { filterType }
            every { it.testClasses } answers { testClasses }
            every { it.exactMatchFilterSize } answers { listSize }
        }

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

        slot<String>().also { testNameSlot ->
            every { _listTestsOutputValueProcessor.process(capture(testNameSlot)) } answers { ProcessedListTestsOutput(true, testNameSlot.captured)}
        }

        val manager = create()
        val session = manager.startSession()

        val testClassFQN = testClasses.first()

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
        val filterType = SplitTestsFilterType.Includes
        val testClasses = sequenceOf("Namespace.TestClass0", "Namespace.TestClass1")
        val listSize = 3
        _settingsMock.also { it ->
            every { it.filterType } answers { filterType }
            every { it.testClasses } answers { testClasses }
            every { it.exactMatchFilterSize } answers { listSize }
        }

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

        slot<String>().also { testNameSlot ->
            every { _listTestsOutputValueProcessor.process(capture(testNameSlot)) } answers { ProcessedListTestsOutput(true, testNameSlot.captured)}
        }

        val manager = create()
        val session = manager.startSession()

        val testClassFQN = testClasses.first()
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
        verify (exactly = 0) { testListMockList[1].tests }
        verify (exactly = 0) { testListMockList[1].dispose() }
        verify (exactly = 0) { testListMockList[2].tests }
        verify (exactly = 0) { testListMockList[2].dispose() }
    }

    @Test
    fun `should iterates over chunks and read all saved tests names from different test lists`() {
        // arrange
        val filterType = SplitTestsFilterType.Includes
        val testClasses = sequenceOf("Namespace.TestClass0", "Namespace.TestClass1")
        val listSize = 3
        _settingsMock.also { it ->
            every { it.filterType } answers { filterType }
            every { it.testClasses } answers { testClasses }
            every { it.exactMatchFilterSize } answers { listSize }
        }

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

        slot<String>().also { testNameSlot ->
            every { _listTestsOutputValueProcessor.process(capture(testNameSlot)) } answers { ProcessedListTestsOutput(true, testNameSlot.captured)}
        }

        val manager = create()
        val session = manager.startSession()

        val testClassFQN = testClasses.first()
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
        val filterType = SplitTestsFilterType.Includes
        val testClasses = sequenceOf("Namespace.TestClass0", "Namespace.TestClass1")
        _settingsMock.also { it ->
            every { it.filterType } answers { filterType }
            every { it.testClasses } answers { testClasses }
            every { it.exactMatchFilterSize } answers { 42 }
        }

        justRun { _loggerMock.debug(any<String>()) }

        val testListMock = mockk<TestsList>()
        every { _testListFactoryMock.new() } answers { testListMock }
        justRun { testListMock.add(any()) }
        justRun { testListMock.dispose() }

        slot<String>().also { testNameSlot ->
            every { _listTestsOutputValueProcessor.process(capture(testNameSlot)) } answers { ProcessedListTestsOutput(true, testNameSlot.captured)}
        }

        val manager = create()
        val session = manager.startSession()
        session.tryToSave("${testClasses.first()}.TestName0")

        // act
        session.dispose()

        // assert
        verify (exactly = 1) { testListMock.dispose() }
    }

    private fun create() = SplitTestsNamesManager(_settingsMock, _testListFactoryMock, _listTestsOutputValueProcessor)
}