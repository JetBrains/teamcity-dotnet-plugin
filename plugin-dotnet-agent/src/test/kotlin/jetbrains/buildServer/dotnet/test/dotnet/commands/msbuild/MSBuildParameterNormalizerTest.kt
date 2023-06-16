package jetbrains.buildServer.dotnet.test.dotnet.commands.msbuild

import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameterNormalizer
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MSBuildParameterNormalizerTest {
    @DataProvider
    fun nameCases(): Array<Array<String>> {
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
            arrayOf("name?", "name_")
        )
    }

    @Test(dataProvider = "nameCases")
    fun shouldNormalizeName(name: String, expectedName: String) {
        // Given
        val normalizer = MSBuildParameterNormalizer

        // When
        val actualName = normalizer.normalizeName(name)

        // Then
        Assert.assertEquals(expectedName, actualName)
    }

    @DataProvider
    fun valueCases(): Array<Array<Any>> {
        return arrayOf(
            // escaped
            arrayOf("value 123", false, "\"value 123\"", "value 123"),
            arrayOf("value123\\", false, "value123\\\\", "value123\\\\"),
            arrayOf("value 123\\", false, "\"value 123\\\\\"", "value 123\\\\"),
            arrayOf("value 123\\ ", false, "\"value 123\\ \"", "value 123\\ "),
            arrayOf("value123\\a", false, "value123\\a", "value123\\a"),
            arrayOf("value 123\\a", false, "\"value 123\\a\"", "value 123\\a"),
            arrayOf("value 123  \\a", false, "\"value 123  \\a\"", "value 123  \\a"),
            arrayOf("value \" 123", false, "\"value %22 123\"", "value %22 123"),
            arrayOf("value \"\" 123", false, "\"value %22%22 123\"", "value %22%22 123"),
            arrayOf("value \" \" 123", false, "\"value %22 %22 123\"", "value %22 %22 123"),
            arrayOf("value1 \n value2", false, "\"value1 %0A value2\"", "value1 %0A value2"),
            arrayOf("value1\rvalue2", false, "\"value1%0Dvalue2\"", "value1%0Dvalue2"),
            arrayOf("value1 \t value2", false, "\"value1 %09 value2\"", "value1 %09 value2"),
            arrayOf("value1 \b value2", false, "\"value1 %08 value2\"", "value1 %08 value2"),
            arrayOf("value1 , value2", false, "\"value1 , value2\"", "value1 , value2"),
            arrayOf("value1 , value2", true, "\"value1 %2C value2\"", "value1 %2C value2"),

            // should not escape `;` for response files and should wrap a parameter by double quotes
            // in this case https://github.com/JetBrains/teamcity-dotnet-plugin/issues/144
            arrayOf("Value;123", false, "\"Value;123\"", "Value;123"),
            arrayOf(
                "!@#\$%^&*()_+~1234-=/;'][{}\":<>,.?/??~`",
                false,
                "\"!@#$%^&*()_+~1234-=/;'][{}%22:<>,.?/??~`\"",
                "!@#$%^&*()_+~1234-=/;'][{}%22:<>,.?/??~`"
            ),

            // should not escape digits and letters when full escaping is on (e.g. predefined params)
            arrayOf("Value;123", true, "\"Value%3B123\"", "Value%3B123"),
            arrayOf("Value,123", true, "\"Value%2C123\"", "Value%2C123"),
            arrayOf(
                "!@#\$%^&*()_+~1234-=/;'][{}\":<>,.?/~`",
                true,
                "\"%21%40%23%24%%5E%26%2A()_%2B%7E1234-%3D/%3B%27%5D%5B%7B%7D%22:%3C%3E%2C.%3F/%7E%60\"",
                "%21%40%23%24%%5E%26%2A()_%2B%7E1234-%3D/%3B%27%5D%5B%7B%7D%22:%3C%3E%2C.%3F/%7E%60"
            )
        )
    }

    @Test(dataProvider = "valueCases")
    fun `should normalize and quote value`(
        value: String,
        fullEscaping: Boolean,
        expectedWithQuoting: String,
        expectedWithoutQuoting: String
    ) {
        // arrange
        val normalizer = MSBuildParameterNormalizer

        // act
        val actualWithQuoting = normalizer.normalizeAndQuoteValue(value, fullEscaping, false)
        val actualWithoutQuoting = normalizer.normalizeValue(value, fullEscaping)

        // assert
        Assert.assertEquals(actualWithQuoting, expectedWithQuoting)
        Assert.assertEquals(actualWithoutQuoting, expectedWithoutQuoting)
    }

    @DataProvider
    fun casesForTrailingSlashQuotation(): Array<Array<Any>> = arrayOf(
        // cases with special characters - quoting regardless of trailing slashes and quoteTrailingBackslash switch
        arrayOf("value 123\\", false, "\"value 123\\\\\""),
        arrayOf("value 123\\", true, "\"value 123\\\\\""),
        arrayOf("value 123", false, "\"value 123\""),
        arrayOf("value 123", true, "\"value 123\""),

        // cases without special characters - quoting only when switch is on (e.g. for Windows)
        // and only if there's one trailing slash
        arrayOf("value123\\", false, "value123\\\\"),
        arrayOf("value123\\", true, "\"value123\\\\\""),
        arrayOf("value123\\\\", true, "value123\\\\\\"),
        arrayOf("value123\\\\\\", true, "value123\\\\\\\\"),
        arrayOf("\\value123", true, "\\value123"),
        arrayOf("value\\123", true, "value\\123"),
        arrayOf("value\\123\\", true, "\"value\\123\\\\\""),
    )

    @Test(dataProvider = "casesForTrailingSlashQuotation")
    fun `should normalize value when escaping trailing backslash`(
        value: String,
        quoteTrailingBackslash: Boolean,
        expectedValue: String
    ) {
        // arrange
        val normalizer = MSBuildParameterNormalizer

        // act
        val actualValue = normalizer.normalizeAndQuoteValue(value, false, quoteTrailingBackslash)

        // assert
        Assert.assertEquals(actualValue, expectedValue)
    }
}