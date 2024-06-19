package jetbrains.buildServer.dotnet.test.nunit.arguments

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.nunit.*
import jetbrains.buildServer.nunit.arguments.NUnitArgumentsProvider
import jetbrains.buildServer.nunit.arguments.NUnitTestFilterProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class NUnitArgumentsProviderTest {
    @MockK
    private lateinit var _nUnitSettings: NUnitSettings

    @MockK
    private lateinit var _nUnitFilterProvider: NUnitTestFilterProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    private val resultFile = File("result.xml")

    @Test
    fun `should provide nunit command line arguments`() {
        // arrange
        every { _nUnitFilterProvider.filter } returns "filter"

        _nUnitSettings.run {
            every { additionalCommandLine } returns "--workers=10 --verbose"
        }

        val provider = NUnitArgumentsProvider(
            _nUnitSettings, ArgumentsServiceStub(), _nUnitFilterProvider
        )

        // act
        val arguments = provider.createCommandLineArguments(resultFile).map { it.value }.toList()

        // assert
        Assert.assertEquals(arguments, listOf(
            "--result=" + resultFile.absolutePath,
            "--noheader",
            "--where",
            "filter",
            "--workers=10",
            "--verbose"
        ),)
    }
}