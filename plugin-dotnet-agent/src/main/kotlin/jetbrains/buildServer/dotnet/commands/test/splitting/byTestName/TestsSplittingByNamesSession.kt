

package jetbrains.buildServer.dotnet.commands.test.splitting.byTestName

import jetbrains.buildServer.rx.Disposable

interface TestsSplittingByNamesSession : TestsSplittingByNamesSaver, TestsSplittingByNamesReader, Disposable {
     fun <T> forEveryTestsNamesChunk(handleChunk: () -> T): Sequence<T>
}