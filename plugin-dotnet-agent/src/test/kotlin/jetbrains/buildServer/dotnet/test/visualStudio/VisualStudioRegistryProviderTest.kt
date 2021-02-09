package jetbrains.buildServer.dotnet.test.visualStudio

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceFactory
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.visualStudio.VisualStudioRegistryProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.testng.Assert
import org.testng.annotations.*
import java.io.File

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class VisualStudioRegistryProviderTest {
    @MockK private lateinit var _windowsRegistry: WindowsRegistry
    @MockK private lateinit var _visualStudioInstanceFactory: ToolInstanceFactory
    @MockK private lateinit var _visualStudioTestInstanceFactory: ToolInstanceFactory
    @MockK private lateinit var _msTestConsoleInstanceFactory: ToolInstanceFactory

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
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "10.0" + "InstallDir", WindowsRegistryValueType.Str, "path")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("path"), Version(10, 0), Version(10, 0), Platform.Default)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "10.0" + "InstallDir", WindowsRegistryValueType.Str, "path3")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "10.0" + "InstallDir2", WindowsRegistryValueType.Str, "path")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "10.0" + "InstallDir", WindowsRegistryValueType.Int, 101)
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "10a0" + "InstallDir", WindowsRegistryValueType.Str, "path")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "abc" + "InstallDir", WindowsRegistryValueType.Str, "path")
                        ),
                        emptySequence<ToolInstance>()
                ),
                // VSTest
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "QualityTools" + "InstallDir", WindowsRegistryValueType.Str, "path1")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.VisualStudioTest, File("path1"), Version(1, 2, 3), Version(1, 2, 3), Platform.Default)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "QualityTools" + "InstallDir", WindowsRegistryValueType.Str, "path3")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "QualityTools2" + "InstallDir", WindowsRegistryValueType.Str, "path1")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "QualityTools" + "InstallDir2", WindowsRegistryValueType.Str, "path1")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "QualityTools" + "InstallDir", WindowsRegistryValueType.Int, 11)
                        ),
                        emptySequence<ToolInstance>()
                ),
                // MSTest
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "QualityTools" + "InstallDir", WindowsRegistryValueType.Str, "path2")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.MSTest, File("path2"), Version(1, 2, 3), Version(1, 2, 3), Platform.Default)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "QualityToolsAaa" + "InstallDir", WindowsRegistryValueType.Str, "path2")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "QualityTools" + "Abc", WindowsRegistryValueType.Str, "path2")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "QualityTools" + "InstallDir", WindowsRegistryValueType.Int, 12)
                        ),
                        emptySequence<ToolInstance>()
                )
        )
    }

    @Test(dataProvider = "testDataValues")
    fun shouldProvideInstances(registryValues: Sequence<WindowsRegistryValue>, expectedInstances: Sequence<ToolInstance>) {
        // Given
        val instanceProvider = createInstance()

        // When
        every { _windowsRegistry.accept(VisualStudioRegistryProvider.RegKey, any(), false) } answers {
            val visitor = arg<WindowsRegistryVisitor>(1)
            for (registryValue in registryValues) {
                if (!visitor.visit(registryValue)) {
                    break
                }
            }
        }

        every { _visualStudioInstanceFactory.tryCreate(any(), any(), Platform.Default) } answers { ToolInstance(ToolInstanceType.VisualStudio, arg<File>(0), arg<Version>(1), arg<Version>(1), Platform.Default) }
        every { _visualStudioInstanceFactory.tryCreate(File("path3"), any(), Platform.Default) } returns null

        every { _visualStudioTestInstanceFactory.tryCreate(any(), Version.Empty, Platform.Default) } returns null
        every { _visualStudioTestInstanceFactory.tryCreate(File("path1"), Version.Empty, Platform.Default) } answers { ToolInstance(ToolInstanceType.VisualStudioTest, arg<File>(0), Version(1, 2, 3), Version(1, 2, 3), Platform.Default) }

        every { _msTestConsoleInstanceFactory.tryCreate(any(), Version.Empty, Platform.Default) } returns null
        every { _msTestConsoleInstanceFactory.tryCreate(File("path2"), Version.Empty, Platform.Default) } answers { ToolInstance(ToolInstanceType.MSTest, arg<File>(0), Version(1, 2, 3), Version(1, 2, 3), Platform.Default) }

        val actualInstances = instanceProvider.getInstances()

        // Then
        Assert.assertEquals(actualInstances.sortedBy { it.toString() }.toList(), expectedInstances.sortedBy { it.toString() }.toList())
    }

    @DataProvider
    fun testDataKeys(): Array<Array<Sequence<WindowsRegistryKey>>> {
        return arrayOf(
                arrayOf(
                        sequenceOf(
                                VisualStudioRegistryProvider.RegKey + "10.0"
                        ),
                        sequenceOf(
                                VisualStudioRegistryProvider.RegKey + "10.0",
                                VisualStudioRegistryProvider.RegKey + "10.0" + "EnterpriseTools" + "QualityTools"
                        )
                ),
                arrayOf(
                        sequenceOf(
                                VisualStudioRegistryProvider.RegKey + "10.1"
                        ),
                        sequenceOf(
                                VisualStudioRegistryProvider.RegKey + "10.1",
                                VisualStudioRegistryProvider.RegKey + "10.1" + "EnterpriseTools" + "QualityTools"
                        )
                ),
                arrayOf(
                        sequenceOf(
                                VisualStudioRegistryProvider.RegKey + "10.0.1"
                        ),
                        emptySequence()
                ),
                arrayOf(
                        sequenceOf(
                                VisualStudioRegistryProvider.RegKey + "10.0.1.2"
                        ),
                        emptySequence()
                ),
                arrayOf(
                        sequenceOf(
                                VisualStudioRegistryProvider.RegKey + "10"
                        ),
                        emptySequence()
                ),
                arrayOf(
                        sequenceOf(
                                VisualStudioRegistryProvider.RegKey + "abc"
                        ),
                        emptySequence()
                ),
                arrayOf(
                        sequenceOf(
                                VisualStudioRegistryProvider.RegKey + "10aa.1"
                        ),
                        emptySequence()
                )
        )
    }

    @Test(dataProvider = "testDataKeys")
    fun shouldIterateThroughRegKeysWithVersions(keys: Sequence<WindowsRegistryKey>, expectedKeys: Sequence<WindowsRegistryKey>) {
        // Given
        val instanceProvider = createInstance()
        val actualKeys = mutableListOf<WindowsRegistryKey>()

        // When
        every { _windowsRegistry.accept(any(), any(), false) } answers {
            val key = arg<WindowsRegistryKey>(0)
            if (key != VisualStudioRegistryProvider.RegKey) {
                actualKeys.add(key)
            } else {
                val visitor = arg<WindowsRegistryVisitor>(1)
                for (versionKey in keys) {
                    if (!visitor.visit(versionKey)) {
                        break
                    }
                }
            }
        }

        instanceProvider.getInstances()

        // Then
        Assert.assertEquals(actualKeys, expectedKeys.toList())
    }

    private fun createInstance() =
            VisualStudioRegistryProvider(
                    _windowsRegistry,
                    _visualStudioInstanceFactory,
                    _visualStudioTestInstanceFactory,
                    _msTestConsoleInstanceFactory)
}