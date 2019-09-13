package jetbrains.buildServer.agent

import jetbrains.buildServer.rx.Observable
import jetbrains.buildServer.rx.emptyObservable

data class CommandLineResult(
        private val _exitCode: Sequence<Int>,
        val standardOutput: Sequence<String>,
        val errorOutput: Sequence<String>) {

    val exitCode: Int
        get() = _exitCode.single()
}