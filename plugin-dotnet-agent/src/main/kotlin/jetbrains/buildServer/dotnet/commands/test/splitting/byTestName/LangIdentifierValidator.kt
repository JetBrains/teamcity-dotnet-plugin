

package jetbrains.buildServer.dotnet.commands.test.splitting.byTestName

interface LangIdentifierValidator {
    fun isValid(identifier: String): Boolean
}