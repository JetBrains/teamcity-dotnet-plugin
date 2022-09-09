package jetbrains.buildServer.dotnet.test.dotnet.commands.resolution

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStream
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStreamResolver
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStreamResolvingStage
import jetbrains.buildServer.dotnet.commands.resolution.RootDotnetCommandStreamResolver
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class RootDotnetCommandStreamResolverTests {
    @MockK
    private lateinit var _resolverMock1: DotnetCommandsStreamResolver

    @MockK
    private lateinit var _resolverMock2: DotnetCommandsStreamResolver

    @MockK
    private lateinit var _resolverMock3: DotnetCommandsStreamResolver

    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
    }

    @Test
    fun `should be on initial stage`() {
        // arrange
        val resolver = create()

        // act
        val result = resolver.stage

        // assert
        Assert.assertEquals(result, DotnetCommandsStreamResolvingStage.Initial)
    }

    @Test
    fun `should be always applicable`() {
        // arrange
        every { _resolverMock1.stage } answers { DotnetCommandsStreamResolvingStage.CommandRetrieve }
        every { _resolverMock2.stage } answers { DotnetCommandsStreamResolvingStage.Targeting }
        every { _resolverMock3.stage } answers { DotnetCommandsStreamResolvingStage.Transformation }

        val resolver1ResultStream = mockk<DotnetCommandsStream>()
        every { _resolverMock1.resolve(any()) } answers { resolver1ResultStream }
        val resolver2ResultStream = mockk<DotnetCommandsStream>()
        every { _resolverMock2.resolve(any()) } answers { resolver2ResultStream }
        val resolver3ResultStream = mockk<DotnetCommandsStream>()
        every { _resolverMock3.resolve(any()) } answers { resolver3ResultStream }

        val resolver = create()

        val initialStream = mockk<DotnetCommandsStream>()

        // act
        val result = resolver.resolve(initialStream)

        // assert
        Assert.assertNotNull(result)
        verify (exactly = 1) { _resolverMock1.resolve(match { it == initialStream }) }
        verify (exactly = 1) { _resolverMock2.resolve(match { it == resolver1ResultStream }) }
        verify (exactly = 1) { _resolverMock3.resolve(match { it == resolver2ResultStream }) }
        Assert.assertEquals(result, resolver3ResultStream)
    }

    private fun create() = RootDotnetCommandStreamResolver(listOf(_resolverMock1, _resolverMock2, _resolverMock3))
}