

package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsFileProvider
import jetbrains.buildServer.dotnet.commands.test.TestsFilterProvider
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingModeProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class DotnetFilterFactoryImplTest {
    @MockK private lateinit var _testsFilterProvider: TestsFilterProvider
    @MockK private lateinit var _testsSplittingModeProvider: TestsSplittingModeProvider
    @MockK private lateinit var _testRunSettingsFileProvider: TestRunSettingsFileProvider
    @MockK private lateinit var _dotnetCommandContext: DotnetCommandContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun `should create filter using settings file when filter size bigger than limit`() {
        // arrange
        val factory = createInstance()
        val filter = "a".repeat(DotnetFilterFactoryImpl.MaxArgSize + 1)
        val settingsFile = File("My.settings")

        // act
        every { _testsFilterProvider.getFilterExpression(any()) } returns filter
        every { _testsSplittingModeProvider.getMode(any()) } returns TestsSplittingMode.TestClassNameFilter
        every { _testRunSettingsFileProvider.tryGet(_dotnetCommandContext) } returns settingsFile
        every { _dotnetCommandContext.command.commandType } returns DotnetCommandType.Test
        every { _dotnetCommandContext.toolVersion } returns Version.Empty
        val actualFilter = factory.createFilter(_dotnetCommandContext)

        // assert
        Assert.assertEquals(actualFilter, DotnetFilter("", settingsFile))
    }

    @Test
    fun `should use filter as argument when cannot generate settings file`() {
        // arrange
        val factory = createInstance()
        val filter = "a".repeat(DotnetFilterFactoryImpl.MaxArgSize + 1)

        // act
        every { _testsFilterProvider.getFilterExpression(any()) } returns filter
        every { _testsSplittingModeProvider.getMode(any()) } returns TestsSplittingMode.TestClassNameFilter
        every { _testRunSettingsFileProvider.tryGet(_dotnetCommandContext) } returns null
        every { _dotnetCommandContext.command.commandType } returns DotnetCommandType.Test
        every { _dotnetCommandContext.toolVersion } returns Version.Empty
        val actualFilter = factory.createFilter(_dotnetCommandContext)

        // assert
        Assert.assertEquals(actualFilter, DotnetFilter(filter, null))
    }

    @Test
    fun `should use filter as argument when filter is less or eq the limit`() {
        // arrange
        val factory = createInstance()
        val filter = "a".repeat(DotnetFilterFactoryImpl.MaxArgSize)

        // act
        every { _testsFilterProvider.getFilterExpression(any()) } returns filter
        every { _testsSplittingModeProvider.getMode(any()) } returns TestsSplittingMode.TestClassNameFilter
        every { _dotnetCommandContext.command.commandType } returns DotnetCommandType.Test
        every { _dotnetCommandContext.toolVersion } returns Version.Empty
        val actualFilter = factory.createFilter(_dotnetCommandContext)

        // assert
        Assert.assertEquals(actualFilter, DotnetFilter(filter, null))
        verify(exactly = 0) { _testRunSettingsFileProvider.tryGet(any()) }
    }

    @Test
    fun `should use filter using settings when no test splitting, for example test retry`() {
        // arrange
        val factory = createInstance()
        val filter = "a".repeat(DotnetFilterFactoryImpl.MaxArgSize + 1)
        val settingsFile = File("My.settings")

        // act
        every { _testsFilterProvider.getFilterExpression(any()) } returns filter
        every { _testRunSettingsFileProvider.tryGet(_dotnetCommandContext) } returns settingsFile
        every { _testsSplittingModeProvider.getMode(any()) } returns TestsSplittingMode.Disabled
        every { _dotnetCommandContext.command.commandType } returns DotnetCommandType.Test
        every { _dotnetCommandContext.toolVersion } returns Version.Empty
        val actualFilter = factory.createFilter(_dotnetCommandContext)

        // assert
        Assert.assertEquals(actualFilter, DotnetFilter("", settingsFile))
    }

    private fun createInstance() = DotnetFilterFactoryImpl(_testsFilterProvider, _testsSplittingModeProvider, _testRunSettingsFileProvider)
}