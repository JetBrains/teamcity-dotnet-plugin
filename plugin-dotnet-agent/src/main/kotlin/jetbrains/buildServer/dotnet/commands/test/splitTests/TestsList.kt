package jetbrains.buildServer.dotnet.commands.test.splitTests

import jetbrains.buildServer.rx.Disposable

interface TestsList : Disposable {
    val testsCount: Int
    val tests: Sequence<String>
    fun add(testName: String)
}