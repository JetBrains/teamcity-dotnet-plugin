package jetbrains.buildServer.dotnet.commands.test.splitTests

import jetbrains.buildServer.rx.Disposable

interface SplitTestsNamesSessionManager {
    fun startSession() : SplitTestsNamesSession
}

interface SplitTestsNamesSession : Disposable {
    val chunksCount: Int
    fun getSaver(): SplitTestsNamesSaver
}

interface SplitTestsNamesSaver : Disposable {
    fun tryToSave(testName: String)
}

interface SplitTestsNamesReader {
    fun read(): Sequence<String>
}
