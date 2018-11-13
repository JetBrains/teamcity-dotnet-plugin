package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
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
    private lateinit var _sdkPathProvider: SdkPathProvider


    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _commandLineExecutor = _ctx.mock(CommandLineExecutor::class.java)
        _versionParser = _ctx.mock(VersionParser::class.java)
        _sdkPathProvider = _ctx.mock(SdkPathProvider::class.java)
    }

    @Test
    fun shouldGetDotnetInfo() {
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
                oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(versionCommandline)
                will(returnValue(getVersionResult))

                oneOf<VersionParser>(_versionParser).tryParse(stdOut)
                will(returnValue(versionStr))

                oneOf<SdkPathProvider>(_sdkPathProvider).path
                will(returnValue(File("sdkRoot")))
            }
        })

        val fileSystemService = VirtualFileSystemService()
                .addDirectory(File(File("sdkRoot"), "1.2.3"))
                .addDirectory(File(File("sdkRoot"), "1.2.3-rc"))
                .addFile(File(File("sdkRoot"), "1.2.4"))
                .addDirectory(File(File("sdkRoot"), "nuget"))
                .addDirectory(File(File("sdkRoot"), "1.2.5"))
        val dotnetCliToolInfo = createInstance(fileSystemService)

        // When
        val actualInfo = dotnetCliToolInfo.getInfo(toolPath, workingDirectoryPath)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(
                actualInfo,
                DotnetInfo(
                        Version(1, 0, 1),
                        listOf(
                                DotnetSdk(File(File("sdkRoot"), "1.2.3"), Version(1, 2,3)),
                                DotnetSdk(File(File("sdkRoot"), "1.2.3-rc"), Version.parse("1.2.3-rc")),
                                DotnetSdk(File(File("sdkRoot"), "1.2.5"), Version(1, 2,5))
                        )
                ))
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            DotnetCliToolInfoImpl(
                    _commandLineExecutor,
                    _versionParser,
                    fileSystemService,
                    _sdkPathProvider)
}