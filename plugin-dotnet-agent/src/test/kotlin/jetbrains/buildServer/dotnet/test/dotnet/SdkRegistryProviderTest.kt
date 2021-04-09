package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceFactory
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.dotnet.SdkRegistryProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class SdkRegistryProviderTest {
    @MockK private lateinit var _windowsRegistry: WindowsRegistry
    @MockK private lateinit var _sdkInstanceFactory: ToolInstanceFactory
    private val _key = SdkRegistryProvider.RegKeys.first()

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testDataValues(): Array<Array<Sequence<Any>>> {
        return arrayOf(
                // .NET Framework SDK

                //HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Microsoft SDKs\Windows\v8.0A\WinSDK-NetFx35Tools-x86
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path"), Version.parse("8.0.50727"), Version(3, 5), Platform.x86)
                        )
                ),
                //HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Microsoft SDKs\NETFXSDK\4.7.1\WinSDK-NetFx40Tools-x64
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx40Tools-x64" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx40Tools-x64" + "ProductVersion", WindowsRegistryValueType.Str, "4.7.03081")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path"), Version.parse("4.7.03081"), Version.parse("4.0"), Platform.x64)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx50Tools-x64" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx50Tools-x64" + "ProductVersion", WindowsRegistryValueType.Str, "4.7.03081")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path"), Version.parse("4.7.03081"), Version.parse("5.0"), Platform.x64)
                        )
                ),arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7" + "WinSDK-NetFx40Tools-x64" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7" + "WinSDK-NetFx40Tools-x64" + "ProductVersion", WindowsRegistryValueType.Str, "4.7.03081"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")

                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path"), Version.parse("4.7.03081"), Version.parse("4.0"), Platform.x64),
                                ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path"), Version.parse("8.0.50727"), Version(3, 5), Platform.x86)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx40Tools-x64" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx40Tools-x64" + "ProductVersion", WindowsRegistryValueType.Str, "4.7.03082"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.1" + "WinSDK-NetFx40Tools-x64" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.1" + "WinSDK-NetFx40Tools-x64" + "ProductVersion", WindowsRegistryValueType.Str, "4.7.03081")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path"), Version.parse("4.7.03082"), Version.parse("4.0"), Platform.x64)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx40Tools-x64" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx40Tools-x64" + "ProductVersion", WindowsRegistryValueType.Str, "4.7.03082"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.1" + "WinSDK-NetFx40Tools-x86" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.1" + "WinSDK-NetFx40Tools-x86" + "ProductVersion", WindowsRegistryValueType.Str, "4.7.03081")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path"), Version.parse("4.7.03082"), Version.parse("4.0"), Platform.x64),
                                ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path"), Version.parse("4.7.03081"), Version.parse("4.0"), Platform.x86)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion", WindowsRegistryValueType.Str, "8.0..50727")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path"), Version.parse("8.0.50727"), Version(3, 5), Platform.x86)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "InstallationFolder", WindowsRegistryValueType.Str, "path")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "InstallationFolder", WindowsRegistryValueType.Int, 11),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion", WindowsRegistryValueType.Int, 12)
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx3Tools-x86" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFxTools-x86" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "InstallationFolder2", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion2", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "InstallationFolder", WindowsRegistryValueType.Str, "path")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8A" + "WinSDK-NetFx35Tools-x86" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0.2A" + "WinSDK-NetFx35Tools-x86" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0.2" + "WinSDK-NetFx35Tools-x86" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "8.0A" + "WinSDK-NetFx35Tools-x86" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "WinSDK-NetFx35Tools-x86" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2.1" + "WinSDK-NetFx40Tools-x64" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx40Tools-x64" + "ProductVersion", WindowsRegistryValueType.Str, "4.7.03081")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "NETFXSDK" + "4" + "WinSDK-NetFx40Tools-x64" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx40Tools-x64" + "ProductVersion", WindowsRegistryValueType.Str, "4.7.03081")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "NETFXSDK" + "4." + "WinSDK-NetFx40Tools-x64" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx40Tools-x64" + "ProductVersion", WindowsRegistryValueType.Str, "4.7.03081")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx40Tools2-x64" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx40Tools-x64" + "ProductVersion", WindowsRegistryValueType.Str, "4.7.03081")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx40Tools-x64" + "ProductVersion", WindowsRegistryValueType.Str, "4.7.03081")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "Abc-NetFx40Tools-x64" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx40Tools-x64" + "ProductVersion", WindowsRegistryValueType.Str, "4.7.03081")
                        ),
                        emptySequence<ToolInstance>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx40Tools-x64" + "InstallationFolder", WindowsRegistryValueType.Str, "path3"),
                                WindowsRegistryValue(_key + "NETFXSDK" + "4.7.2" + "WinSDK-NetFx40Tools-x64" + "ProductVersion", WindowsRegistryValueType.Str, "4.7.03081")
                        ),
                        emptySequence<ToolInstance>()
                ),

                // Windows SDK
                //HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Microsoft SDKs\Windows\v8.0A
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.WindowsSDK, File("path"), Version.parse("8.0.50727"), Version.parse("8.0-A"), Platform.Default)
                        )
                ),
                //HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Microsoft SDKs\Windows\v7.0
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v7.0" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v7.0" + "ProductVersion", WindowsRegistryValueType.Str, "7.0.7600.16385.40715")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.WindowsSDK, File("path"), Version.parse("7.0.7600.16385.40715"), Version.parse("7.0"), Platform.Default)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v10.0" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v10.0" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.WindowsSDK, File("path"), Version.parse("8.0.50727"), Version.parse("10.0"), Platform.Default)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50726")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.WindowsSDK, File("path"), Version.parse("8.0.50726"), Version.parse("8.0"), Platform.Default)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50726")
                        ),
                        sequenceOf(
                                ToolInstance(ToolInstanceType.WindowsSDK, File("path"), Version.parse("8.0.50727"), Version.parse("8.0-A"), Platform.Default),
                                ToolInstance(ToolInstanceType.WindowsSDK, File("path"), Version.parse("8.0.50726"), Version.parse("8.0"), Platform.Default)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows2" + "v8.0A" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows2" + "v8.0A" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "InstallationFolder2", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "ProductVersion2", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0.1A" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8A" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "8.0A" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "InstallationFolder", WindowsRegistryValueType.Str, ""),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "InstallationFolder", WindowsRegistryValueType.Str, "   "),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "InstallationFolder", WindowsRegistryValueType.Int, 11),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "InstallationFolder", WindowsRegistryValueType.Str, "path"),
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "ProductVersion", WindowsRegistryValueType.Int, 12)
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "ProductVersion", WindowsRegistryValueType.Str, "8.0.50727")
                        ),
                        emptySequence<ToolInstanceType>()
                ),
                arrayOf(
                        sequenceOf(
                                WindowsRegistryValue(_key + "Windows" + "v8.0A" + "InstallationFolder", WindowsRegistryValueType.Str, "path")
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
        every { _windowsRegistry.accept(any(), any(), true) } answers {
            val visitor = arg<WindowsRegistryVisitor>(1)
            for (registryValue in registryValues) {
                if (!visitor.visit(registryValue)) {
                    break
                }
            }
        }

        every { _sdkInstanceFactory.tryCreate(any(), any(), any()) } answers { ToolInstance(ToolInstanceType.DotNetFrameworkSDK, arg<File>(0), arg<Version>(1), arg<Version>(1), arg<Platform>(2)) }
        every { _sdkInstanceFactory.tryCreate(File("path3"), any(), any()) } returns null

        val actualInstances = instanceProvider.getInstances()

        // Then
        Assert.assertEquals(actualInstances.sortedBy { it.toString() }.toList(), expectedInstances.sortedBy { it.toString() }.toList())
    }

    private fun createInstance() =
            SdkRegistryProvider(
                    _windowsRegistry,
                    _sdkInstanceFactory)
}