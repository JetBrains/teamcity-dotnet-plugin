package jetbrains.buildServer.dotnet.commands.test.splitTests

import jetbrains.buildServer.rx.Disposable

interface SplitTestsNamesSession : Disposable {
    val chunksCount: Int
    fun getSaver(): SplitTestsNamesSaver
}