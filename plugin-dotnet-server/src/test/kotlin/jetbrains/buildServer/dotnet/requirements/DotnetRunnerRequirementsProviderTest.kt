package jetbrains.buildServer.dotnet.requirements

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.RequirementsProvider
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.requirements.commands.DotnetCommandRequirementsProvider
import jetbrains.buildServer.requirements.Requirement
import org.testng.Assert
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class DotnetRunnerRequirementsProviderTest {
    private lateinit var _commandRequirementsProvidersMocks: List<DotnetCommandRequirementsProvider>
    @MockK private lateinit var _dotnetCommandRequirementsProviderMock1: DotnetCommandRequirementsProvider
    @MockK private lateinit var _dotnetCommandRequirementsProviderMock2: DotnetCommandRequirementsProvider
    @MockK private lateinit var _dotnetCommandRequirementsProviderMock3: DotnetCommandRequirementsProvider
    @MockK private lateinit var _sdkRequirementsProviderMock: RequirementsProvider
    @MockK private lateinit var _dotCoverRequirementsProviderMock: RequirementsProvider
    private lateinit var _instance: DotnetRunnerRequirementsProvider;

    @BeforeMethod
    fun setUp() {
        clearAllMocks()
        MockKAnnotations.init(this, relaxed = true)
        _commandRequirementsProvidersMocks = listOf(
            _dotnetCommandRequirementsProviderMock1,
            _dotnetCommandRequirementsProviderMock2,
            _dotnetCommandRequirementsProviderMock3,
        )
        _instance = DotnetRunnerRequirementsProvider(
            _commandRequirementsProvidersMocks,
            _sdkRequirementsProviderMock,
            _dotCoverRequirementsProviderMock,
        )
    }

    @Test
    fun `should not provide requirements when docker image parameter is present`() {
        // act
        val actualRequirements = _instance.getRequirements(mapOf(DotnetConstants.PARAM_DOCKER_IMAGE to "something"))

        // assert
        assertEquals(0, actualRequirements.toList().size)
        _commandRequirementsProvidersMocks.forEach {
            verify(exactly = 0) { it.getRequirements(any()) }
        }
        _sdkRequirementsProviderMock.also {
            verify(exactly = 0) { it.getRequirements(any()) }
        }
    }

    @Test
    fun `should provide requirements according to command type and coverage type`() {
        // arrange
        val dotnetCommandType = DotnetCommandType.Build
        val coverageType = CoverageConstants.PARAM_DOTCOVER
        val requirements1 = sequenceOf<Requirement>(mockk(), mockk())
        val requirements2 = sequenceOf<Requirement>(mockk())
        val sdkRequirements = sequenceOf<Requirement>(mockk(), mockk(), mockk())
        val dotCoverRequirements = sequenceOf<Requirement>(mockk())
        _dotnetCommandRequirementsProviderMock1.also {
            every { it.commandType } returns dotnetCommandType
            every { it.getRequirements(any()) } returns requirements1
        }
        _dotnetCommandRequirementsProviderMock2.also {
            every { it.commandType } returns dotnetCommandType
            every { it.getRequirements(any()) } returns requirements2
        }
        _dotnetCommandRequirementsProviderMock3.also {
            every { it.commandType } returns DotnetCommandType.Test
        }
        _sdkRequirementsProviderMock.also {
            every { it.getRequirements(any()) } returns sdkRequirements
        }
        _dotCoverRequirementsProviderMock.also {
            every { it.getRequirements(any()) } returns dotCoverRequirements
        }
        val parameters = mapOf(
            DotnetConstants.PARAM_COMMAND to dotnetCommandType.id,
            CoverageConstants.PARAM_TYPE to coverageType,
        )

        // act
        val actualRequirements = _instance.getRequirements(parameters).toList()

        // assert
        assertEquals(actualRequirements.size, 7)
        assertEquals(
            actualRequirements,
            (requirements1 + requirements2 + sdkRequirements + dotCoverRequirements).toList()
        )
        _dotnetCommandRequirementsProviderMock1.also {
            verify(exactly = 1) { it.getRequirements(parameters) }
        }
        _dotnetCommandRequirementsProviderMock2.also {
            verify(exactly = 1) { it.getRequirements(parameters) }
        }
        _dotnetCommandRequirementsProviderMock3.also {
            verify(exactly = 0) { it.getRequirements(parameters) }
        }
        _sdkRequirementsProviderMock.also {
            verify(exactly = 1) { it.getRequirements(parameters) }
        }
        _dotCoverRequirementsProviderMock.also {
            verify(exactly = 1) { it.getRequirements(parameters) }
        }
    }

    @Test
    fun `should not provide dotCover requirements when coverage type is not 'dotcover'`() {
        // arrange
        val dotnetCommandType = DotnetCommandType.Build
        val requirements1 = sequenceOf<Requirement>(mockk(), mockk())
        val requirements2 = sequenceOf<Requirement>(mockk())
        val sdkRequirements = sequenceOf<Requirement>(mockk(), mockk(), mockk())
        val dotCoverRequirements = sequenceOf<Requirement>(mockk())
        _dotnetCommandRequirementsProviderMock1.also {
            every { it.commandType } returns dotnetCommandType
            every { it.getRequirements(any()) } returns requirements1
        }
        _dotnetCommandRequirementsProviderMock2.also {
            every { it.commandType } returns dotnetCommandType
            every { it.getRequirements(any()) } returns requirements2
        }
        _dotnetCommandRequirementsProviderMock3.also {
            every { it.commandType } returns DotnetCommandType.Test
        }
        _sdkRequirementsProviderMock.also {
            every { it.getRequirements(any()) } returns sdkRequirements
        }
        _dotCoverRequirementsProviderMock.also {
            every { it.getRequirements(any()) } returns dotCoverRequirements
        }
        val parameters = mapOf(
            DotnetConstants.PARAM_COMMAND to dotnetCommandType.id,
            CoverageConstants.PARAM_TYPE to "NOT_DOTCOVER",
        )

        // act
        val actualRequirements = _instance.getRequirements(parameters).toList()

        // assert
        assertEquals(actualRequirements.size, 6)
        assertEquals(
            actualRequirements,
            (requirements1 + requirements2 + sdkRequirements).toList()
        )
        _dotnetCommandRequirementsProviderMock1.also {
            verify(exactly = 1) { it.getRequirements(parameters) }
        }
        _dotnetCommandRequirementsProviderMock2.also {
            verify(exactly = 1) { it.getRequirements(parameters) }
        }
        _dotnetCommandRequirementsProviderMock3.also {
            verify(exactly = 0) { it.getRequirements(parameters) }
        }
        _sdkRequirementsProviderMock.also {
            verify(exactly = 1) { it.getRequirements(parameters) }
        }
        _dotCoverRequirementsProviderMock.also {
            verify(exactly = 0) { it.getRequirements(parameters) }
        }
    }
}