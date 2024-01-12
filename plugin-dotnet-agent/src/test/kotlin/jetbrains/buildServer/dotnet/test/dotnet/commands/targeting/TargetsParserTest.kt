

package jetbrains.buildServer.dotnet.test.dotnet.commands.targeting

import jetbrains.buildServer.dotnet.commands.targeting.TargetsParserImpl
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class TargetsParserTest {
    @DataProvider
    fun versionCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf("", ""),
                arrayOf(" ", ""),
                arrayOf("abc", "abc"),
                arrayOf(" abc  ", "abc"),
                arrayOf("abc;xyz", "abc;xyz"),
                arrayOf("abc; xyz", "abc;xyz"),
                arrayOf(" abc  ; xyz ", "abc;xyz"),
                arrayOf("abc xyz", "abc;xyz"),
                arrayOf("abc  xyz", "abc;xyz"),
                arrayOf(" abc  xyz  ", "abc;xyz"),
                arrayOf("\"abc\"", "abc"),
                arrayOf("\"ab c\"", "ab c"),
                arrayOf("\"ab c\";\"xy z\"", "ab c;xy z"),
                arrayOf(" \"ab c\"   \"xy z\"; ", "ab c;xy z"),
                arrayOf(" \"ab c\"   xyz ", "ab c;xyz"),
                arrayOf(" \"ab c\";xyz ", "ab c;xyz")
        )
    }

    @Test(dataProvider = "versionCases")
    fun shouldParseVersion(targets: String, expected: String) {
        // Given
        val parser = TargetsParserImpl()

        // When
        val actual = parser.parse(targets)

        // Then
        Assert.assertEquals(actual, expected)
    }
}