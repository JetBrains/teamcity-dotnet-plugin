package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetPropertiesExtension
import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.rx.subjectOf
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetPropertiesExtensionTest {
    private lateinit var _ctx: Mockery
    private lateinit var _agentLifeCycleEventSources: AgentLifeCycleEventSources
    private lateinit var _commandLineExecutor: CommandLineExecutor
    private lateinit var _versionParser: VersionParser
    private lateinit var _toolProvider: ToolProvider
    private lateinit var _buildAgent: BuildAgent
    private lateinit var _buildAgentConfiguration: BuildAgentConfiguration
    private lateinit var _fileSystemService: FileSystemService

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _agentLifeCycleEventSources = _ctx.mock(AgentLifeCycleEventSources::class.java)
        _commandLineExecutor = _ctx.mock(CommandLineExecutor::class.java)
        _versionParser = _ctx.mock(VersionParser::class.java)
        _toolProvider = _ctx.mock(ToolProvider::class.java)
        _buildAgent = _ctx.mock(BuildAgent::class.java)
        _buildAgentConfiguration = _ctx.mock(BuildAgentConfiguration::class.java)
        _fileSystemService = _ctx.mock(FileSystemService::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<Sequence<DotnetPropertiesExtension.Sdk>>> {
        return arrayOf(
                arrayOf(
                        emptySequence(),
                        emptySequence()),

                arrayOf(
                        sequenceOf(
                                DotnetPropertiesExtension.Sdk(File("1.0.0"), Version(1, 0, 0))),
                        sequenceOf(
                                DotnetPropertiesExtension.Sdk(File("1.0.0"), Version(1, 0)),
                                DotnetPropertiesExtension.Sdk(File("1.0.0"), Version(1, 0, 0)))),

                // Select newest version as default for group by Version(x, y)
                arrayOf(
                        sequenceOf(
                                DotnetPropertiesExtension.Sdk(File("1.1.100"), Version(1, 1, 100)),
                                DotnetPropertiesExtension.Sdk(File("1.1.300"), Version(1, 1, 300)),
                                DotnetPropertiesExtension.Sdk(File("1.1.1"), Version(1, 1, 1))),
                        sequenceOf(
                                DotnetPropertiesExtension.Sdk(File("1.1.300"), Version(1, 1)),
                                DotnetPropertiesExtension.Sdk(File("1.1.100"), Version(1, 1, 100)),
                                DotnetPropertiesExtension.Sdk(File("1.1.300"), Version(1, 1, 300)),
                                DotnetPropertiesExtension.Sdk(File("1.1.1"), Version(1, 1, 1))))
        )
    }

    @Test(dataProvider = "testData")
    fun shouldShutdownDotnetBuildServer(
            originSdks: Sequence<DotnetPropertiesExtension.Sdk>,
            expectedSdks: Sequence<DotnetPropertiesExtension.Sdk>) {
        // Given
        val toolPath = File("dotnet")
        val versionCommandline = CommandLine(
                TargetType.Tool,
                toolPath,
                File("."),
                DotnetPropertiesExtension.versionArgs,
                emptyList())

        val stdOut = sequenceOf("stdOut")
        val stdErr = sequenceOf("stdErr")
        val getVersionResult = CommandLineResult(sequenceOf(0), stdOut, stdErr)
        val versionStr = "1.0.1"

        val beforeAgentConfigurationLoadedSource = subjectOf<AgentLifeCycleEventSources.BeforeAgentConfigurationLoadedEvent>()
        _ctx.checking(object : Expectations() {
            init {
                oneOf<AgentLifeCycleEventSources>(_agentLifeCycleEventSources).beforeAgentConfigurationLoadedSource
                will(returnValue(beforeAgentConfigurationLoadedSource))

                oneOf<ToolProvider>(_toolProvider).getPath(DotnetConstants.EXECUTABLE)
                will(returnValue(toolPath.path))

                oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(versionCommandline)
                will(returnValue(getVersionResult))

                oneOf<VersionParser>(_versionParser).tryParse(stdOut)
                will(returnValue(versionStr))

                oneOf<BuildAgent>(_buildAgent).configuration
                will(returnValue(_buildAgentConfiguration))

                oneOf<BuildAgentConfiguration>(_buildAgentConfiguration).addConfigurationParameter(DotnetConstants.CONFIG_NAME, versionStr)
                oneOf<BuildAgentConfiguration>(_buildAgentConfiguration).addConfigurationParameter(DotnetConstants.CONFIG_PATH, toolPath.absolutePath)

                oneOf<FileSystemService>(_fileSystemService).list(File(toolPath.parentFile, "sdk"))
                will(returnValue(originSdks.map { it.path }))

                for ((path, _) in originSdks) {
                    oneOf<FileSystemService>(_fileSystemService).isDirectory(path)
                    will(returnValue(true))
                }

                for ((path, version) in expectedSdks) {
                    oneOf<BuildAgentConfiguration>(_buildAgentConfiguration).addConfigurationParameter("${DotnetConstants.CONFIG_SDK_NAME}$version${DotnetConstants.PATH_SUFFIX}", path.absolutePath)
                }
            }
        })

        createInstance()

        // When
        beforeAgentConfigurationLoadedSource.onNext(AgentLifeCycleEventSources.BeforeAgentConfigurationLoadedEvent(_buildAgent))

        // Then
        _ctx.assertIsSatisfied()
    }

    private fun createInstance() =
            DotnetPropertiesExtension(
                    _agentLifeCycleEventSources,
                    _toolProvider,
                    _commandLineExecutor,
                    _versionParser,
                    _fileSystemService)
}