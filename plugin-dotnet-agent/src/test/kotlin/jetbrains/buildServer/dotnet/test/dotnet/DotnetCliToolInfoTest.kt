package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
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
    fun shouldGetDotnetInfoWhenSdksListIsNotSupported() {
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

    @DataProvider
    fun testData(): Array<Array<List<Any>>> {
        return arrayOf(
                arrayOf(
                        listOf("1.0.4 [${sdkPath}]", "2.1.403 [${sdkPath}]"),
                        listOf(
                                DotnetSdk(File(sdkPath, "1.0.4"), Version(1, 0, 4)),
                                DotnetSdk(File(sdkPath, "2.1.403"), Version(2, 1, 403))
                        )
                ),
                arrayOf(
                        emptyList<String>(),
                        emptyList<DotnetSdk>()
                ),
                arrayOf(
                        listOf("1.0.4 [${sdkPath}]", "2.1.403-rc [${sdkPath}]"),
                        listOf(
                                DotnetSdk(File(sdkPath, "1.0.4"), Version(1, 0, 4)),
                                DotnetSdk(File(sdkPath, "2.1.403-rc"), Version.parse("2.1.403-rc"))
                        )
                )
        )
    }

    @Test(dataProvider = "testData")
    fun shouldGetDotnetInfoWhenSdksListIsSupported(stdOutSdks: List<String>, expectedSdks: List<DotnetSdk>) {
        // Given
        val workingDirectoryPath = File("wd")
        val toolPath = File("dotnet")
        val versionCommandline = CommandLine(
                TargetType.Tool,
                toolPath,
                workingDirectoryPath,
                DotnetCliToolInfoImpl.versionArgs,
                emptyList())

        val sdksCommandline = CommandLine(
                TargetType.Tool,
                toolPath,
                workingDirectoryPath,
                DotnetCliToolInfoImpl.listSdksArgs,
                emptyList())

        val stdOutVersion = sequenceOf("stdOut")
        val stdErr = sequenceOf("stdErr")
        val getVersionResult = CommandLineResult(sequenceOf(0), stdOutVersion, stdErr)
        val getSdksResult = CommandLineResult(sequenceOf(0), stdOutSdks.asSequence(), stdErr)
        val versionStr = Version.VersionSupportingSdksList.toString()
        _ctx.checking(object : Expectations() {
            init {
                oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(versionCommandline)
                will(returnValue(getVersionResult))

                oneOf<VersionParser>(_versionParser).tryParse(stdOutVersion)
                will(returnValue(versionStr))

                oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(sdksCommandline)
                will(returnValue(getSdksResult))
            }
        })

        val fileSystemService = VirtualFileSystemService()
        val dotnetCliToolInfo = createInstance(fileSystemService)

        // When
        val actualInfo = dotnetCliToolInfo.getInfo(toolPath, workingDirectoryPath)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(
                actualInfo,
                DotnetInfo(
                        Version.VersionSupportingSdksList,
                        expectedSdks
                ))
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            DotnetCliToolInfoImpl(
                    _commandLineExecutor,
                    _versionParser,
                    fileSystemService,
                    _sdkPathProvider)

    companion object {
        private val sdkPath = File(File(File("Program Files"), "dotnet"), "sdk")
    }
}