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

package jetbrains.buildServer.dotnet.commands.test.splitTests

import java.util.regex.Pattern

class DotnetListTestsOutputValueProcessor(
    private val langIdentifierValidator: LangIdentifierValidator
) : ListTestsOutputValueProcessor {
    // just cutting-off last segment wrapped in parentheses to get rid of parameters in string
    // e.g. `Namespace.TestClass.TestName(a: "value1, b: "value2")`     --> `Namespace.TestClass.TestName`
    // or   `Namespace.TestClass.TestName(a:`                           --> `Namespace.TestClass.TestName`
    private val _regex = Pattern.compile(
        "(\\(.*)$",
        Pattern.MULTILINE or Pattern.UNICODE_CHARACTER_CLASS,
    ).toRegex()

    override fun process(output: String) =
        output.trim()
            .let { _regex.replace(it, "") }
            .let { processedValue ->
                ProcessedListTestsOutput(
                    isValidIdentifier = langIdentifierValidator.isValid(processedValue),
                    value = processedValue
                )
            }
}