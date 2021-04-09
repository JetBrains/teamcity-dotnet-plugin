package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.MSBuildParameter
import jetbrains.buildServer.dotnet.MSBuildParameterConverterImpl
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MSBuildParameterConverterTest {
    @DataProvider
    fun casesNames(): Array<Array<String>> {
        return arrayOf(
                arrayOf("name", "name"),
                arrayOf("Name", "Name"),
                // special
                arrayOf("_Name", "_Name"),
                arrayOf("Na_me", "Na_me"),
                arrayOf("Na_me_", "Na_me_"),
                arrayOf("Na-me-", "Na-me-"),
                arrayOf("Na_m88e-9", "Na_m88e-9"),
                // first symbol
                arrayOf("1name", "_name"),
                arrayOf("=name", "_name"),
                arrayOf("-name", "_name"),
                arrayOf("@name", "_name"),
                arrayOf("+name", "_name"),
                arrayOf("-name", "_name"),
                arrayOf("#name", "_name"),
                arrayOf("\$name", "_name"),
                // other symbols
                arrayOf("name#", "name_"),
                arrayOf("name.aa", "name_aa"),
                arrayOf("name.Aa", "name_Aa"),
                arrayOf("name Aa", "name_Aa"),
                arrayOf("name^aa", "name_aa"),
                arrayOf("name&aa", "name_aa"),
                arrayOf("name*aa", "name_aa"),
                arrayOf("name=aa", "name_aa"),
                arrayOf("name!", "name_"),
                arrayOf("name>", "name_"),
                arrayOf("name<", "name_"),
                arrayOf("name?", "name_"))
    }

    @Test(dataProvider = "casesNames")
    fun shouldNormalizeName(name: String, expectedName: String) {
        // Given
        val converter = MSBuildParameterConverterImpl()

        // When
        val actualName = converter.normalizeName(name)

        // Then
        Assert.assertEquals(expectedName, actualName)
    }

    @DataProvider
    fun valueCases(): Array<Array<Any>> {
        return arrayOf(
                // escaped
                arrayOf("!@#\$%^&*()_+~1234-=/;'][{}\":<>,.?/??~`", "\"%21%40%23%24%%5E%26%2A%28%29%5F%2B%7E1234%2D%3D%2F;%27%5D%5B%7B%7D%22%3A%3C%3E%2C%2E%3F%2F%3F%3F%7E%60\"", false),
                arrayOf("value 123", "value%20123", false),
                arrayOf("value \" 123", "value%20%22%20123", false),
                arrayOf("value \\ 123", "value%20%5C%20123", false),
                arrayOf("value \"\" 123", "value%20%22%22%20123", false),
                arrayOf("value \" \" 123", "value%20%22%20%22%20123", false),
                arrayOf("value1 \n value2", "value1%20%0A%20value2", false),
                arrayOf("value1 \r value2", "value1%20%0D%20value2", false),
                arrayOf("value1 \t value2", "value1%20%09%20value2", false),
                arrayOf("value1 \b value2", "value1%20%08%20value2", false),

                // should not escape `;` for response files and should wrap a parameter by double quotes in this case (https://github.com/JetBrains/teamcity-dotnet-plugin/issues/144)
                arrayOf("Value;123", "\"Value;123\"", false),

                // should escape `;` for command line parameters https://youtrack.jetbrains.com/issue/TW-64835
                arrayOf("Value;123", "Value%3B123", true)
                )
    }

    @Test(dataProvider = "valueCases")
    fun shouldNormalizeValue(
            value: String,
            expectedValue: String,
            isCommandLineParameter: Boolean) {
        // Given
        val converter = MSBuildParameterConverterImpl()

        // When
        val actualValue = converter.normalizeValue(value, isCommandLineParameter)

        // Then
        Assert.assertEquals(actualValue, expectedValue)
    }

    @Test
    fun shouldConvert() {
        // Given
        val converter = MSBuildParameterConverterImpl()

        // When
        val actualParams = converter.convert(
                sequenceOf(MSBuildParameter("d", "   "), MSBuildParameter("d", ""), MSBuildParameter("a", "b"), MSBuildParameter("", "ñ"), MSBuildParameter("  ", "ñ")), false)
                .toList()

        // Then
        Assert.assertEquals(actualParams, listOf("/p:a=b"))
    }
}