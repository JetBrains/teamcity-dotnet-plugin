package jetbrains.buildServer.dotnet.commands.test.splitTests

import java.util.regex.Pattern

class CSharpTestNameValidator : TestNameValidator {
    private val _fullyQualifiedNameRegex =
        // can't use kotlin.text.Regex() because
        // there is no way to set UNICODE_CHARACTER_CLASS flag for some reason
        // even it is said in docs that it always set – it's not working
        Pattern.compile(
            "^([\\w&&\\D]\\w+\\.{1})+([\\w&&\\D]\\w+)\$",
            Pattern.MULTILINE or Pattern.UNICODE_CHARACTER_CLASS,
        ).toRegex()
    
    override fun isValid(testName: String) = _fullyQualifiedNameRegex.matches(testName)
}