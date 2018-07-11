package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.DotnetCliToolInfoImpl
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Version
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class DotnetCliToolInfoTest {
    private lateinit var _ctx: Mockery
    private lateinit var _commandLineExecutor: CommandLineExecutor
    private lateinit var _versionParser: VersionParser
    private lateinit var _toolProvider: ToolProvider

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _commandLineExecutor = _ctx.mock(CommandLineExecutor::class.java)
        _versionParser = _ctx.mock(VersionParser::class.java)
        _toolProvider = _ctx.mock(ToolProvider::class.java)
    }

    @Test
    fun shouldGetDotnetVersionByPath() {
        // Given
        val workingDirectoryPath = File("wd")
        val toolPath = File("dotnet")
        val versionCommandline = CommandLine(
                TargetType.Tool,
                toolPath,
                workingDirectoryPath,
                DotnetCliToolInfoImpl.versionArgs,
                emptyList())

        val stdOut = sequenceOf("stdOut")
        val stdErr = sequenceOf("stdErr")
        val getVersionResult = CommandLineResult(sequenceOf(0), stdOut, stdErr)
        val versionStr = "1.0.1"
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ToolProvider>(_toolProvider).getPath(DotnetConstants.EXECUTABLE)
                will(returnValue(toolPath.path))

                oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(versionCommandline)
                will(returnValue(getVersionResult))

                oneOf<VersionParser>(_versionParser).tryParse(stdOut)
                will(returnValue(versionStr))
            }
        })

        val dotnetCliToolInfo = createInstance()

        // When
        val version = dotnetCliToolInfo.getVersion(workingDirectoryPath)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(version, Version(1, 0, 1))
    }

    private fun createInstance() =
            DotnetCliToolInfoImpl(
                    _toolProvider,
                    _commandLineExecutor,
                    _versionParser)
}