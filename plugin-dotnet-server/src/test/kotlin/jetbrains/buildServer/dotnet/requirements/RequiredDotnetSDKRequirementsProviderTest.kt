package jetbrains.buildServer.dotnet.requirements

import io.mockk.*
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.MonoConstants
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import org.bouncycastle.cert.ocsp.Req
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class RequiredDotnetSDKRequirementsProviderTest {
    @BeforeMethod
    fun setup() {
        clearAllMocks()
        MockKAnnotations.init(this)
    }

    @DataProvider
    fun testRequirementsData() = arrayOf(
        arrayOf(emptyMap<String, String>(), emptyList<String>()),
        arrayOf(mapOf(DotnetConstants.PARAM_REQUIRED_SDK to "  "), emptyList<String>()),
        arrayOf(
            mapOf(DotnetConstants.PARAM_REQUIRED_SDK to "net2.2\nnet5.0;net6.0 net7.0"),
            listOf("net2.2", "net5.0", "net6.0", "net7.0")
        ),
    )

    @Test(dataProvider = "testRequirementsData")
    fun shouldProvideRequirements(parameters: Map<String, String>, expectedSDKs: List<String>) {
        // arrange
        val _sdkBasedRequirementFactoryMock = mockk<SDKBasedRequirementFactory> {
            expectedSDKs.forEach { sdk ->
                every { tryCreate(sdk) } returns mockk<Requirement> { every { id } returns sdk }
            }
        }
        val instance = RequiredDotnetSDKRequirementsProvider(_sdkBasedRequirementFactoryMock)

        // act
        val actualRequirements = instance.getRequirements(parameters)

        // assert
        assertEquals(actualRequirements.map { it.id }.toList(), expectedSDKs)
        verify(exactly = expectedSDKs.size) { _sdkBasedRequirementFactoryMock.tryCreate(any()) }
        expectedSDKs.forEach {
            verify(exactly = 1) { _sdkBasedRequirementFactoryMock.tryCreate(it) }
        }
    }

    @Test
    fun `should not provide requirement if factory returns null`() {
        // arrange
        val sdk1 = "net7.0"
        val sdk2 = "net8.0"
        val parameters = mapOf(DotnetConstants.PARAM_REQUIRED_SDK to "$sdk1 $sdk2")
        val _sdkBasedRequirementFactoryMock = mockk<SDKBasedRequirementFactory> {
            every { tryCreate(sdk1) } returns mockk()
            every { tryCreate(sdk2) } returns null
        }
        val instance = RequiredDotnetSDKRequirementsProvider(_sdkBasedRequirementFactoryMock)

        // act
        val actualRequirements = instance.getRequirements(parameters)

        // assert
        assertEquals(actualRequirements.toList().size, 1)
        verify(exactly = 2) { _sdkBasedRequirementFactoryMock.tryCreate(any()) }
    }
}