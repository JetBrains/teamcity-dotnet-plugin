package jetbrains.buildServer.dotnet.test.dotnet.commands.test.retry

import jetbrains.buildServer.dotnet.commands.test.retry.TestRetryFilterProviderImpl
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class TestRetryFilterProviderImplTest {
    @Test
    fun `should build empty filter when no tests are set`() {
        // arrange
        val provider = TestRetryFilterProviderImpl()

        // act
        val actualFilter = provider.getFilterExpression(TestsSplittingMode.Disabled)

        // assert
        Assert.assertEquals(actualFilter, "")
    }

    @DataProvider(name = "testData")
    fun testData(): Any {
        return arrayOf(
            arrayOf(emptyList<String>(), ""),
            arrayOf(listOf("testName1", "testName2"), "FullyQualifiedName=testName1 | FullyQualifiedName=testName2"),
            arrayOf(listOf("special&!chars"), """FullyQualifiedName=special\&\!chars"""),
            arrayOf(listOf("testWithParameters(a=1)"), """FullyQualifiedName~testWithParameters"""),
            arrayOf(listOf("genericTest<String>"), """FullyQualifiedName~genericTest"""),
        )
    }

    @Test(dataProvider = "testData")
    fun `should build filter`(testNames: List<String>, expectedFilter: String) {
        // arrange
        val provider = TestRetryFilterProviderImpl()
        provider.setTestNames(testNames)

        // act
        val actualFilter = provider.getFilterExpression(TestsSplittingMode.Disabled)

        // assert
        Assert.assertEquals(actualFilter, expectedFilter)
    }

    @Test
    fun `should reset filter after dispose`() {
        // arrange
        val provider = TestRetryFilterProviderImpl()
        val filterDisposable = provider.setTestNames(listOf("test1", "test2"))

        // act
        filterDisposable.dispose()
        val actualFilter = provider.getFilterExpression(TestsSplittingMode.Disabled)

        // assert
        Assert.assertEquals(actualFilter, "")
    }
}