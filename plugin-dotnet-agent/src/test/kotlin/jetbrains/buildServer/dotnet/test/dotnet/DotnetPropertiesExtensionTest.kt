package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
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
    private lateinit var _pathsService: PathsService
    private lateinit var _dotnetCliToolInfo: DotnetCliToolInfo
    private lateinit var _toolProvider: ToolProvider
    private lateinit var _buildAgent: BuildAgent
    private lateinit var _buildAgentConfiguration: BuildAgentConfiguration
    private lateinit var _fileSystemService: FileSystemService

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _agentLifeCycleEventSources = _ctx.mock(AgentLifeCycleEventSources::class.java)
        _pathsService = _ctx.mock(PathsService::class.java)
        _dotnetCliToolInfo = _ctx.mock(DotnetCliToolInfo::class.java)
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
        val version101 = Version(1, 0, 1)
        val workPath = File("work")

        val beforeAgentConfigurationLoadedSource = subjectOf<AgentLifeCycleEventSources.BeforeAgentConfigurationLoadedEvent>()
        _ctx.checking(object : Expectations() {
            init {
                oneOf<AgentLifeCycleEventSources>(_agentLifeCycleEventSources).beforeAgentConfigurationLoadedSource
                will(returnValue(beforeAgentConfigurationLoadedSource))

                oneOf<ToolProvider>(_toolProvider).getPath(DotnetConstants.EXECUTABLE)
                will(returnValue(toolPath.path))

                oneOf<PathsService>(_pathsService).getPath(PathType.Work)
                will(returnValue(workPath))

                oneOf<DotnetCliToolInfo>(_dotnetCliToolInfo).getVersion(toolPath, workPath)
                will(returnValue(version101))

                oneOf<BuildAgent>(_buildAgent).configuration
                will(returnValue(_buildAgentConfiguration))

                oneOf<BuildAgentConfiguration>(_buildAgentConfiguration).addConfigurationParameter(DotnetConstants.CONFIG_NAME, version101.toString())
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
                    _dotnetCliToolInfo,
                    _pathsService,
                    _fileSystemService)
}