package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.Version
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class VersionTest {
    @DataProvider
    fun testDataComparable(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(Version(2), Version(1), 1),
                arrayOf(Version(2, 0), Version(1, 0), 1),
                arrayOf(Version(2), Version(2), 0),
                arrayOf(Version(0, 2), Version(0, 2), 0),
                arrayOf(Version(2), Version(2, 0), 0),
                arrayOf(Version(2, 0, 1), Version(2, 0, 1), 0),
                arrayOf(Version(2, 1), Version(2, 1, 0), 0),
                arrayOf(Version(2, 1), Version(2, 1, 0, 0, 0), 0),
                arrayOf(Version(2, 0, 1), Version(2, 0, 1, 0, 0, 0), 0),
                arrayOf(Version(1), Version(2), -1),
                arrayOf(Version(1, 2), Version(1, 1), 1),
                arrayOf(Version(), Version(1), -1),
                arrayOf(Version(3, 3), Version(), 2),
                arrayOf(Version(0, 3), Version(5), -5))
    }

    @Test(dataProvider = "testDataComparable")
    fun shouldBeComparable(version1: Version, version2: Version, exptectedCompareResult: Int) {
        // Given

        // When
        val actualCompareResult = version1.compareTo(version2)

        // Then
        Assert.assertEquals(actualCompareResult, exptectedCompareResult)
    }

    @DataProvider
    fun testDataToString(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(Version(2), "2"),
                arrayOf(Version(99, 3, 10), "99.3.10"),
                arrayOf(Version(0, 2), "0.2"),
                arrayOf(Version(2, 0, 0), "2.0.0"),
                arrayOf(Version(0, 0, 2, 0, 0), "0.0.2.0.0"))
    }

    @Test(dataProvider = "testDataToString")
    fun shouldSupportToString(version: Version, expectedString: String) {
        // Given

        // When
        val actualString = version.toString()

        // Then
        Assert.assertEquals(actualString, expectedString)
    }

    @DataProvider
    fun testDataEquitable(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(Version(1), Version(1), true),
                arrayOf(Version(0, 1), Version(0, 1), true),
                arrayOf(Version(1), Version(1, 0), true),
                arrayOf(Version(1), Version(1, 0, 0), true),
                arrayOf(Version(1), Version(2), false),
                arrayOf(Version(1, 0), Version(2, 0), false),
                arrayOf(Version(0, 1, 0), Version(0, 2, 0), false),
                arrayOf(Version(), Version(2), false),
                arrayOf(Version(1, 2), Version(1, 2), true),
                arrayOf(Version(1, 2), Version(1), false),
                arrayOf(Version(1, 2), Version(1, 3), false),
                arrayOf(Version(1, 2), Version(2, 2), false))
    }

    @Test(dataProvider = "testDataEquitable")
    fun shouldBeEquitable(version1: Version, version2: Version, exptectedEqualsResult: Boolean) {
        // Given

        // When
        val actualEqualsResult1 = version1 == version2
        val actualEqualsResult2 = version1 == version2
        val actualEqualsResult3 = version2 == version1
        val hashCode1 = version1.hashCode()
        val hashCode2 = version1.hashCode()

        // Then
        Assert.assertEquals(actualEqualsResult1, exptectedEqualsResult)
        Assert.assertEquals(actualEqualsResult2, exptectedEqualsResult)
        Assert.assertEquals(actualEqualsResult3, exptectedEqualsResult)
        if (exptectedEqualsResult) {
            Assert.assertTrue(hashCode1 == hashCode2)
        }
    }

    @DataProvider
    fun testDataParse(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf("", Version.Empty),
                arrayOf("1", Version(1)),
                arrayOf("1.23.99", Version(1, 23, 99)),
                arrayOf("1 . 23 .   99", Version(1, 23, 99)),
                arrayOf("abc", Version.Empty),
                arrayOf("abc.xyz", Version.Empty),
                arrayOf("abc.", Version.Empty),
                arrayOf("1.", Version.Empty),
                arrayOf(".xyz", Version.Empty),
                arrayOf(".1", Version.Empty),
                arrayOf("abc.1", Version.Empty),
                arrayOf("1.abc", Version.Empty))
    }

    @Test(dataProvider = "testDataParse")
    fun shouldParse(text: String, expectedVersion: Version) {
        // Given

        // When
        val actualVersion = Version.parse(text)

        // Then
        Assert.assertEquals(actualVersion, expectedVersion)
    }
}