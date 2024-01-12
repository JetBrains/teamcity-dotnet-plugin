

package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import jetbrains.buildServer.dotnet.ComposedTestsFilterProvider
import jetbrains.buildServer.dotnet.commands.test.TestsFilterProvider
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ComposedTestsFilterProviderTest {
    @DataProvider(name = "testData")
    fun testData(): Any {
        val abc = mockk<TestsFilterProvider>();
        every { abc.getFilterExpression(any()) } returns "Abc"
        val qw = mockk<TestsFilterProvider>()
        every { qw.getFilterExpression(any()) } returns "Qw"
        val xyz = mockk<TestsFilterProvider>()
        every { xyz.getFilterExpression(any()) } returns "Xyz"
        val blank = mockk<TestsFilterProvider>()
        every { blank.getFilterExpression(any()) } returns "  "
        val empty = mockk<TestsFilterProvider>()
        every { empty.getFilterExpression(any()) } returns ""

        return arrayOf(
                arrayOf(emptyList<String>(), ""),
                arrayOf(listOf(abc), "Abc"),
                arrayOf(listOf(empty), ""),
                arrayOf(listOf(blank), ""),
                arrayOf(listOf(blank, empty), ""),
                arrayOf(listOf(abc, xyz), "(Abc) & (Xyz)"),
                arrayOf(listOf(blank, xyz), "Xyz"),
                arrayOf(listOf(blank, xyz, empty), "Xyz"),
                arrayOf(listOf(abc, blank, xyz), "(Abc) & (Xyz)"),
                arrayOf(listOf(abc, blank, xyz, qw, empty), "(Abc) & (Xyz) & (Qw)")
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideFilter(testsFilterProviders: List<TestsFilterProvider>, expecedFilter: String) {
        // Given
        val provider = createInstance(testsFilterProviders)

        // When
        val actualFilter = provider.getFilterExpression(TestsSplittingMode.TestClassNameFilter)

        // Then
        Assert.assertEquals(actualFilter, expecedFilter)
    }

    private fun createInstance(testsFilterProviders: List<TestsFilterProvider>) = ComposedTestsFilterProvider(testsFilterProviders)
}