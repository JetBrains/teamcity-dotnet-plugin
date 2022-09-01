package jetbrains.buildServer.dotnet.commands.test.splitTests

import java.util.regex.Pattern

class CSharpTestNameValidator : TestNameValidator {
    private val _fullyQualifiedNameRegex =
        Pattern.compile(
            "^([\\w&&\\D]\\w+\\.{1})+([\\w&&\\D]\\w+)\$",
            Pattern.MULTILINE or Pattern.UNICODE_CHARACTER_CLASS
        ).toRegex()
    
    override fun isValid(testName: String) = _fullyQualifiedNameRegex.matches(testName)
}