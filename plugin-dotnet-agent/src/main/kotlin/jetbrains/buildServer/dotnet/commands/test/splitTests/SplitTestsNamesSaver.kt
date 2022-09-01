package jetbrains.buildServer.dotnet.commands.test.splitTests

import jetbrains.buildServer.rx.Disposable

interface SplitTestsNamesSaver : Disposable {
    fun tryToSave(testName: String)
}