package jetbrains.buildServer.dotnet.test.dotnet.coverage.utils

import jetbrains.buildServer.dotnet.coverage.Sequencer
import jetbrains.buildServer.dotnet.coverage.utils.FileService
import jetbrains.buildServer.dotnet.coverage.utils.TempFactory
import jetbrains.buildServer.dotnet.coverage.utils.TempFactoryImpl
import jetbrains.buildServer.dotnet.test.utils.TestUtils
import org.assertj.core.api.BDDAssertions
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.api.Invocation
import org.jmock.lib.action.CustomAction
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.io.IOException

class TempFactoryTest {

    private lateinit var _ctx: Mockery
    private lateinit var _fileService: FileService
    private lateinit var _sequencer: Sequencer<String>

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _fileService = _ctx.mock(FileService::class.java)
        _sequencer = _ctx.mock(Sequencer::class.java) as Sequencer<String>
    }

    @Test
    @Throws(IOException::class)
    fun shouldCreateTempFileWhenHasNoFile() {
        // arrange
        val factory: TempFactory = createInstance()
        val file0 = File("tempDir", "my.txt")

        // act
        _ctx.checking(object : Expectations() {
            init {
                allowing(_fileService).sanitizeFileName(TestUtils.uninitialized(with(any(String::class.java))))
                will(object : CustomAction("sanitizeFileName") {
                    @Throws(Throwable::class)
                    override fun invoke(invocation: Invocation): Any {
                        return invocation.getParameter(0)
                    }
                })
                oneOf(_fileService).createDirectory(File("tempDir"))
                oneOf(_fileService).exists(file0)
                will(returnValue(false))
                oneOf(_fileService).isDirectory(file0)
                will(returnValue(false))
                oneOf(_fileService).createFile(file0)
                will(returnValue(true))
            }
        })
        val tempFile: File = factory.createTempFile(File("tempDir"), "my", ".txt", 100)

        // assert
        _ctx.assertIsSatisfied()
        BDDAssertions.then(tempFile).isEqualTo(file0)
    }

    @Test
    @Throws(IOException::class)
    fun shouldCreateTempFileWhenHasFile() {
        // arrange
        val factory: TempFactory = createInstance()
        val file0 = File("tempDir", "my.txt")
        val file1 = File("tempDir", "my1.txt")

        // act
        _ctx.checking(object : Expectations() {
            init {
                allowing(_fileService).sanitizeFileName(TestUtils.uninitialized(with(any(String::class.java))))
                will(object : CustomAction("sanitizeFileName") {
                    @Throws(Throwable::class)
                    override fun invoke(invocation: Invocation): Any {
                        return invocation.getParameter(0)
                    }
                })
                oneOf(_fileService).createDirectory(File("tempDir"))
                oneOf(_fileService).exists(file0)
                will(returnValue(true))
                oneOf(_sequencer).nextFrom("")
                will(returnValue("1"))
                oneOf(_fileService).exists(file1)
                will(returnValue(false))
                oneOf(_fileService).isDirectory(file1)
                will(returnValue(false))
                oneOf(_fileService).createFile(file1)
                will(returnValue(true))
            }
        })
        val tempFile: File = factory.createTempFile(File("tempDir"), "my", ".txt", 100)

        // assert
        _ctx.assertIsSatisfied()
        BDDAssertions.then(tempFile).isEqualTo(file1)
    }

    @Test
    @Throws(IOException::class)
    fun shouldCreateTempFileWhenFileIsDirectory() {
        // arrange
        val factory: TempFactory = createInstance()
        val file0 = File("tempDir", "my.txt")
        val file1 = File("tempDir", "my1.txt")

        // act
        _ctx.checking(object : Expectations() {
            init {
                allowing(_fileService).sanitizeFileName(TestUtils.uninitialized(with(any(String::class.java))))
                will(object : CustomAction("sanitizeFileName") {
                    @Throws(Throwable::class)
                    override fun invoke(invocation: Invocation): Any {
                        return invocation.getParameter(0)
                    }
                })
                oneOf(_fileService).createDirectory(File("tempDir"))
                oneOf(_fileService).exists(file0)
                will(returnValue(false))
                oneOf(_fileService).isDirectory(file0)
                will(returnValue(true))
                oneOf(_sequencer).nextFrom("")
                will(returnValue("1"))
                oneOf(_fileService).exists(file1)
                will(returnValue(false))
                oneOf(_fileService).isDirectory(file1)
                will(returnValue(false))
                oneOf(_fileService).createFile(file1)
                will(returnValue(true))
            }
        })
        val tempFile: File = factory.createTempFile(File("tempDir"), "my", ".txt", 100)

        // assert
        _ctx.assertIsSatisfied()
        BDDAssertions.then(tempFile).isEqualTo(file1)
    }

    @Test
    @Throws(IOException::class)
    fun shouldCreateTempFileWhenCannotCreateFile() {
        // arrange
        val factory: TempFactory = createInstance()
        val file0 = File("tempDir", "my.txt")
        val file1 = File("tempDir", "my1.txt")

        // act
        _ctx.checking(object : Expectations() {
            init {
                allowing(_fileService).sanitizeFileName(TestUtils.uninitialized(with(any(String::class.java))))
                will(object : CustomAction("sanitizeFileName") {
                    @Throws(Throwable::class)
                    override fun invoke(invocation: Invocation): Any {
                        return invocation.getParameter(0)
                    }
                })
                oneOf(_fileService).createDirectory(File("tempDir"))
                oneOf(_fileService).exists(file0)
                will(returnValue(false))
                oneOf(_fileService).isDirectory(file0)
                will(returnValue(false))
                oneOf(_fileService).createFile(file0)
                will(returnValue(false))
                oneOf(_sequencer).nextFrom("")
                will(returnValue("1"))
                oneOf(_fileService).exists(file1)
                will(returnValue(false))
                oneOf(_fileService).isDirectory(file1)
                will(returnValue(false))
                oneOf(_fileService).createFile(file1)
                will(returnValue(true))
            }
        })
        val tempFile: File = factory.createTempFile(File("tempDir"), "my", ".txt", 100)

        // assert
        _ctx.assertIsSatisfied()
        BDDAssertions.then(tempFile).isEqualTo(file1)
    }

    @Test
    @Throws(IOException::class)
    fun shouldCreateTempFileWhenHasExceptionDuringFileCreation() {
        // arrange
        val factory: TempFactory = createInstance()
        val file0 = File("tempDir", "my.txt")
        val file1 = File("tempDir", "my1.txt")

        // act
        _ctx.checking(object : Expectations() {
            init {
                allowing(_fileService).sanitizeFileName(TestUtils.uninitialized(with(any(String::class.java))))
                will(object : CustomAction("sanitizeFileName") {
                    @Throws(Throwable::class)
                    override fun invoke(invocation: Invocation): Any {
                        return invocation.getParameter(0)
                    }
                })
                oneOf(_fileService).createDirectory(File("tempDir"))
                oneOf(_fileService).exists(file0)
                will(returnValue(false))
                oneOf(_fileService).isDirectory(file0)
                will(returnValue(false))
                oneOf(_fileService).createFile(file0)
                will(throwException(IOException()))
                oneOf(_sequencer).nextFrom("")
                will(returnValue("1"))
                oneOf(_fileService).exists(file1)
                will(returnValue(false))
                oneOf(_fileService).isDirectory(file1)
                will(returnValue(false))
                oneOf(_fileService).createFile(file1)
                will(returnValue(true))
            }
        })
        val tempFile: File = factory.createTempFile(File("tempDir"), "my", ".txt", 1)

        // assert
        _ctx.assertIsSatisfied()
        BDDAssertions.then(tempFile).isEqualTo(file1)
    }

    @Test
    @Throws(IOException::class)
    fun shouldThrowExceptionWhenAllAttemptsEnded() {
        // arrange
        val factory: TempFactory = createInstance()
        val file0 = File("tempDir", "my.txt")
        val file1 = File("tempDir", "my1.txt")
        val file2 = File("tempDir", "my2.txt")

        // act
        _ctx.checking(object : Expectations() {
            init {
                allowing(_fileService).sanitizeFileName(TestUtils.uninitialized(with(any(String::class.java))))
                will(object : CustomAction("sanitizeFileName") {
                    @Throws(Throwable::class)
                    override fun invoke(invocation: Invocation): Any {
                        return invocation.getParameter(0)
                    }
                })
                oneOf(_fileService).createDirectory(File("tempDir"))
                oneOf(_fileService).exists(file0)
                will(returnValue(false))
                oneOf(_fileService).isDirectory(file0)
                will(returnValue(false))
                oneOf(_fileService).createFile(file0)
                will(throwException(IOException()))
                oneOf(_sequencer).nextFrom("")
                will(returnValue("1"))
                oneOf(_fileService).exists(file1)
                will(returnValue(false))
                oneOf(_fileService).isDirectory(file1)
                will(returnValue(false))
                oneOf(_fileService).createFile(file1)
                will(throwException(IOException()))
                oneOf(_sequencer).nextFrom("1")
                will(returnValue("2"))
                oneOf(_fileService).exists(file2)
                will(returnValue(false))
                oneOf(_fileService).isDirectory(file2)
                will(returnValue(false))
                oneOf(_fileService).createFile(file2)
                will(throwException(IOException()))
            }
        })
        var actualException: IOException? = null
        try {
            factory.createTempFile(File("tempDir"), "my", ".txt", 1)
        } catch (ex: IOException) {
            actualException = ex
        }

        // assert
        _ctx.assertIsSatisfied()
        BDDAssertions.then(actualException).isNotNull()
    }

    @Test
    @Throws(IOException::class)
    fun shouldCreateTempDirectoryWhenHasNoDirectory() {
        // arrange
        val factory: TempFactory = createInstance()
        val tempDir0 = File("tempDir")

        // act
        _ctx.checking(object : Expectations() {
            init {
                oneOf(_fileService).exists(tempDir0)
                will(returnValue(false))
                oneOf(_fileService).createDirectory(tempDir0)
            }
        })
        val tempDir: File = factory.createTempDirectory(tempDir0, 100)

        // assert
        _ctx.assertIsSatisfied()
        BDDAssertions.then(tempDir).isEqualTo(tempDir0)
    }

    @Test
    @Throws(IOException::class)
    fun shouldReturnTempDirectoryWhenItExists() {
        // arrange
        val factory: TempFactory = createInstance()
        val tempDir0 = File("tempDir")

        // act
        _ctx.checking(object : Expectations() {
            init {
                oneOf(_fileService).exists(tempDir0)
                will(returnValue(true))
                oneOf(_fileService).isDirectory(tempDir0)
                will(returnValue(true))
                never(_fileService).createDirectory(tempDir0)
            }
        })
        val tempDir: File = factory.createTempDirectory(tempDir0, 100)

        // assert
        _ctx.assertIsSatisfied()
        BDDAssertions.then(tempDir).isEqualTo(tempDir0)
    }

    @Test
    @Throws(IOException::class)
    fun shouldCreateNextTempDirectoryWhenHasNoDirectory() {
        // arrange
        val factory: TempFactory = createInstance()
        val tempDir0 = File("tempDir")
        val tempDir1 = File("tempDir1")

        // act
        _ctx.checking(object : Expectations() {
            init {
                oneOf(_fileService).exists(tempDir0)
                will(returnValue(true))
                oneOf(_fileService).isDirectory(tempDir0)
                will(returnValue(false))
                oneOf(_sequencer).nextFrom("")
                will(returnValue("1"))
                oneOf(_fileService).exists(tempDir1)
                will(returnValue(false))
                oneOf(_fileService).createDirectory(tempDir1)
            }
        })
        val tempDir: File = factory.createTempDirectory(tempDir0, 100)

        // assert
        _ctx.assertIsSatisfied()
        BDDAssertions.then(tempDir).isEqualTo(tempDir1)
    }

    private fun createInstance(): TempFactory {
        return TempFactoryImpl(_fileService, _sequencer)
    }
}