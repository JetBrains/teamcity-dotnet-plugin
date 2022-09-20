package jetbrains.buildServer.dotnet.commands.test.splitTests

interface SplitTestsNamesSessionManager {
    fun startSession() : SplitTestsNamesSession
}

