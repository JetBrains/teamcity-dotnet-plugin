

package jetbrains.buildServer.dotnet.test.dotnet.commands.test.splitting.byName

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.dotnet.commands.test.splitting.byTestName.DotnetListTestsOutputValueProcessor
import jetbrains.buildServer.dotnet.commands.test.splitting.byTestName.LangIdentifierValidator
import jetbrains.buildServer.dotnet.commands.test.splitting.byTestName.ProcessedListTestsOutput
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetListTestsOutputValueProcessorTests {
    @MockK
    private lateinit var _langIdentifierValidator: LangIdentifierValidator

    @BeforeClass
    fun beforeAll() = MockKAnnotations.init(this)

    @DataProvider
    fun testData() = arrayOf(
        arrayOf(
            " %\$ANYTHIN#*(a: \"value1\", b: \"value2\")",
            false,
            ProcessedListTestsOutput(false, "%\$ANYTHIN#*"),
        ),
        arrayOf(
            "Namespace.TestClass.TestName(a: \"value1\", b: \"value2\")",
            true,
            ProcessedListTestsOutput(true, "Namespace.TestClass.TestName"),
        ),
        arrayOf(
            "Namespace.TestClass.TestName(a:",
            true,
            ProcessedListTestsOutput(true, "Namespace.TestClass.TestName"),
        ),
        arrayOf(
            "   Namespace.TestClass.TestName(sdf4rr983rr33^ % &&* asdf)  ",
            true,
            ProcessedListTestsOutput(true, "Namespace.TestClass.TestName"),
        ),
        arrayOf(
            "Namespace.TestClass.TestName()",
            true,
            ProcessedListTestsOutput(true, "Namespace.TestClass.TestName"),
        ),
        arrayOf(
            "Namespace.TestClass.TestName(a: \"value1\", b: \"value2\"))",
            true,
            ProcessedListTestsOutput(true, "Namespace.TestClass.TestName"),
        ),
        arrayOf(
            "Namespace.TestClass.TestName)(a: \"value1\", b: \"value2\"))",
            true,
            ProcessedListTestsOutput(true, "Namespace.TestClass.TestName)"),
        ),
        arrayOf(
            "Namespace.TestClass.TestName))())))",
            true,
            ProcessedListTestsOutput(true, "Namespace.TestClass.TestName))"),
        ),
        arrayOf(
            "Namespace.TestClass.TestName(",
            true,
            ProcessedListTestsOutput(true, "Namespace.TestClass.TestName"),
        ),
        arrayOf(
            "Namespace.TestClass.TestName)",
            false,
            ProcessedListTestsOutput(false, "Namespace.TestClass.TestName)"),
        ),
        arrayOf(
            "Namespace.TestClass.TestName",
            true,
            ProcessedListTestsOutput(true, "Namespace.TestClass.TestName"),
        ),
        arrayOf(
            "\"value1\"",
            false,
            ProcessedListTestsOutput(false, "\"value1\""),
        ),
        arrayOf(
            "\"value1\",",
            false,
            ProcessedListTestsOutput(false, "\"value1\","),
        ),
        arrayOf(
            "\"value1\")",
            false,
            ProcessedListTestsOutput(false, "\"value1\")"),
        ),
        arrayOf(
            ", parameter:",
            false,
            ProcessedListTestsOutput(false, ", parameter:"),
        ),
    )

    @Test(dataProvider = "testData")
    fun `should process output of dotnet list tests operation`(outputLine: String, identifierValidatorAnswer: Boolean, expected: ProcessedListTestsOutput) {
        // assert
        every { _langIdentifierValidator.isValid(any()) } answers { identifierValidatorAnswer }
        val provessor = createInstance()

        // act
        val actual = provessor.process(outputLine)

        // assert
        Assert.assertEquals(actual, expected)
    }

    fun createInstance() = DotnetListTestsOutputValueProcessor(_langIdentifierValidator)
}