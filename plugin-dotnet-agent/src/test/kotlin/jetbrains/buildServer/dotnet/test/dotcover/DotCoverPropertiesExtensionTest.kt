package jetbrains.buildServer.dotnet.test.dotcover

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.WorkflowComposer
import jetbrains.buildServer.dotcover.DotCoverPropertiesExtension
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.Verbosity
import jetbrains.buildServer.rx.Observable
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.rx.subjectOf
import jetbrains.buildServer.util.OSType
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverPropertiesExtensionTest {
    @MockK private lateinit var _agentLifeCycleEventSources: AgentLifeCycleEventSources
    @MockK private lateinit var _beforeAgentConfigurationLoadedEvent: Observable<AgentLifeCycleEventSources.BeforeAgentConfigurationLoadedEvent>
    @MockK private lateinit var _environment: Environment
    @MockK private lateinit var _buildAgent: BuildAgent
    @MockK private lateinit var _buildAgentConfiguration: BuildAgentConfiguration
    private val _beforeAgentConfigurationLoadedSubject = subjectOf<AgentLifeCycleEventSources.BeforeAgentConfigurationLoadedEvent>()

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _agentLifeCycleEventSources.beforeAgentConfigurationLoadedSource } returns _beforeAgentConfigurationLoadedEvent
        every { _beforeAgentConfigurationLoadedEvent.subscribe(any()) } answers {
            _beforeAgentConfigurationLoadedSubject.subscribe(arg<Observer<AgentLifeCycleEventSources.BeforeAgentConfigurationLoadedEvent>>(0))
        }
        every { _buildAgent.configuration } returns _buildAgentConfiguration
    }

    @DataProvider(name = "testData")
    fun getShowDiagnosticCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(OSType.UNIX, mapOf("name" to "val"), mapOf(CoverageConstants.DOTCOVER_CROSS_PLATFORM_REQUIREMENT to OSType.UNIX.toString())),
                arrayOf(OSType.MAC, mapOf("name" to "val"), mapOf(CoverageConstants.DOTCOVER_CROSS_PLATFORM_REQUIREMENT to OSType.MAC.toString())),
                arrayOf(
                        OSType.WINDOWS,
                        mapOf("DotNetFramework4.8_x64" to "4.8"),
                        mapOf("${CoverageConstants.DOTCOVER_CROSS_PLATFORM_REQUIREMENT}_DotNetFramework4.8_x64" to OSType.WINDOWS.toString())),
                arrayOf(
                        OSType.WINDOWS,
                        mapOf("DotNetFramework4.9_x64" to "4.9"),
                        mapOf("${CoverageConstants.DOTCOVER_CROSS_PLATFORM_REQUIREMENT}_DotNetFramework4.9_x64" to OSType.WINDOWS.toString())),
                arrayOf(
                        OSType.WINDOWS,
                        mapOf("DotNetFramework4.6_x64" to "4.6", "DotNetFramework4.8_x64" to "4.8"),
                        mapOf("${CoverageConstants.DOTCOVER_CROSS_PLATFORM_REQUIREMENT}_DotNetFramework4.8_x64" to OSType.WINDOWS.toString())),
                arrayOf(
                        OSType.WINDOWS,
                        mapOf("DotNetFramework4.6.1_x86" to "4.6.1"),
                        mapOf("${CoverageConstants.DOTCOVER_CROSS_PLATFORM_REQUIREMENT}_DotNetFramework4.6.1_x86" to OSType.WINDOWS.toString())),
                arrayOf(
                        OSType.WINDOWS,
                        emptyMap<String, String>(),
                        emptyMap<String, String>()),
                arrayOf(
                        OSType.WINDOWS,
                        mapOf("DotNetFramework4.6_x64" to "4.6"),
                        emptyMap<String, String>()))
    }

    @Test(dataProvider = "testData")
    fun shouldNotPublishServiceMessageWhenWorkflowFailed(os: OSType, configurationParameters: Map<String, String>, generatedConfigurationParameters: Map<String, String>) {
        // Given
        val extension = createInstance()

        // When
        every { _environment.os } returns os
        every { _buildAgentConfiguration.configurationParameters } returns configurationParameters
        every { _buildAgentConfiguration.addConfigurationParameter(any(), any()) } returns Unit

        _beforeAgentConfigurationLoadedSubject.onNext(AgentLifeCycleEventSources.BeforeAgentConfigurationLoadedEvent(_buildAgent))

        // Then
        for (generatedConfigurationParameter in generatedConfigurationParameters) {
            verify { _buildAgentConfiguration.addConfigurationParameter(generatedConfigurationParameter.key, generatedConfigurationParameter.value) }
        }
    }

    private fun createInstance() =
        DotCoverPropertiesExtension(_agentLifeCycleEventSources, _environment)
}