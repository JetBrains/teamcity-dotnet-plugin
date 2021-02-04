package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceFactory
import jetbrains.buildServer.dotnet.DotnetFrameworkSdkRegistryProvider
import jetbrains.buildServer.dotnet.Platform
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetFrameworkSdkRegistryProviderTest {
    @MockK private lateinit var _windowsRegistry: WindowsRegistry
    @MockK private lateinit var _sdkInstanceFactory: ToolInstanceFactory

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testDataValues(): Array<Array<Sequence<Any>>> {
        return arrayOf(
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(DotnetFrameworkSdkRegistryProvider.RegKey + "sdkInstallRootv1.1", WindowsRegistryValueType.Str, "path")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path"), Version(1, 1), Version(1, 1), Platform.x86)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(DotnetFrameworkSdkRegistryProvider.RegKey + "sdkInstallRootv1.0", WindowsRegistryValueType.Str, "path")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path"), Version(1, 0), Version(1, 0), Platform.x86)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(DotnetFrameworkSdkRegistryProvider.RegKey + "sdkInstallRootv1.1", WindowsRegistryValueType.Str, "path3")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(DotnetFrameworkSdkRegistryProvider.RegKey + "sdkInstallRootv1.1.1", WindowsRegistryValueType.Str, "path")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(DotnetFrameworkSdkRegistryProvider.RegKey + "sdkInstallRootv1", WindowsRegistryValueType.Str, "path")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(DotnetFrameworkSdkRegistryProvider.RegKey + "sdkInstallRootv", WindowsRegistryValueType.Str, "path")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(DotnetFrameworkSdkRegistryProvider.RegKey + "sdkInstallRoot1.1", WindowsRegistryValueType.Str, "path")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(DotnetFrameworkSdkRegistryProvider.RegKey + "abc", WindowsRegistryValueType.Str, "path")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(DotnetFrameworkSdkRegistryProvider.RegKey + "sdkInstallRootv1.1", WindowsRegistryValueType.Str, " ")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(DotnetFrameworkSdkRegistryProvider.RegKey + "sdkInstallRootv1.1", WindowsRegistryValueType.Str, "")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(DotnetFrameworkSdkRegistryProvider.RegKey + "sdkInstallRootv1.1", WindowsRegistryValueType.Int, 99)
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        emptySequence<WindowsRegistryValue>(),
                        emptySequence<ToolInstanceType>()
                )
        )
    }

    @Test(dataProvider = "testDataValues")
    fun shouldProvideInstances(registryValues: Sequence<WindowsRegistryValue>, expectedInstances: Sequence<ToolInstance>) {
        // Given
        val instanceProvider = createInstance()

        // When
        every { _windowsRegistry.accept(DotnetFrameworkSdkRegistryProvider.RegKey, any(), false) } answers {
            val visitor = arg<WindowsRegistryVisitor>(1)
            for (registryValue in registryValues) {
                if (!visitor.visit(registryValue)) {
                    break
                }
            }
        }

        every { _sdkInstanceFactory.tryCreate(any(), any(), Platform.x86) } answers { ToolInstance(ToolInstanceType.DotNetFrameworkSDK, arg<File>(0), arg<Version>(1), arg<Version>(1), Platform.x86) }
        every { _sdkInstanceFactory.tryCreate(File("path3"), any(), Platform.x86) } returns null

        val actualInstances = instanceProvider.getInstances()

        // Then
        Assert.assertEquals(actualInstances.sortedBy { it.toString() }.toList(), expectedInstances.sortedBy { it.toString() }.toList())
    }

    private fun createInstance() =
            DotnetFrameworkSdkRegistryProvider(
                    _windowsRegistry,
                    _sdkInstanceFactory)
}