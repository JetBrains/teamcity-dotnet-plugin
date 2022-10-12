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