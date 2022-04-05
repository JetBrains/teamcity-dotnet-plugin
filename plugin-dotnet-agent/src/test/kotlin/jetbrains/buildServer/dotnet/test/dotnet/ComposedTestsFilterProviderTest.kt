package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import jetbrains.buildServer.dotnet.ComposedTestsFilterProvider
import jetbrains.buildServer.dotnet.TestsFilterProvider
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ComposedTestsFilterProviderTest {
    @DataProvider(name = "testData")
    fun testData(): Any? {
        val abc = mockk<TestsFilterProvider>();
        every { abc.filterExpression } returns "Abc"
        val qw = mockk<TestsFilterProvider>();
        every { qw.filterExpression } returns "Qw"
        val xyz = mockk<TestsFilterProvider>();
        every { xyz.filterExpression } returns "Xyz"
        val blank = mockk<TestsFilterProvider>();
        every { blank.filterExpression } returns "  "
        val empty = mockk<TestsFilterProvider>();
        every { empty.filterExpression } returns ""

        return arrayOf(
                arrayOf(emptyList<String>(), ""),
                arrayOf(listOf(abc), "\"Abc\""),
                arrayOf(listOf(empty), ""),
                arrayOf(listOf(blank), ""),
                arrayOf(listOf(blank, empty), ""),
                arrayOf(listOf(abc, xyz), "\"(Abc) & (Xyz)\""),
                arrayOf(listOf(blank, xyz), "\"Xyz\""),
                arrayOf(listOf(blank, xyz, empty), "\"Xyz\""),
                arrayOf(listOf(abc, blank, xyz), "\"(Abc) & (Xyz)\""),
                arrayOf(listOf(abc, blank, xyz, qw, empty), "\"(Abc) & (Xyz) & (Qw)\"")
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideFilter(testsFilterProviders: List<TestsFilterProvider>, expecedFilter: String) {
        // Given
        val provider = createInstance(testsFilterProviders)

        // When
        val actualFilter = provider.filterExpression;

        // Then
        Assert.assertEquals(actualFilter, expecedFilter)
    }

    private fun createInstance(testsFilterProviders: List<TestsFilterProvider>) = ComposedTestsFilterProvider(testsFilterProviders)
}