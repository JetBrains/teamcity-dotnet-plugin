package jetbrains.buildServer.dotnet.test.agent.runner

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.agent.FileReadOperation
import jetbrains.buildServer.agent.FileReadOperationResult
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.MessagePositionsSource
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.runner.Source
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.nio.ByteBuffer

private const val Long_SIZE = 8L

class MessagePositionsSourceTest {
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _fileSystemService: FileSystemService
    private val _file = File(File("Base"), "Src")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _pathsService.getPath(PathType.AgentTemp) } returns File("Base")
        every { _fileSystemService.isExists(_file) } returns true
        every { _fileSystemService.isFile(_file) } returns true
    }

    @Test
    fun shoLdProvideFirstPosition() {
        // Given
        val source = createInstance()
        every { _fileSystemService.readBytes(_file, match { it.count() == 1 && it.single().fromPosition == 0L }) } answers {
            val operation = arg<Sequence<FileReadOperation>>(1).single();
            val bytes = operation.to;
            val buffer = ByteBuffer.wrap(bytes);
            buffer.putLong(0, 33L)
            sequenceOf(FileReadOperationResult(operation, bytes.size))
        }

        // When
        var actualPositions = source.read("Src", 0L, 1L).toList()

        // Then
        Assert.assertEquals(actualPositions, listOf(33L))
    }

    @Test
    fun shoLdProvidePositions() {
        // Given
        val source = createInstance()
        every { _fileSystemService.readBytes(_file, match { it.count() == 1 && it.single().fromPosition == 1L * Long_SIZE }) } answers {
            val operation = arg<Sequence<FileReadOperation>>(1).single();
            val bytes = operation.to;
            val buffer = ByteBuffer.wrap(bytes);
            buffer.putLong(0, 33L)
            buffer.putLong((1L * Long_SIZE).toInt(), 53L)
            buffer.putLong((2L * Long_SIZE).toInt(), 73L)
            sequenceOf(FileReadOperationResult(operation, bytes.size))
        }

        // When
        var actualPositions = source.read("Src", 1L, 3L).toList()

        // Then
        Assert.assertEquals(actualPositions, listOf(33L, 53L, 73L))
    }

    @Test
    fun shoLdDetectCorruptedPositions() {
        // Given
        val source = createInstance()
        every { _fileSystemService.readBytes(_file, match { it.count() == 1 && it.single().fromPosition == 1L * Long_SIZE }) } answers {
            val operation = arg<Sequence<FileReadOperation>>(1).single();
            val bytes = operation.to;
            val buffer = ByteBuffer.wrap(bytes);
            buffer.putLong(0, 33L)
            buffer.putLong((1L * Long_SIZE).toInt(), 93L)
            buffer.putLong((2L * Long_SIZE).toInt(), 53L)
            sequenceOf(FileReadOperationResult(operation, bytes.size))
        }

        // When
        var actualPositions = source.read("Src", 1L, 3L).toList()

        // Then
        Assert.assertEquals(actualPositions, listOf(33L, 93L))
    }

    @Test
    fun shoLdNotProvideWhenCountIsZero() {
        // Given
        val source = createInstance()

        // When
        var actualPositions = source.read("Src", 0L, 0L).toList()

        // Then
        Assert.assertEquals(actualPositions, emptyList<Long>())
    }

    @Test
    fun shoLdNotProvideWhenFileIsMissing() {
        // Given
        val source = createInstance()

        // When
        every { _fileSystemService.isExists(_file) } returns false
        var actualPositions = source.read("Src", 0L, 1L).toList()

        // Then
        Assert.assertEquals(actualPositions, emptyList<Long>())
    }

    @Test
    fun shoLdNotProvideWhenDiretoryInsteadFile() {
        // Given
        val source = createInstance()

        // When
        every { _fileSystemService.isFile(_file) } returns false
        var actualPositions = source.read("Src", 0L, 1L).toList()

        // Then
        Assert.assertEquals(actualPositions, emptyList<Long>())
    }

    @Test
    fun shoLdNotProvideWhenHasNoData() {
        // Given
        val source = createInstance()

        // When
        every { _fileSystemService.readBytes(_file, match { it.count() == 1 && it.single().fromPosition == 0L }) } answers {
            val operation = arg<Sequence<FileReadOperation>>(1).single();
            val bytes = operation.to;
            sequenceOf(FileReadOperationResult(operation, bytes.size - 1))
        }

        var actualPositions = source.read("Src", 0L, 1L).toList()

        // Then
        Assert.assertEquals(actualPositions, emptyList<Long>())
    }

    private fun createInstance() =
            MessagePositionsSource(_pathsService, _fileSystemService)
}