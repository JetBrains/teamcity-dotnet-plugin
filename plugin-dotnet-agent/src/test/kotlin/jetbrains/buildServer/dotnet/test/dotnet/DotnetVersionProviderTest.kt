

package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver
import jetbrains.buildServer.dotnet.DotnetVersionProviderImpl
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetVersionProviderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _commandLineExecutor: CommandLineExecutor
    private lateinit var _versionParser: ToolVersionOutputParser
    private lateinit var _dotnetToolResolver: DotnetToolResolver
    private lateinit var _buildStepContext: BuildStepContext

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _commandLineExecutor = _ctx.mock(CommandLineExecutor::class.java)
        _versionParser = _ctx.mock(ToolVersionOutputParser::class.java)
        _dotnetToolResolver = _ctx.mock(DotnetToolResolver::class.java)
        _buildStepContext = _ctx.mock(BuildStepContext::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(listOf("3.0.100-preview9-014004"), emptyList<String>(), 0, Version(2, 2, 202)),
                arrayOf(listOf("3.0.100-preview9-014004"), emptyList<String>(), 1, Version.Empty),
                arrayOf(listOf("3.0.100-preview9-014004"), emptyList<String>(), -2, Version.Empty),
                arrayOf(listOf("3.0.100-preview9-014004"), listOf("some error"), 0, Version.Empty),
                arrayOf(emptyList<String>(), listOf("some error"), 0, Version.Empty),
                arrayOf(emptyList<String>(), listOf("some error"), 1, Version.Empty)
                )
    }

    @Test(dataProvider = "testData")
    fun shouldGetDotnetVersion(stdOutVersion: Collection<String>, stdErr: Collection<String>, exitCode: Int, expectedVersion: Version) {
        // Given
        val workingDirectoryPath = Path("wd")
        val toolPath = Path("dotnet")
        val versionCommandline = CommandLine(
                null,
                TargetType.Tool,
                toolPath,
                workingDirectoryPath,
                DotnetVersionProviderImpl.versionArgs,
                emptyList())

        val getVersionResult = CommandLineResult(exitCode, stdOutVersion, stdErr)
        _ctx.checking(object : Expectations() {
            init {
                oneOf(_commandLineExecutor).tryExecute(versionCommandline)
                will(returnValue(getVersionResult))

                allowing(_versionParser).parse(stdOutVersion)
                will(returnValue(Version(2, 2, 202)))
            }
        })

        val fileSystemService = VirtualFileSystemService()
        val dotnetVersionProvider = createInstance(fileSystemService)

        // When
        val actualVersion = dotnetVersionProvider.getVersion(toolPath, workingDirectoryPath)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualVersion, expectedVersion)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            DotnetVersionProviderImpl(
                    _buildStepContext,
                    _commandLineExecutor,
                    _versionParser,
                    fileSystemService,
                    _dotnetToolResolver)
}