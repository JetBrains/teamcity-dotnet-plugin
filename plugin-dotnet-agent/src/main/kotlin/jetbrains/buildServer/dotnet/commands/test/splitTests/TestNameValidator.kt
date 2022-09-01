package jetbrains.buildServer.dotnet.commands.test.splitTests

interface TestNameValidator {
    fun isValid(testName: String): Boolean
}