package jetbrains.buildServer.agent

import jetbrains.buildServer.rx.Observer

class CommandLineOutputAccumulationObserver() : Observer<CommandResultEvent> {

    private val _output: StringBuilder = StringBuilder()

    val output: String
        get() = _output.toString().trim()

    override fun onNext(value: CommandResultEvent) {
        when (value) {
            is CommandResultOutput ->
            _output.append(value.output).append(System.lineSeparator())
        }
    }

    override fun onError(error: Exception) = Unit

    override fun onComplete() = Unit
}