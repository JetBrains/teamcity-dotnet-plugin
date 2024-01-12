

package jetbrains.buildServer.dotnet.commands.test.splitting.byTestName

import jetbrains.buildServer.rx.Disposable

interface TestsList : Disposable {
    val testsCount: Int
    val tests: Sequence<String>
    fun add(testName: String)
}