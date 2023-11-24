package jetbrains.buildServer.dotCover

import io.mockk.mockk
import jetbrains.buildServer.tools.ToolType
import org.testng.Assert.assertTrue
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverToolComparatorTest {

    @DataProvider
    fun testDataVersionComparison(): Array<Array<String>> {
        return arrayOf(
            // only major
            arrayOf("2023", "<", "2024"),
            arrayOf("2024", ">", "2023"),
            arrayOf("2023", "=", "2023"),
            // major and major.minor
            arrayOf("2023", "<", "2023.1"),
            arrayOf("2023.1", ">", "2023"),
            arrayOf("2022.9", "<", "2023"),
            arrayOf("2023", ">", "2022.9"),
            arrayOf("2023", "=", "2023.0"),
            arrayOf("2023.0", "=", "2023"),
            // major and major.minor.patch
            arrayOf("2023", "<", "2023.0.1"),
            arrayOf("2023.0.1", ">", "2023"),
            arrayOf("2023.0.0", "=", "2023"),
            arrayOf("2023", "=", "2023.0.0"),
            // major.minor and major.minor
            arrayOf("2023.1", "<", "2023.2"),
            arrayOf("2023.2", ">", "2023.1"),
            arrayOf("2023.1", "=", "2023.1"),
            // major.minor and major.minor.patch
            arrayOf("2023.3", "<", "2023.3.1"),
            arrayOf("2023.3.1", ">", "2023.3"),
            arrayOf("2023.2.9", "<", "2023.3"),
            arrayOf("2023.3", ">", "2023.2.9"),
            arrayOf("2023.3.0", "=", "2023.3"),
            arrayOf("2023.3", "=", "2023.3.0"),
            // major.minor.patch and major.minor.patch
            arrayOf("2023.3.1", "<", "2023.3.2"),
            arrayOf("2023.3.2", ">", "2023.3.1"),
            arrayOf("2023.2.9", "<", "2023.3.1"),
            arrayOf("2023.3.1", ">", "2023.2.9"),
            arrayOf("2023.3.1", "=", "2023.3.1"),
            // major.minor and major.minor.patch-eap
            arrayOf("2023.3.0-eap01", "<", "2023.3"),
            arrayOf("2023.3", ">", "2023.3.0-eap01"),
            // major.minor.patch and major.minor.patch-eap
            arrayOf("2023.3.0-eap01", "<", "2023.3.0"),
            arrayOf("2023.3.0", ">", "2023.3.0-eap01"),
            // major.minor.patch-eap and major.minor.patch-eap
            arrayOf("2023.3.0-eap01", "<", "2023.3.0-eap02"),
            arrayOf("2023.3.0-eap02", ">", "2023.3.0-eap01"),
            arrayOf("2023.3.0-eap01", "=", "2023.3.0-eap01"),
            // major.minor and major.minor.patch-tc
            arrayOf("2023.3.0-tc01", "<", "2023.3"),
            arrayOf("2023.3", ">", "2023.3.0-tc01"),
            // major.minor.patch and major.minor.patch-tc
            arrayOf("2023.3.0-tc01", "<", "2023.3.0"),
            arrayOf("2023.3.0", ">", "2023.3.0-tc01"),
            // major.minor.patch-tc and major.minor.patch-tc
            arrayOf("2023.3.0-tc01", "<", "2023.3.0-tc02"),
            arrayOf("2023.3.0-tc02", ">", "2023.3.0-tc01"),
            arrayOf("2023.3.0-tc01", "=", "2023.3.0-tc01"),
            // major.minor.patch-eap and major.minor.patch-tc
            arrayOf("2023.3.0-eap01", "<", "2023.3.0-tc01"),
            arrayOf("2023.3.0-tc01", ">", "2023.3.0-eap01"),
        )
    }

    @Test(dataProvider = "testDataVersionComparison")
    fun `should compare dotCover versions`(v1: String, expectedResult: String, v2: String) {
        // arrange
        val comparator = DotCoverToolComparator()

        // act
        val result = comparator.compare(version(v1), version(v2))

        // assert
        val errorMessage = "$v1 should be $expectedResult $v2"
        when (expectedResult) {
            "<" -> assertTrue(result < 0, errorMessage)
            ">" -> assertTrue(result > 0, errorMessage)
            "=" -> assertTrue(result == 0, errorMessage)
        }
    }

    private fun version(version: String) = DotCoverToolVersion(
        mockk<ToolType>(relaxed = true),
        version,
        "JetBrains.dotCover.CommandLineTools",
        false
    )
}