/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet.test.dotnet.commands.test.splitTests

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.dotnet.commands.test.splitTests.DotnetListTestsOutputValueProcessor
import jetbrains.buildServer.dotnet.commands.test.splitTests.LangIdentifierValidator
import jetbrains.buildServer.dotnet.commands.test.splitTests.ProcessedListTestsOutput
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