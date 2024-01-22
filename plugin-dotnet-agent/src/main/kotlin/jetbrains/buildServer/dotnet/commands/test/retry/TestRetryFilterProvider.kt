package jetbrains.buildServer.dotnet.commands.test.retry

import jetbrains.buildServer.dotnet.commands.test.TestsFilterProvider
import jetbrains.buildServer.rx.Disposable

interface TestRetryFilterProvider : TestsFilterProvider {
    fun setTestNames(tests: List<String>): Disposable
}

