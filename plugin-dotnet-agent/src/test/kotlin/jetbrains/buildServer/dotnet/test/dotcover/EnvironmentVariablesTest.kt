package jetbrains.buildServer.dotnet.test.dotcover

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.dotcover.EnvironmentVariablesImpl
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class EnvironmentVariablesTest {
    @MockK private lateinit var  _virtualContext: VirtualContext;

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider(name = "defaultVars")
    fun osTypesData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(OSType.UNIX, EnvironmentVariablesImpl.linuxDefaultVariables),
                arrayOf(OSType.MAC, emptySequence<CommandLineEnvironmentVariable>()),
                arrayOf(OSType.WINDOWS, emptySequence<CommandLineEnvironmentVariable>()))
    }

    @Test(dataProvider = "defaultVars")
    fun shouldProvideLinuxDefaultVarsWhenLinux(os: OSType, expectedVariables: Sequence<CommandLineEnvironmentVariable>) {
        // Given
        val environmentVariables = createInstance()

        // When
        every { _virtualContext.targetOSType } returns os
        val actualVariables = environmentVariables.getVariables().toList()

        // Then
        Assert.assertEquals(actualVariables, expectedVariables.toList())
    }

      private fun createInstance() = EnvironmentVariablesImpl(_virtualContext)
}