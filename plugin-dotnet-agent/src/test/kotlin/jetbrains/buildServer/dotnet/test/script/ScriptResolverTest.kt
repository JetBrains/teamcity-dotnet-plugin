package jetbrains.buildServer.dotnet.test.script

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.script.ScriptConstants
import jetbrains.buildServer.script.ScriptResolverImpl
import jetbrains.buildServer.script.ScriptType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.*

class ScriptResolverTest {
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _fileSystemService: FileSystemService
    @MockK private lateinit var _pathsService: PathsService
    private val _workingDirectory = File("wd")
    private val _tempFile = File("tmpFile")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _pathsService.getPath(PathType.WorkingDirectory) } returns _workingDirectory
    }

    @DataProvider(name = "cases")
    fun getCases(): Array<Array<out Any?>> {
        return arrayOf(
                // Script file
                arrayOf(
                        mapOf(
                                ScriptConstants.SCRIPT_TYPE to ScriptType.File.id,
                                ScriptConstants.SCRIPT_FILE to "AbsolutScriptFile.csx"
                        ),
                        File("AbsolutScriptFile.csx"),
                        null
                ),

                arrayOf(
                        mapOf(
                                ScriptConstants.SCRIPT_TYPE to ScriptType.File.id,
                                ScriptConstants.SCRIPT_FILE to "Abc.csx"
                        ),
                        File(_workingDirectory, "Abc.csx"),
                        null
                ),

                arrayOf(
                        mapOf(
                                ScriptConstants.SCRIPT_TYPE to ScriptType.File.id,
                                ScriptConstants.SCRIPT_FILE to File(File("SomeDir"), "Abc.csx").path
                        ),
                        File(_workingDirectory, File(File("SomeDir"), "Abc.csx").path),
                        null
                ),

                // Script content
                arrayOf(
                        mapOf(
                                ScriptConstants.SCRIPT_TYPE to ScriptType.Custom.id,
                                ScriptConstants.SCRIPT_CONTENT to "Abc"
                        ),
                        _tempFile,
                        "Abc"
                )
        )
    }

    @Test(dataProvider = "cases")
    fun shouldResolve(
            parameters: Map<String, String>,
            expectedScriptFile: File,
            expectedContent: String?) {
        // Given
        every { _parametersService.tryGetParameter(ParameterType.Runner, any()) } answers {
            parameters[arg<String>(1)]
        }
        every { _fileSystemService.isAbsolute(any()) } returns false
        every { _fileSystemService.isAbsolute(File("AbsolutScriptFile.csx")) } returns true

        val tempDirectory = File("tmp")
        var actualContent: String? = null
        every { _pathsService.getPath(PathType.AgentTemp) } returns tempDirectory
        every { _fileSystemService.generateTempFile(tempDirectory, "CSharpScript", ".csx") } returns _tempFile
        every { _fileSystemService.write(_tempFile, any()) } answers {
            val outputStream = PipedOutputStream()
            val inputStream = PipedInputStream()
            outputStream.connect(inputStream)
            arg<(OutputStream) -> Unit>(1)(outputStream)
            inputStream.use {
                InputStreamReader(it).use {
                    actualContent = it.readText()
                }
            }
        }

        val provider = createInstance()

        // When
        val actualScriptFile = provider.resolve()

        // Then
        Assert.assertEquals(actualScriptFile, expectedScriptFile)
        Assert.assertEquals(actualContent, expectedContent)
    }

    private fun createInstance() =
            ScriptResolverImpl(_parametersService, _fileSystemService, _pathsService)
}