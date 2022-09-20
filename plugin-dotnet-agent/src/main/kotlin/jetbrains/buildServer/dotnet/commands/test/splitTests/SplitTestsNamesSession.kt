package jetbrains.buildServer.dotnet.commands.test.splitTests

import jetbrains.buildServer.rx.Disposable

interface SplitTestsNamesSession : SplitTestsNamesSaver, SplitTestsNamesReader, Disposable {
     fun <T> forEveryTestsNamesChunk(handleChunk: () -> T): Sequence<T>
}