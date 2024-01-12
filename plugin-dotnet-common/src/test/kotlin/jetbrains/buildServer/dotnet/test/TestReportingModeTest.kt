

package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.TestReportingMode
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.util.*

class TestReportingModeTest {

    @DataProvider
    fun testDataParse(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf("", EnumSet.noneOf<TestReportingMode>(TestReportingMode::class.java)),
                arrayOf("   ", EnumSet.noneOf<TestReportingMode>(TestReportingMode::class.java)),
                arrayOf("off", EnumSet.of<TestReportingMode>(TestReportingMode.Off)),
                arrayOf("Off", EnumSet.of<TestReportingMode>(TestReportingMode.Off)),
                arrayOf("OFF", EnumSet.of<TestReportingMode>(TestReportingMode.Off)),
                arrayOf("on", EnumSet.of<TestReportingMode>(TestReportingMode.On)),
                arrayOf("On", EnumSet.of<TestReportingMode>(TestReportingMode.On)),
                arrayOf("MultiAdapterPath", EnumSet.of<TestReportingMode>(TestReportingMode.MultiAdapterPath)),
                arrayOf("multiadapterpath", EnumSet.of<TestReportingMode>(TestReportingMode.MultiAdapterPath)),
                arrayOf("off|on|MultiAdapterPath", EnumSet.of<TestReportingMode>(TestReportingMode.Off, TestReportingMode.On, TestReportingMode.MultiAdapterPath)),
                arrayOf("  OFF |on   | MultiAdapterPath ", EnumSet.of<TestReportingMode>(TestReportingMode.Off, TestReportingMode.On, TestReportingMode.MultiAdapterPath)))
    }

    @Test(dataProvider = "testDataParse")
    fun shouldParse(text: String, expectedMode: EnumSet<TestReportingMode>) {
        // Given

        // When
        val actualModes = TestReportingMode.parse(text)

        // Then
        Assert.assertEquals(actualModes, expectedMode)
    }
}