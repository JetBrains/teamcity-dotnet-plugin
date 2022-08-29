package jetbrains.buildServer.dotnet.commands.test.splitTests

import jetbrains.buildServer.rx.Disposable

interface SplitTestsNamesSessionManager {
    fun getSession() : SplitTestsNamesSession
}

interface SplitTestsNamesSession : Disposable {
    val chunksCount: Int
    fun getSaver(): SplitTestsNamesSaver
}

interface SplitTestsNamesSaver : Disposable {
    fun save(testName: String)
}

interface SplitTestsNamesReader {
    fun read(): Sequence<String>
}
