package jetbrains.buildServer.agent

import jetbrains.buildServer.rx.Observable
import jetbrains.buildServer.rx.emptyObservable

data class CommandLineResult(
        val exitCode: Int,
        val standardOutput: Collection<String>,
        val errorOutput: Collection<String>) {
}