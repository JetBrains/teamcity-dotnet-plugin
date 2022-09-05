package jetbrains.buildServer.dotnet.commands.test.splitTests

interface LangIdentifierValidator {
    fun isValid(identifier: String): Boolean
}