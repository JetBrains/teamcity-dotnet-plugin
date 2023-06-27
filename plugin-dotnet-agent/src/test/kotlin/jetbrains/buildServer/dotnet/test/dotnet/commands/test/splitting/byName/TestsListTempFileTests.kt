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

package jetbrains.buildServer.dotnet.test.dotnet.commands.test.splitting.byName

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.dotnet.commands.test.splitting.byTestName.TestsListTempFile
import jetbrains.buildServer.utils.getBufferedReader
import jetbrains.buildServer.utils.getBufferedWriter
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File

class TestsListTempFileTests {
    @MockK
    private lateinit var _fileMock: File

    @BeforeClass
    fun beforeAll() = MockKAnnotations.init(this)

    @BeforeMethod
    fun setup() {
        mockkStatic(File::getBufferedWriter)
        mockkStatic(File::getBufferedReader)
        clearAllMocks()
    }

    @Test
    fun `test count should be zero initially`() {
        // arrange
        val testListTempFile = create()

        // act
        val result = testListTempFile.testsCount

        // assert
        Assert.assertEquals(result, 0)
    }

    @Test
    fun `tests should be empty initially`() {
        // arrange
        val readerMock = mockk<BufferedReader>()
        every { readerMock.ready() } answers { false }
        every { readerMock.close() } answers { mockk() }

        every { _fileMock.getBufferedReader() } returns readerMock

        val testListTempFile = create()

        // act
        val result = testListTempFile.tests.toList()

        // assert
        Assert.assertEquals(result.size, 0)
        verify (exactly = 1) { _fileMock.getBufferedReader() }
        verify (exactly = 1) { readerMock.ready() }
        verify (exactly = 0) { readerMock.readLine() }
        verify (exactly = 1) { readerMock.close() }
    }

    @Test
    fun `should add test names`() {
        // arrange
        val (first, second) = Pair("123", "456")
        val writerMock = mockk<BufferedWriter>()
        every { writerMock.write(any<String>()) } answers { mockk() }
        every { writerMock.newLine() } answers { mockk() }
        every { writerMock.close() } answers { mockk() }

        val readerMock = mockk<BufferedReader>()
        every {
            readerMock.ready()
        } answers { true } andThenAnswer { false } andThenAnswer { true } andThenAnswer { true } andThenAnswer { false }
        every { readerMock.readLine() } answers { first } andThenAnswer { first } andThenAnswer { second }
        every { readerMock.close() } answers { mockk() }

        every { _fileMock.getBufferedWriter() } returns writerMock
        every { _fileMock.getBufferedReader() } returns readerMock

        val testListTempFile = create()

        // act
        testListTempFile.add(first)
        val testCounterAfterFirst = testListTempFile.testsCount
        val testsAfterFirst = testListTempFile.tests.toList()
        testListTempFile.add(second)
        val testCounterAfterSecond = testListTempFile.testsCount
        val testsAfterSecond = testListTempFile.tests.toList()

        // assert
        Assert.assertEquals(testCounterAfterFirst, 1)
        Assert.assertEquals(testsAfterFirst.size, 1)
        Assert.assertEquals(testsAfterFirst[0], first)
        Assert.assertEquals(testCounterAfterSecond, 2)
        Assert.assertEquals(testsAfterSecond.size, 2)
        Assert.assertEquals(testsAfterSecond[0], first)
        Assert.assertEquals(testsAfterSecond[1], second)
        verify (exactly = 2) { _fileMock.getBufferedWriter() }
        verify (exactly = 2) { _fileMock.getBufferedReader() }
        verify (exactly = 2) { writerMock.write(any<String>()) }
        verify (exactly = 2) { writerMock.newLine() }
        verify (exactly = 5) { readerMock.ready() }
        verify (exactly = 3) { readerMock.readLine() }
        verify (exactly = 2) { readerMock.close() }
    }

    @Test
    fun `should reinit on dispose`() {
        // arrange
        val writerMock = mockk<BufferedWriter>()
        every { writerMock.write(any<String>()) } answers { mockk() }
        every { writerMock.newLine() } answers { mockk() }
        every { writerMock.close() } answers { mockk() }


        val readerMock = mockk<BufferedReader>()
        every { readerMock.ready() } answers { false }
        every { readerMock.close() } answers { mockk() }

        every { _fileMock.getBufferedWriter() } returns writerMock
        every { _fileMock.getBufferedReader() } returns readerMock

        val testListTempFile = create()

        // act
        testListTempFile.add("abc")
        testListTempFile.dispose()
        val testsCount = testListTempFile.testsCount
        val tests = testListTempFile.tests.toList()

        // assert
        Assert.assertEquals(testsCount, 0)
        Assert.assertEquals(tests.size, 0)
        verify (exactly = 1) { _fileMock.getBufferedWriter() }
        verify (exactly = 1) { _fileMock.getBufferedReader() }
        verify (exactly = 1) { readerMock.ready() }
        verify (exactly = 0) { readerMock.readLine() }
        verify (exactly = 1) { readerMock.close() }
        verify (exactly = 1) { writerMock.newLine() }
        verify (exactly = 1) { writerMock.close() }
    }

    private fun create() = TestsListTempFile(_fileMock)
}