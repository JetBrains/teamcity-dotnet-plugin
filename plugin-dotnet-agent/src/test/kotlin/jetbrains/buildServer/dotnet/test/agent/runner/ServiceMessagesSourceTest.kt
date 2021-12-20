package jetbrains.buildServer.dotnet.test.agent.runner

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.FileReadOperation
import jetbrains.buildServer.agent.FileReadOperationResult
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class ServiceMessagesSourceTest {
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _fileSystemService: FileSystemService
    @MockK private lateinit var _indicesSource: Source<Index>
    private val _file = File(File("Base"), "Src.msg")
    private val _text1Str = "Text 1"
    private val _text1 = _text1Str.toByteArray(Charsets.UTF_8)
    private val _text2Str = "                 Text    2   "
    private val _text2 = _text2Str.toByteArray(Charsets.UTF_8)
    private val _text2StrTrimmed = _text2Str.trimEnd()

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _pathsService.getPath(PathType.AgentTemp) } returns File("Base")
        every { _fileSystemService.isExists(_file) } returns true
        every { _fileSystemService.isFile(_file) } returns true
    }

    @Test
    fun shoLdProvideMessageWhenPositionIsZero() {
        // Given
        val source = createInstance()
        every { _fileSystemService.readBytes(_file, match { it.count() == 1 && it.single().fromPosition == 0L }) } answers {
            val operation = arg<Sequence<FileReadOperation>>(1).single();
            val bytes = operation.to;
            _text1.copyInto(bytes)
            sequenceOf(FileReadOperationResult(operation, bytes.size))
        }

        // When
        every { _indicesSource.read("Src", 0L, 1L) } returns sequenceOf(Index(0L, _text1.size.toLong()))
        var actualPositions = source.read("Src", 0L, 1L).toList()

        // Then
        Assert.assertEquals(actualPositions, listOf(_text1Str))
    }

    @Test
    fun shoLdProvideSeveralMessages() {
        // Given
        val source = createInstance()
        every { _fileSystemService.readBytes(_file, match { it.count() == 2 && it.first().fromPosition == 33L && it.last().fromPosition == 73L }) } answers {
            val operations = arg<Sequence<FileReadOperation>>(1).toList();
            _text1.copyInto(operations[0].to)
            _text2.copyInto(operations[1].to)
            sequenceOf(
                    FileReadOperationResult(operations[0], operations[0].to.size),
                    FileReadOperationResult(operations[1], operations[1].to.size))
        }

        // When
        every { _indicesSource.read("Src", 1L, 2L) } returns sequenceOf(
                Index(33L, _text1.size.toLong()),
                Index(73L, _text2.size.toLong())
        )

        var actualPositions = source.read("Src", 1L, 2L).toList()

        // Then
        Assert.assertEquals(actualPositions, listOf(_text1Str, _text2StrTrimmed))
    }

    @Test
    fun shoLdNotProvideMessageWhenHasNoData() {
        // Given
        val source = createInstance()
        every { _fileSystemService.readBytes(_file, match { it.count() == 2 && it.first().fromPosition == 33L && it.last().fromPosition == 73L }) } answers {
            val operations = arg<Sequence<FileReadOperation>>(1).toList();
            _text1.copyInto(operations[0].to)
            _text2.copyInto(operations[1].to)
            sequenceOf(
                    FileReadOperationResult(operations[0], operations[0].to.size - 1),
                    FileReadOperationResult(operations[1], operations[1].to.size))
        }

        every { _indicesSource.read("Src", 1L, 2L) } returns sequenceOf(
                Index(33L, _text1.size.toLong()),
                Index(73L, _text2.size.toLong())
        )

        var actualPositions = source.read("Src", 1L, 2L).toList()

        // Then
        Assert.assertEquals(actualPositions, listOf(_text2StrTrimmed))
    }

    @Test
    fun shoLdNotProvideMessageWhenFileIsMissing() {
        // Given
        val source = createInstance()

        // When
        every { _fileSystemService.isExists(_file) } returns false
        var actualPositions = source.read("Src", 1L, 2L).toList()

        // Then
        Assert.assertEquals(actualPositions, emptyList<String>())
    }

    @Test
    fun shoLdNotProvideMessageWhenDiretoryInsteadFile() {
        // Given
        val source = createInstance()

        // When
        every { _fileSystemService.isFile(_file) } returns false
        var actualPositions = source.read("Src", 1L, 2L).toList()

        // Then
        Assert.assertEquals(actualPositions, emptyList<String>())
    }

    private fun createInstance() =
            ServiceMessagesSource(_pathsService, _fileSystemService, _indicesSource)
}