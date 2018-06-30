package jetbrains.buildServer.dotnet.test.dotcover

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.runner.Converter
import jetbrains.buildServer.dotcover.CoverageFilter
import jetbrains.buildServer.dotcover.DotCoverFilterConverterImpl
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverFilterConverterTest {
    @DataProvider(name = "filterCases")
    fun getFilterCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("+:aaa", "+:aaa;module=*;class=*;function=*", false),
                arrayOf("+:aaa;module=bbb", "+:aaa;module=bbb;class=*;function=*", false),
                arrayOf("-:bbb", "-:bbb;module=*;class=*;function=*", false),
                arrayOf("", "", false),
                arrayOf("+:aaa$ourLineSeparator-:module=bbb", "+:aaa;module=*;class=*;function=*$ourLineSeparator-:*;module=bbb;class=*;function=*", false),
                arrayOf("+:module=aaa$ourLineSeparator  $ourLineSeparator-:module=bbb", "+:*;module=aaa;class=*;function=*$ourLineSeparator-:*;module=bbb;class=*;function=*", false),
                arrayOf("+aaa$ourLineSeparator-:bbb", "", true),
                arrayOf("$ourLineSeparator-:bbb", "-:bbb;module=*;class=*;function=*", false),
                arrayOf("+:$ourLineSeparator-:bbb", "", true),
                arrayOf("+$ourLineSeparator-:bbb", "", true),
                arrayOf("?$ourLineSeparator-:bbb", "", true),
                arrayOf("+:aaa;-:bbb", "", true),
                arrayOf("+:aaa,-:bbb", "", true),
                arrayOf("+:aaa*$ourLineSeparator-:b*bb", "+:aaa*;module=*;class=*;function=*$ourLineSeparator-:b*bb;module=*;class=*;function=*", false),
                arrayOf("+:aaa*.dll$ourLineSeparator-:b*bb", "+:aaa*.dll;module=*;class=*;function=*$ourLineSeparator-:b*bb;module=*;class=*;function=*", false),
                arrayOf("-:module=aaa;bbb", "-:bbb;module=aaa;class=*;function=*", false),
                arrayOf("-:module=aaa;class=bbb", "-:*;module=aaa;class=bbb;function=*", false),
                arrayOf("-:module=aaa;class=bbb;ccc", "-:ccc;module=aaa;class=bbb;function=*", false),
                arrayOf("-:class=bbb;module=aaa", "-:*;module=aaa;class=bbb;function=*", false),
                arrayOf("-:module=aaa;class=bbb;function=ccc", "-:*;module=aaa;class=bbb;function=ccc", false),
                arrayOf("-:function=ccc;class=bbb;module=aaa", "-:*;module=aaa;class=bbb;function=ccc", false),
                arrayOf("-:function=ccc;class=bbb", "-:*;module=*;class=bbb;function=ccc", false),
                arrayOf("-:function=ccc;class=bbb;aaa", "-:aaa;module=*;class=bbb;function=ccc", false),
                arrayOf("-:ccc;class=bbb;module=aaa", "-:ccc;module=aaa;class=bbb;function=*", false),
                arrayOf("-:Assembly=aaa;Class=bbb;Function=ccc", "-:*;module=aaa;class=bbb;function=ccc", false),
                arrayOf("-:Assembly=aaa;Type=bbb;Function=ccc", "-:*;module=aaa;class=bbb;function=ccc", false),
                arrayOf("-:Assembly=aaa;Type=bbb;METHOD=ccc", "-:*;module=aaa;class=bbb;function=ccc", false),
                arrayOf("-:Assembly=aaa;Attribute=bbb;METHOD=ccc", "-:*;module=aaa;class=bbb;function=ccc", false),
                arrayOf("-:Assembly=aaa;AttributeName=bbb;METHOD=ccc", "-:*;module=aaa;class=bbb;function=ccc", false),
                arrayOf("-:function=ccc;bbb;aaa", "", true),
                arrayOf("?:aaa", "", true),
                arrayOf("aaa", "", true),
                arrayOf("+:aaa$ourLineSeparator-:bb:b", "", true))
    }

    @Test(dataProvider = "filterCases")
    fun shouldParseFilter(filterStr: String, expectedFiltersStr: String, expectedRunBuildException: Boolean) {
        // Given
        val instance = createInstance()
        var actualThrownRunBuildException = false
        var actualFilters = emptyList<CoverageFilter>()

        // When
        try {
            actualFilters = instance.convert(filterStr).toList()
        } catch (ex: RunBuildException) {
            actualThrownRunBuildException = true
        }

        // Then
        Assert.assertEquals(actualThrownRunBuildException, expectedRunBuildException)
        if (!actualThrownRunBuildException) {
            Assert.assertEquals(actualFilters.joinToString(ourLineSeparator) { it.toString() }, expectedFiltersStr)
        }
    }

    private fun createInstance(): Converter<String, Sequence<CoverageFilter>> {
        return DotCoverFilterConverterImpl()
    }

    companion object {
        private val ourLineSeparator = System.getProperty("line.separator")
    }
}