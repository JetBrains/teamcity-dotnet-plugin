

package jetbrains.buildServer.dotnet.test.dotnet.commands.nuget

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.commands.nuget.NugetCredentialProviderSelectorImpl
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.discovery.dotnetRuntime.DotnetRuntime
import jetbrains.buildServer.dotnet.discovery.dotnetRuntime.DotnetRuntimesProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class NugetCredentialProviderSelectorTest {
    @MockK
    private lateinit var _parametersService: ParametersService

    @MockK
    private lateinit var _virtualContext: VirtualContext

    @MockK
    private lateinit var _runtimesProvider: DotnetRuntimesProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun cases(): Array<Array<out Any?>> {
        return arrayOf(
            // Should not select when disabled
            arrayOf(
                Version(3, 1, 100),
                sequenceOf(Version(3, 1, 100)),
                mapOf(
                    "teamcity.nuget.credentialprovider.disabled" to "true",
                    "DotNetCredentialProvider1.0.0_Path" to "CredentialProvider1.dll",
                    "DotNetCredentialProvider2.0.0_Path" to "CredentialProvider2.dll",
                    "DotNetCredentialProvider3.0.0_Path" to "CredentialProvider3.dll",
                    "DotNetCredentialProvider4.0.0_Path" to "CredentialProvider.exe",
                    "DotNetCredentialProvider5.0.0_Path" to "CredentialProvider5.dll",
                    "DotNetCredentialProvider6.0.0_Path" to "CredentialProvider6.dll"
                ),
                false,
                null
            ),

            // Should select .NET Framework credentials provider when sdkVersion is Version.Empty
            arrayOf(
                Version.Empty,
                sequenceOf(Version(3, 1, 100)),
                mapOf(
                    "teamcity.nuget.credentialprovider.disabled" to null,
                    "DotNetCredentialProvider1.0.0_Path" to "CredentialProvider1.dll",
                    "DotNetCredentialProvider2.0.0_Path" to "CredentialProvider2.dll",
                    "DotNetCredentialProvider3.0.0_Path" to "CredentialProvider3.dll",
                    "DotNetCredentialProvider4.0.0_Path" to "CredentialProvider.exe",
                    "DotNetCredentialProvider5.0.0_Path" to "CredentialProvider5.dll",
                    "DotNetCredentialProvider6.0.0_Path" to "CredentialProvider6.dll"
                ),
                false,
                "CredentialProvider.exe"
            ),

            // Simple case - should select by given SDK version
            arrayOf(
                Version(3, 1, 200),
                emptySequence<Version>(),
                mapOf(
                    "teamcity.nuget.credentialprovider.disabled" to null,
                    "DotNetCredentialProvider1.0.0_Path" to "CredentialProvider1.dll",
                    "DotNetCredentialProvider2.0.0_Path" to "CredentialProvider2.dll",
                    "DotNetCredentialProvider3.0.0_Path" to "CredentialProvider3.dll",
                    "DotNetCredentialProvider4.0.0_Path" to "CredentialProvider.exe",
                    "DotNetCredentialProvider5.0.0_Path" to "CredentialProvider5.dll",
                    "DotNetCredentialProvider6.0.0_Path" to "CredentialProvider6.dll"
                ),
                false,
                "CredentialProvider3.dll"
            ),

            // Should not select when there's no corresponding credential providers and runtimes
            arrayOf(
                Version(3, 1, 200),
                emptySequence<Version>(),
                mapOf(
                    "teamcity.nuget.credentialprovider.disabled" to null,
                    "DotNetCredentialProvider3.0.0_Path" to null
                ),
                false,
                null
            ),

            // Should select .NET 5
            arrayOf(
                Version(5, 1, 100),
                emptySequence<Version>(),
                mapOf(
                    "teamcity.nuget.credentialprovider.disabled" to "FalsE",
                    "DotNetCredentialProvider5.0.0_Path" to "CredentialProvider5.dll"
                ),
                false,
                "CredentialProvider5.dll"
            ),

            // Should not select when given SDK version is less than CredentialProviderVersion
            arrayOf(
                Version(2, 1, 399),
                sequenceOf(Version(2, 1, 399)),
                mapOf(
                    "teamcity.nuget.credentialprovider.disabled" to null,
                    "DotNetCredentialProvider1.0.0_Path" to "CredentialProvider1.dll",
                    "DotNetCredentialProvider2.0.0_Path" to "CredentialProvider2.dll",
                    "DotNetCredentialProvider3.0.0_Path" to "CredentialProvider3.dll",
                    "DotNetCredentialProvider4.0.0_Path" to "CredentialProvider.exe",
                    "DotNetCredentialProvider5.0.0_Path" to "CredentialProvider5.dll",
                    "DotNetCredentialProvider6.0.0_Path" to "CredentialProvider6.dll"
                ),
                false,
                null
            ),

            // Should select credential provider according to available runtimes when failed by SDK version
            arrayOf(
                Version(5, 1, 100, "preview"),
                sequenceOf(Version(3, 1, 200)),
                mapOf(
                    "teamcity.nuget.credentialprovider.disabled" to null,
                    "DotNetCredentialProvider1.0.0_Path" to "CredentialProvider1.dll",
                    "DotNetCredentialProvider2.0.0_Path" to "CredentialProvider2.dll",
                    "DotNetCredentialProvider3.0.0_Path" to "CredentialProvider3.dll",
                    "DotNetCredentialProvider4.0.0_Path" to "CredentialProvider.exe",
                    "DotNetCredentialProvider5.0.0_Path" to null
                ),
                false,
                "CredentialProvider3.dll"
            ),

            // Should select the greatest credential provider version when multiple runtimes correspond
            arrayOf(
                Version(5, 1, 100, "preview"),
                sequenceOf(Version(1, 2, 200), Version(2, 1, 300)),
                mapOf(
                    "teamcity.nuget.credentialprovider.disabled" to null,
                    "DotNetCredentialProvider1.0.0_Path" to "CredentialProvider1.dll",
                    "DotNetCredentialProvider2.0.0_Path" to "CredentialProvider2.dll",
                    "DotNetCredentialProvider3.0.0_Path" to "CredentialProvider3.dll",
                    "DotNetCredentialProvider4.0.0_Path" to "CredentialProvider.exe",
                    "DotNetCredentialProvider5.0.0_Path" to null
                ),
                false,
                "CredentialProvider2.dll"
            ),

            // Should not select credential provider by available runtimes when in virtual context
            arrayOf(
                Version(3, 1, 100, "preview"),
                sequenceOf(Version(5, 1, 100)),
                mapOf(
                    "teamcity.nuget.credentialprovider.disabled" to null,
                    "DotNetCredentialProvider3.0.0_Path" to null,
                    "DotNetCredentialProvider4.0.0_Path" to "CredentialProvider.exe",
                    "DotNetCredentialProvider5.0.0_Path" to "CredentialProvider5.dll",
                    "DotNetCredentialProvider6.0.0_Path" to null
                ),
                true,
                null
            ),

            // Should select credential provider if it can be roll forwarded to the sdk when in virtual context
            arrayOf(
                Version(6, 1, 100, "preview"),
                sequenceOf(Version(5, 1, 100)),
                mapOf(
                    "teamcity.nuget.credentialprovider.disabled" to null,
                    "DotNetCredentialProvider4.0.0_Path" to "CredentialProvider.exe",
                    "DotNetCredentialProvider5.0.0_Path" to "CredentialProvider5.dll",
                    "DotNetCredentialProvider6.0.0_Path" to null
                ),
                true,
                "v_CredentialProvider5.dll"
            ),

            // Should not select credential provider by available runtimes when .NET Framework
            arrayOf(
                Version.Empty,
                sequenceOf(Version(3, 1, 100)),
                mapOf(
                    "teamcity.nuget.credentialprovider.disabled" to null,
                    "DotNetCredentialProvider1.0.0_Path" to "CredentialProvider1.dll",
                    "DotNetCredentialProvider2.0.0_Path" to "CredentialProvider2.dll",
                    "DotNetCredentialProvider3.0.0_Path" to "CredentialProvider3.dll",
                    "DotNetCredentialProvider4.0.0_Path" to null,
                    "DotNetCredentialProvider5.0.0_Path" to null
                ),
                false,
                null
            ),

            // Should resolve path in virtual context
            arrayOf(
                Version(5, 1, 200),
                emptySequence<Version>(),
                mapOf(
                    "teamcity.nuget.credentialprovider.disabled" to null,
                    "DotNetCredentialProvider5.0.0_Path" to "CredentialProvider5.dll"
                ),
                true,
                "v_CredentialProvider5.dll"
            ),

            // Should select greatest plugin version with available roll-forward runtime when there's no matching runtime
            arrayOf(
                Version(8, 0, 100, "preview"),
                sequenceOf(Version(7, 1, 300)),
                mapOf(
                    "teamcity.nuget.credentialprovider.disabled" to null,
                    "DotNetCredentialProvider1.0.0_Path" to "CredentialProvider1.dll",
                    "DotNetCredentialProvider2.0.0_Path" to "CredentialProvider2.dll",
                    "DotNetCredentialProvider3.0.0_Path" to "CredentialProvider3.dll",
                    "DotNetCredentialProvider4.0.0_Path" to "CredentialProvider.exe",
                    "DotNetCredentialProvider5.0.0_Path" to "CredentialProvider5.dll",
                    "DotNetCredentialProvider6.0.0_Path" to "CredentialProvider6.dll",
                    "DotNetCredentialProvider7.0.0_Path" to null,
                    "DotNetCredentialProvider8.0.0_Path" to null
                ),
                false,
                "CredentialProvider6.dll"
            ),

            // Should not roll-forward plugin when there's matching runtime
            arrayOf(
                Version(8, 0, 100, "preview"),
                sequenceOf(Version(5, 2, 200), Version(7, 1, 300)),
                mapOf(
                    "teamcity.nuget.credentialprovider.disabled" to null,
                    "DotNetCredentialProvider1.0.0_Path" to "CredentialProvider1.dll",
                    "DotNetCredentialProvider2.0.0_Path" to "CredentialProvider2.dll",
                    "DotNetCredentialProvider3.0.0_Path" to "CredentialProvider3.dll",
                    "DotNetCredentialProvider4.0.0_Path" to "CredentialProvider.exe",
                    "DotNetCredentialProvider5.0.0_Path" to "CredentialProvider5.dll",
                    "DotNetCredentialProvider6.0.0_Path" to "CredentialProvider6.dll",
                    "DotNetCredentialProvider7.0.0_Path" to null,
                    "DotNetCredentialProvider8.0.0_Path" to null
                ),
                false,
                "CredentialProvider5.dll"
            )
        )
    }

    @Test(dataProvider = "cases")
    fun shouldSelectCredentialProvider(
        sdkVersion: Version,
        runtimes: Sequence<Version>,
        params: Map<String, String?>,
        isVirtual: Boolean,
        expectedCredentialProvider: String?
    ) {
        // Given
        val selector = createInstance()

        // When
        for ((key, value) in params) {
            every { _parametersService.tryGetParameter(ParameterType.Configuration, key) } returns value
        }

        every { _parametersService.getParameterNames(ParameterType.Configuration) } returns params.keys.asSequence()
        every { _runtimesProvider.getRuntimes() } returns runtimes.map { DotnetRuntime(File("."), it, "") }
        every { _virtualContext.isVirtual } returns isVirtual
        every { _virtualContext.resolvePath(any()) } answers { (if (isVirtual) "v_" else "") + arg<String>(0) }

        val actualCredentialProvider = selector.trySelect(sdkVersion)

        // Then
        Assert.assertEquals(actualCredentialProvider, expectedCredentialProvider)
    }

    private fun createInstance() = NugetCredentialProviderSelectorImpl(_parametersService, _runtimesProvider, _virtualContext)
}