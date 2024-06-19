package jetbrains.buildServer.nunit

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.RequirementsProvider
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class NUnitRequirementsProviderTest {
    @MockK
    private lateinit var _dotCoverRequirementsProviderMock: RequirementsProvider

    @BeforeMethod
    fun setUp() {
        clearAllMocks()
        MockKAnnotations.init(this)
    }

    @Test
    fun `should not provide requirements when docker image parameter is present`() {
        // arrange
        val provider = NUnitRequirementsProvider(_dotCoverRequirementsProviderMock)
        val parameters = mapOf(DotnetConstants.PARAM_DOCKER_IMAGE to "something")

        // act
        val requirements = provider.getRequirements(parameters)

        // assert
        assertEquals(requirements.count(), 0)
    }

    @Test
    fun `should provide nunit dotnet framework or mono requirement`() {
        // arrange
        val provider = NUnitRequirementsProvider(_dotCoverRequirementsProviderMock)

        // act
        val requirements = provider.getRequirements(emptyMap()).toList()

        // assert
        assertEquals(requirements.size, 1)
        requirements.single().let {
            assertEquals(it.propertyName, "Exists=>(DotNetFramework.*|Mono_Path)")
            assertNull(it.propertyValue)
        }
    }

    @Test
    fun `should append dotcover requirements to nunit`() {
        // arrange
        every { _dotCoverRequirementsProviderMock.getRequirements(any()) } returns sequenceOf(
            Requirement("dotcover-requirement", null, RequirementType.EXISTS)
        )

        val provider = NUnitRequirementsProvider(_dotCoverRequirementsProviderMock)
        val parameters = mapOf(
            CoverageConstants.PARAM_TYPE to CoverageConstants.PARAM_DOTCOVER,
        )

        // act
        val requirements = provider.getRequirements(parameters).toList()

        // assert
        assertEquals(requirements.size, 2)
        assertTrue(requirements.any { it.propertyName == "dotcover-requirement" })
    }
}