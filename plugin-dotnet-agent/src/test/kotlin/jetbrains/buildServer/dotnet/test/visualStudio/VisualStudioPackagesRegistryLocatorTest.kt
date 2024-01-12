

package jetbrains.buildServer.dotnet.test.visualStudio

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.discovery.msbuild.MSBuildValidator
import jetbrains.buildServer.visualStudio.VisualStudioPackagesRegistryLocator
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class VisualStudioPackagesRegistryLocatorTest {
    @MockK private lateinit var _windowsRegistry: WindowsRegistry
    @MockK private lateinit var _msuildValidator: MSBuildValidator

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldProvideVisualStudioPath() {
        // Given
        val rootKey = WindowsRegistryKey.create(
                WindowsRegistryBitness.Bitness64,
                WindowsRegistryHive.LOCAL_MACHINE,
                "SOFTWARE",
                "Microsoft",
                "VisualStudio",
                "Setup")

        // When
        val regItems = mutableListOf<Any>(
                rootKey,
                rootKey + "12.0",
                WindowsRegistryValue(rootKey + "12.0" + "Abc", WindowsRegistryValueType.Str, "Abc"),
                rootKey + "13.0",
                WindowsRegistryValue(rootKey + "CachePath", WindowsRegistryValueType.Str, "vsPath"),
                WindowsRegistryValue(rootKey + "CachePath", WindowsRegistryValueType.Text, "vsPath2"),
                WindowsRegistryValue(rootKey + "CachePath", WindowsRegistryValueType.ExpandText, "vsPath3"),
                WindowsRegistryValue(rootKey + "CachePath", WindowsRegistryValueType.Long, "0x10")
        )

        every { _windowsRegistry.accept(any<WindowsRegistryKey>(), any<WindowsRegistryVisitor>(), false) } answers  {
            val visitor = arg<WindowsRegistryVisitor>(1)
            for (item in regItems) {
                when (item) {
                    is WindowsRegistryValue -> visitor.visit(item)
                    is WindowsRegistryKey -> visitor.visit(item)
                }
            }
            regItems.clear()
            value
        }

        val locator = createInstance()
        val actualPath = locator.tryGetPackagesPath()

        // Then
        Assert.assertEquals(actualPath, "vsPath")
    }

    private fun createInstance() =
            VisualStudioPackagesRegistryLocator(_windowsRegistry)
}