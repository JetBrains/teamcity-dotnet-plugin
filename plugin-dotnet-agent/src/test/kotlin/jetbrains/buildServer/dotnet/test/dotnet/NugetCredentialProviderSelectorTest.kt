package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.NugetCredentialProviderSelectorImpl
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.DotnetRuntime
import jetbrains.buildServer.dotnet.DotnetRuntimesProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class NugetCredentialProviderSelectorTest {
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _runtimesProvider: DotnetRuntimesProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0) }
    }

    @DataProvider
    fun cases(): Array<Array<out Any?>> {
        return arrayOf(
                // Disabled
                arrayOf(
                        Version(3, 1, 100),
                        emptySequence<Version>(),
                        mapOf(
                                "teamcity.nuget.credentialprovider.disabled" to "true"
                        ),
                        true,
                        null
                ),

                // Full .NET
                arrayOf(
                        Version.Empty,
                        emptySequence<Version>(),
                        mapOf(
                                "teamcity.nuget.credentialprovider.disabled" to null,
                                "DotNetCredentialProvider4.0.0_Path" to "CredentialProvide.exe"
                        ),
                        true,
                        "v_CredentialProvide.exe"
                ),

                // Simple case
                arrayOf(
                        Version.CredentialProviderVersion,
                        emptySequence<Version>(),
                        mapOf(
                                "teamcity.nuget.credentialprovider.disabled" to null,
                                "DotNetCredentialProvider2.0.0_Path" to "CredentialProvide.dll"
                        ),
                        true,
                        "v_CredentialProvide.dll"
                ),

                // Has no any DotNetCredentialProvider
                arrayOf(
                        Version(3, 1, 200),
                        emptySequence<Version>(),
                        mapOf(
                                "teamcity.nuget.credentialprovider.disabled" to null,
                                "DotNetCredentialProvider2.0.0_Path" to null,
                                "DotNetCredentialProvider3.0.0_Path" to null
                        ),
                        true,
                        null
                ),

                // .NET 5
                arrayOf(
                        Version(5, 1, 100),
                        emptySequence<Version>(),
                        mapOf(
                                "teamcity.nuget.credentialprovider.disabled" to "FalsE",
                                "DotNetCredentialProvider5.0.0_Path" to "CredentialProvide.dll"
                        ),
                        true,
                        "v_CredentialProvide.dll"
                ),

                // Version too low
                arrayOf(
                        Version(2, 1, 399),
                        emptySequence<Version>(),
                        mapOf(
                                "teamcity.nuget.credentialprovider.disabled" to null,
                                "DotNetCredentialProvider3.0.0_Path" to "CredentialProvide.dll"
                        ),
                        true,
                        null
                ),

                // Should select the credential provider related to the available CredentialProvider and .NET runtime version
                arrayOf(
                        Version(5, 1, 100, "preview"),
                        sequenceOf(Version(3, 1, 200)),
                        mapOf(
                                "teamcity.nuget.credentialprovider.disabled" to null,
                                "DotNetCredentialProvider1.0.0_Path" to "CredentialProvide1.dll",
                                "DotNetCredentialProvider2.0.0_Path" to "CredentialProvide2.dll",
                                "DotNetCredentialProvider3.0.0_Path" to "CredentialProvide3.dll",
                                "DotNetCredentialProvider4.0.0_Path" to "CredentialProvide.exe",
                                "DotNetCredentialProvider5.0.0_Path" to null
                        ),
                        false,
                        "v_CredentialProvide3.dll"
                ),

                // Should select the credential provider related to an the newest available CredentialProvider and .NET runtime version
                arrayOf(
                        Version(5, 1, 100, "preview"),
                        sequenceOf(Version(1, 2, 200), Version(2, 1, 300)),
                        mapOf(
                                "teamcity.nuget.credentialprovider.disabled" to null,
                                "DotNetCredentialProvider1.0.0_Path" to "CredentialProvide1.dll",
                                "DotNetCredentialProvider2.0.0_Path" to "CredentialProvide2.dll",
                                "DotNetCredentialProvider3.0.0_Path" to "CredentialProvide3.dll",
                                "DotNetCredentialProvider4.0.0_Path" to "CredentialProvide.exe",
                                "DotNetCredentialProvider5.0.0_Path" to null
                        ),
                        false,
                        "v_CredentialProvide2.dll"
                ),

                // Should not select the credential provider when in virtual context
                arrayOf(
                        Version(5, 1, 100, "preview"),
                        emptySequence<Version>(),
                        mapOf(
                                "teamcity.nuget.credentialprovider.disabled" to null,
                                "DotNetCredentialProvider1.0.0_Path" to "CredentialProvide1.dll",
                                "DotNetCredentialProvider2.0.0_Path" to "CredentialProvide2.dll",
                                "DotNetCredentialProvider3.0.0_Path" to "CredentialProvide3.dll",
                                "DotNetCredentialProvider4.0.0_Path" to "CredentialProvide.exe",
                                "DotNetCredentialProvider5.0.0_Path" to null
                        ),
                        true,
                        null
                ),

                // Should not select the credential provider when Full .NET
                arrayOf(
                        Version.Empty,
                        emptySequence<Version>(),
                        mapOf(
                                "teamcity.nuget.credentialprovider.disabled" to null,
                                "DotNetCredentialProvider1.0.0_Path" to "CredentialProvide1.dll",
                                "DotNetCredentialProvider2.0.0_Path" to "CredentialProvide2.dll",
                                "DotNetCredentialProvider3.0.0_Path" to "CredentialProvide3.dll",
                                "DotNetCredentialProvider4.0.0_Path" to null,
                                "DotNetCredentialProvider5.0.0_Path" to null
                        ),
                        true,
                        null
                )
        )
    }

    @Test(dataProvider = "cases")
    fun shouldSelectCredentialProvider(
            sdkVersion: Version,
            runtimes: Sequence<Version>,
            params: Map<String, String?>,
            isVirtual: Boolean,
            expectedCredentialProvider: String?) {
        // Given
        val selector = createInstance()

        // When
        for ((key, value) in params) {
            every { _parametersService.tryGetParameter(ParameterType.Configuration, key) } returns value
        }

        every { _parametersService.getParameterNames(ParameterType.Configuration) } returns params.keys.asSequence()
        every { _runtimesProvider.getRuntimes() } returns runtimes.map { DotnetRuntime(File("."), it, "") }
        every { _virtualContext.isVirtual } returns isVirtual

        val actualCredentialProvider = selector.trySelect(sdkVersion)

        // Then
        Assert.assertEquals(actualCredentialProvider, expectedCredentialProvider)
    }

    private fun createInstance() =
            NugetCredentialProviderSelectorImpl(_parametersService, _runtimesProvider, _virtualContext)
}