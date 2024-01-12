

package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdk
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdksProvider
import jetbrains.buildServer.dotnet.DotnetToolProvider
import jetbrains.buildServer.dotnet.test.agent.ToolSearchServiceStub
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetToolProviderTest {
    @MockK private lateinit var _toolProvidersRegistry: ToolProvidersRegistry
    @MockK private lateinit var _toolEnvironment: ToolEnvironment
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _dotnetSdksProvider: DotnetSdksProvider
    private val Sdk1 = DotnetSdk(File("sdk1"), Version(1, 2))
    private val Sdk2 = DotnetSdk(File("sdk2"), Version(2, 2))
    private val Sdk3 = DotnetSdk(File("sdk3"), Version(3, 2))

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun supportToolCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("dotnet", true),
                arrayOf("  dotnet  ", false),
                arrayOf("DoTnet", true),
                arrayOf("DOTNET", true),
                arrayOf("dotnet.cli", false),
                arrayOf("DOTNET2.cli", false),
                arrayOf("abc", false),
                arrayOf("   ", false),
                arrayOf("", false),
                arrayOf("dotnet.exe", false))
    }

    @Test(dataProvider = "supportToolCases")
    fun shouldSupportTool(toolName: String, expectedResult: Boolean) {
        // Given
        every { _toolProvidersRegistry.registerToolProvider(any()) } returns Unit
        val toolProvider = createInstance(emptySequence())

        // When
        val actualResult = toolProvider.supports(toolName)

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }

    @DataProvider
    fun providePathCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        mapOf(
                                File("dotnet") to sequenceOf(Sdk1)
                        ),
                        File("dotnet")
                ),

                arrayOf(
                        mapOf(
                                File("dotnet") to sequenceOf(Sdk1),
                                File("dotnet2") to sequenceOf(Sdk2)
                        ),
                        File("dotnet")
                ),

                arrayOf(
                        mapOf(
                                File("dotnet") to sequenceOf(),
                                File("dotnet2") to sequenceOf(Sdk2)
                        ),
                        File("dotnet2")
                ),
        )
    }

    @Test(dataProvider = "providePathCases")
    fun shouldProvidePath(sdks: Map<File, Sequence<DotnetSdk>>, expectedExecutable: File) {
        // Given
        every { _toolProvidersRegistry.registerToolProvider(any()) } returns Unit
        every { _toolEnvironment.homePaths } returns sequenceOf(Path("home"))
        every { _toolEnvironment.defaultPaths } returns sequenceOf(Path("default"))
        every { _toolEnvironment.environmentPaths } returns sequenceOf(Path("env"))

        for (sdk in sdks) {
            every { _dotnetSdksProvider.getSdks(sdk.key) } returns sdk.value
        }
        val toolProvider = createInstance(sdks.keys.asSequence().map { Path(it.path) })

        // When
        val actualExecutable = toolProvider.getPath(DotnetConstants.RUNNER_TYPE)

        // Then
        Assert.assertEquals(actualExecutable, expectedExecutable.absolutePath)
    }

    @Test
    fun shouldNotProvidePathForUnknownTool() {
        // Given
        every { _toolProvidersRegistry.registerToolProvider(any()) } returns Unit
        val toolProvider = createInstance(emptySequence())

        // When
        var actualResult: String? = null
        try {
            actualResult = toolProvider.getPath("Abc")
        }
        catch (ex: ToolCannotBeFoundException) { }

        // Then
        Assert.assertEquals(actualResult, null)
    }

    private fun createInstance(paths: Sequence<Path>): DotnetToolProvider =
            DotnetToolProvider(
                    _toolProvidersRegistry,
                    ToolSearchServiceStub(paths),
                    _toolEnvironment,
                    _dotnetSdksProvider)
}