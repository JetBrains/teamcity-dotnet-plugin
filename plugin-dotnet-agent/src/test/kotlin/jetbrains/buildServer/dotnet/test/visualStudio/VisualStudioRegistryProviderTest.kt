package jetbrains.buildServer.dotnet.test.visualStudio

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.visualStudio.VisualStudioInstance
import jetbrains.buildServer.visualStudio.VisualStudioInstanceFactory
import jetbrains.buildServer.visualStudio.VisualStudioRegistryProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VisualStudioRegistryProviderTest {
    @MockK private lateinit var _windowsRegistry: WindowsRegistry
    @MockK private lateinit var _visualStudioInstanceFactory: VisualStudioInstanceFactory

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
                                VisualStudioInstance(File("path"), Version(10, 0), Version(10, 0))
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "10.0" + "InstallDir", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "11.1" + "InstallDir", WindowsRegistryValueType.Str, "path2")
                        ),
                        sequenceOf(
                                VisualStudioInstance(File("path"), Version(10, 0), Version(10, 0))
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "10.0" + "InstallDir", WindowsRegistryValueType.Str, "path3")
                        ),
                        emptySequence<VisualStudioInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "10.0" + "InstallDir2", WindowsRegistryValueType.Str, "path")
                        ),
                        emptySequence<VisualStudioInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "10.0" + "InstallDir", WindowsRegistryValueType.Int, 101)
                        ),
                        emptySequence<VisualStudioInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "10a0" + "InstallDir", WindowsRegistryValueType.Str, "path")
                        ),
                        emptySequence<VisualStudioInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(VisualStudioRegistryProvider.RegKey + "abc" + "InstallDir", WindowsRegistryValueType.Str, "path")
                        ),
                        emptySequence<VisualStudioInstance>()
                )
        )
    }

    @Test(dataProvider = "testDataValues")
    fun shouldProvideInstances(registryValues: Sequence<WindowsRegistryValue>, expectedInstances: Sequence<VisualStudioInstance>) {
        // Given
        val instanceProvider = createInstance()

        // When
        every { _windowsRegistry.get(VisualStudioRegistryProvider.RegKey, any(), false) } answers {
            val visitor = arg<WindowsRegistryVisitor>(1)
            for (registryValue in registryValues) {
                if (!visitor.accept(registryValue)) {
                    break
                }
            }
        }

        every { _visualStudioInstanceFactory.tryCreate(any(), any()) } answers { VisualStudioInstance(arg<File>(0), arg<Version>(1), arg<Version>(1)) }
        every { _visualStudioInstanceFactory.tryCreate(File("path3"), any()) } returns null

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
                                VisualStudioRegistryProvider.RegKey + "10.0"
                        )
                ),
                arrayOf(
                        sequenceOf(
                                VisualStudioRegistryProvider.RegKey + "10.1"
                        ),
                        sequenceOf(
                                VisualStudioRegistryProvider.RegKey + "10.1"
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
        every { _windowsRegistry.get(any(), any(), false) } answers {
            val key = arg<WindowsRegistryKey>(0)
            if (key != VisualStudioRegistryProvider.RegKey) {
                actualKeys.add(key)
            } else {
                val visitor = arg<WindowsRegistryVisitor>(1)
                for (versionKey in keys) {
                    if (!visitor.accept(versionKey)) {
                        break
                    }
                }
            }
        }

        instanceProvider.getInstances().toList()

        // Then
        Assert.assertEquals(actualKeys, expectedKeys.toList())
    }

    private fun createInstance() =
            VisualStudioRegistryProvider(
                    _windowsRegistry,
                    _visualStudioInstanceFactory)
}