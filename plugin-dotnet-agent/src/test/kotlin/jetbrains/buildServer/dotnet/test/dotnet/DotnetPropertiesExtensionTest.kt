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
    private lateinit var _dotnetVersionProvider: DotnetVersionProvider
    private lateinit var _dotnetSdksProvider: DotnetSdksProvider
    private lateinit var _toolProvider: ToolProvider
    private lateinit var _buildAgent: BuildAgent
    private lateinit var _buildAgentConfiguration: BuildAgentConfiguration

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _agentLifeCycleEventSources = _ctx.mock(AgentLifeCycleEventSources::class.java)
        _pathsService = _ctx.mock(PathsService::class.java)
        _dotnetVersionProvider = _ctx.mock(DotnetVersionProvider::class.java)
        _dotnetSdksProvider = _ctx.mock(DotnetSdksProvider::class.java)
        _toolProvider = _ctx.mock(ToolProvider::class.java)
        _buildAgent = _ctx.mock(BuildAgent::class.java)
        _buildAgentConfiguration = _ctx.mock(BuildAgentConfiguration::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<Sequence<Any>>> {
        return arrayOf(
                arrayOf(
                        emptySequence(),
                        emptySequence()),

                arrayOf(
                        sequenceOf(
                                DotnetSdk(File("1.0.0"), Version(1, 0, 0))),
                        sequenceOf(
                                "1.0" to File("1.0.0").absolutePath,
                                "1.0.0" to File("1.0.0").absolutePath)),

                // Select newest version as default for group by Version(x, y)
                arrayOf(
                        sequenceOf(
                                DotnetSdk(File("1.1.100"), Version(1, 1, 100)),
                                DotnetSdk(File("1.1.300"), Version(1, 1, 300)),
                                DotnetSdk(File("1.1.1"), Version(1, 1, 1))),
                        sequenceOf(
                                "1.1" to File("1.1.300").absolutePath,
                                "1.1.1" to File("1.1.1").absolutePath,
                                "1.1.100" to File("1.1.100").absolutePath,
                                "1.1.300" to File("1.1.300").absolutePath)),

                // Display preview versions
                arrayOf(
                        sequenceOf(
                                DotnetSdk(File("1.1.100"), Version(1, 1, 100)),
                                DotnetSdk(File("1.1.300-preview"), Version.parse("1.1.300-preview"))),
                        sequenceOf(
                                "1.1" to File("1.1.300-preview").absolutePath,
                                "1.1.100" to File("1.1.100").absolutePath,
                                "1.1.300-preview" to File("1.1.300-preview").absolutePath))
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideConfigParams(
            originSdks: Sequence<DotnetSdk>,
            expectedSdks: Sequence<Pair<String, String>>) {
        // Given
        val toolPath = Path("dotnet")
        val workPath = Path("work")

        val beforeAgentConfigurationLoadedSource = subjectOf<AgentLifeCycleEventSources.BeforeAgentConfigurationLoadedEvent>()
        _ctx.checking(object : Expectations() {
            init {
                oneOf<AgentLifeCycleEventSources>(_agentLifeCycleEventSources).beforeAgentConfigurationLoadedSource
                will(returnValue(beforeAgentConfigurationLoadedSource))

                oneOf<ToolProvider>(_toolProvider).getPath(DotnetConstants.EXECUTABLE)
                will(returnValue(toolPath.path))

                oneOf<PathsService>(_pathsService).getPath(PathType.Work)
                will(returnValue(File(workPath.path)))

                oneOf<DotnetVersionProvider>(_dotnetVersionProvider).getVersion(toolPath, workPath)
                will(returnValue(Version(1, 0, 1)))

                oneOf<DotnetSdksProvider>(_dotnetSdksProvider).getSdks(File(toolPath.path))
                will(returnValue(originSdks))

                oneOf<BuildAgent>(_buildAgent).configuration
                will(returnValue(_buildAgentConfiguration))

                oneOf<BuildAgentConfiguration>(_buildAgentConfiguration).addConfigurationParameter(DotnetConstants.CONFIG_NAME, Version(1, 0, 1).toString())
                oneOf<BuildAgentConfiguration>(_buildAgentConfiguration).addConfigurationParameter(DotnetConstants.CONFIG_PATH, File(toolPath.path).canonicalPath)

                for ((version, path) in expectedSdks) {
                    oneOf<BuildAgentConfiguration>(_buildAgentConfiguration).addConfigurationParameter("${DotnetConstants.CONFIG_SDK_NAME}$version${DotnetConstants.PATH_SUFFIX}", path)
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
                    _dotnetVersionProvider,
                    _dotnetSdksProvider,
                    _pathsService)
}