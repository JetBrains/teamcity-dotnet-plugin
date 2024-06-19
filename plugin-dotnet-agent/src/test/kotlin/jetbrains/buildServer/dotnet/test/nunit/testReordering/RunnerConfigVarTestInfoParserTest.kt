package jetbrains.buildServer.dotnet.test.nunit.testReordering

import jetbrains.buildServer.nunit.testReordering.RunnerConfigVarTestInfoParser
import jetbrains.buildServer.nunit.testReordering.TestInfo
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class RunnerConfigVarTestInfoParserTest {

    private val separator: String = System.lineSeparator()

    data class TestCase(val text: String, val expectedTests: List<TestInfo>)

    @DataProvider(name = "parseTestsFromStringCases")
    fun getParseTestsFromStringCases(): Array<TestCase> = arrayOf(
        TestCase(
            text = """
                HelloWorld.Tests.NUnit2.UnitTest1${separator}HelloWorld.Tests.NUnit2.UnitTest2
                HelloWorld.Tests.UnitTest1${separator}
                """.trimIndent(),
            expectedTests = listOf(
                TestInfo("HelloWorld.Tests.NUnit2.UnitTest1"),
                TestInfo("HelloWorld.Tests.NUnit2.UnitTest2"),
                TestInfo("HelloWorld.Tests.UnitTest1")
            )
        ),
        TestCase(
            text = """
                HelloWorld.Tests.NUnit2.UnitTest1
                HelloWorld.Tests.NUnit2.UnitTest2${separator}HelloWorld.Tests.UnitTest1
                """.trimIndent(),
            expectedTests = listOf(
                TestInfo("HelloWorld.Tests.NUnit2.UnitTest1"),
                TestInfo("HelloWorld.Tests.NUnit2.UnitTest2"),
                TestInfo("HelloWorld.Tests.UnitTest1")
            )
        ),
        TestCase(text = "", expectedTests = emptyList()),
        TestCase(
            text = """
                c:\abc.dll  : HelloWorld.Tests.NUnit2.UnitTest1
                HelloWorld.Tests.NUnit2.UnitTest2${separator}xyz:HelloWorld.Tests.UnitTest1
                """.trimIndent(),
            expectedTests = listOf(
                TestInfo(File("c:\\abc.dll"), "HelloWorld.Tests.NUnit2.UnitTest1", null),
                TestInfo("HelloWorld.Tests.NUnit2.UnitTest2"),
                TestInfo(
                    File("xyz"), "HelloWorld.Tests.UnitTest1", null
                )
            )
        ),
        TestCase(text = "a:c", expectedTests = listOf(TestInfo(File("a"), "c", null))),
        TestCase(text = ":a:a:c", expectedTests = listOf(TestInfo(File(":a:a"), "c", null))),
        TestCase(text = ":c", expectedTests = listOf(TestInfo(":c"))),
        TestCase(text = "c:", expectedTests = listOf(TestInfo("c:"))),
        TestCase(text = ":", expectedTests = listOf(TestInfo(":"))),
    )


    @Test(dataProvider = "parseTestsFromStringCases")
    fun `should parse tests from string`(testCase: TestCase) {
        // arrange
        val parser = RunnerConfigVarTestInfoParser()

        // act
        val tests = parser.parse(testCase.text)

        // assert
        Assert.assertEquals(tests, testCase.expectedTests)
    }
}