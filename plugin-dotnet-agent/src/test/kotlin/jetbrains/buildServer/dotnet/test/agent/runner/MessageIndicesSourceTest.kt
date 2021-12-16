package jetbrains.buildServer.dotnet.test.agent.runner

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.agent.runner.Index
import jetbrains.buildServer.agent.runner.MessageIndicesSource
import jetbrains.buildServer.agent.runner.Source
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MessageIndicesSourceTest {
    @MockK
    private lateinit var _positionsSource: Source<Long>

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testAddMisingMessages(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(0L, 1L, 0L, 1L, emptySequence<Long>(), emptyList<Long>()),
                arrayOf(0L, 1L, 0L, 1L, sequenceOf(11L), listOf(Index(0L, 11L))),
                arrayOf(0L, 1L, 0L, 1L, sequenceOf(12L), listOf(Index(0L, 12L))),
                arrayOf(0L, 2L, 0L, 2L, sequenceOf(11L, 22L), listOf(Index(0L, 11L), Index(11L, 11L))),
                arrayOf(1L, 1L, 0L, 2L, sequenceOf(10L, 22L), listOf(Index(10L, 12L))),
                arrayOf(1L, 2L, 0L, 3L, sequenceOf(11L, 22L, 33L), listOf(Index(11L, 11L), Index(22L, 11L))),
                arrayOf(2L, 2L, 1L, 3L, sequenceOf(22L, 33L, 44L), listOf(Index(22L, 11L), Index(33L, 11L))),
                arrayOf(2L, 2L, 1L, 3L, sequenceOf(22L, 33L, 45L), listOf(Index(22L, 11L), Index(33L, 12L))),

                // Invalid positions
                arrayOf(0L, 1L, 0L, 1L, sequenceOf(-11L), emptyList<Index>()),
                arrayOf(0L, 2L, 0L, 2L, sequenceOf(11L, 10L), listOf(Index(0L, 11L))),
                arrayOf(1L, 2L, 0L, 3L, sequenceOf(11L, 22L, 20L), listOf(Index(11L, 11L))),
                arrayOf(1L, 2L, 0L, 3L, sequenceOf(11L, 10L, 33L), emptyList<Index>()),
        )
    }

    @Test(dataProvider = "testAddMisingMessages")
    fun shouldProvideIndices(fromPosition: Long, count: Long, messagesFromPosition: Long, messagesCoount: Long, positions: Sequence<Long>, expectedIndices: List<Index>) {
        // Given
        val source = createInstance()
        every { _positionsSource.read("Src", messagesFromPosition, messagesCoount) } returns positions

        // When
        val actualIndices = source.read("Src", fromPosition, count).toList()

        // Then
        verify { _positionsSource.read("Src", messagesFromPosition, messagesCoount) }
        Assert.assertEquals(actualIndices, expectedIndices)
    }

    private fun createInstance() =
            MessageIndicesSource(_positionsSource)
}