package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class DotnetFilterFactoryImplTest {
    @MockK private lateinit var _testsFilterProvider: TestsFilterProvider
    @MockK private lateinit var _splittedTestsFilterSettings: SplittedTestsFilterSettings
    @MockK private lateinit var _testRunSettingsFileProvider: TestRunSettingsFileProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    public fun shouldCreateFilterUsingSettignsFileWhenFilterSizeMoreTheLimit() {
        // Given
        val factory = createInstance()
        val filter = "z".repeat(DotnetFilterFactoryImpl.MaxArgSize + 1)
        val settingsFile = File("My.settings")

        // When
        every { _testsFilterProvider.filterExpression } returns filter
        every { _splittedTestsFilterSettings.isActive } returns true
        every { _testRunSettingsFileProvider.tryGet(DotnetCommandType.Test) } returns settingsFile
        val actualFilter = factory.createFilter(DotnetCommandType.Test)

        // Then
        Assert.assertEquals(actualFilter, DotnetFilter("", settingsFile, true))
    }

    @Test
    public fun shouldUseFilterAsArgumentWhenCannotGenerateSettigsFile() {
        // Given
        val factory = createInstance()
        val filter = "z".repeat(DotnetFilterFactoryImpl.MaxArgSize + 1)

        // When
        every { _testsFilterProvider.filterExpression } returns filter
        every { _splittedTestsFilterSettings.isActive } returns true
        every { _testRunSettingsFileProvider.tryGet(DotnetCommandType.Test) } returns null
        val actualFilter = factory.createFilter(DotnetCommandType.Test)

        // Then
        Assert.assertEquals(actualFilter, DotnetFilter(filter, null, true))
    }

    @Test
    public fun shouldUseFilterAsArgumentWhenFilterIsLessOrEqTheLimit() {
        // Given
        val factory = createInstance()
        val filter = "z".repeat(DotnetFilterFactoryImpl.MaxArgSize)

        // When
        every { _testsFilterProvider.filterExpression } returns filter
        every { _splittedTestsFilterSettings.isActive } returns true
        val actualFilter = factory.createFilter(DotnetCommandType.Test)

        // Then
        Assert.assertEquals(actualFilter, DotnetFilter(filter, null, true))
        verify(exactly = 0) { _testRunSettingsFileProvider.tryGet(DotnetCommandType.Test) }
    }

    @Test
    public fun shouldUseFilterAsArgumentWhenNoTestSplitting() {
        // Given
        val factory = createInstance()
        val filter = "z".repeat(DotnetFilterFactoryImpl.MaxArgSize + 1)

        // When
        every { _testsFilterProvider.filterExpression } returns filter
        every { _splittedTestsFilterSettings.isActive } returns false
        val actualFilter = factory.createFilter(DotnetCommandType.Test)

        // Then
        Assert.assertEquals(actualFilter, DotnetFilter(filter, null, false))
        verify(exactly = 0) { _testRunSettingsFileProvider.tryGet(DotnetCommandType.Test) }
    }

    private fun createInstance() = DotnetFilterFactoryImpl(_testsFilterProvider, _splittedTestsFilterSettings, _testRunSettingsFileProvider)
}