package jetbrains.buildServer.dotnet

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.BuildProblemTypes.TC_ERROR_MESSAGE_TYPE
import jetbrains.buildServer.agent.CommandResultAttribute
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.CommandResultOutput
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.StdOutText
import jetbrains.buildServer.rx.Observer
import java.lang.Integer.min

class NugetResultsObserver(
        private val _loggerService: LoggerService)
    : Observer<CommandResultEvent> {
    override fun onNext(value: CommandResultEvent) {
        when(value) {
            is CommandResultOutput -> {
                tryParseByPrefix(value, ErrorPrefix)?.let {
                    value.attributes.add(CommandResultAttribute.Suppressed)
                    if (it.isBlank() || it.startsWith(' ')) {
                        _loggerService.writeErrorOutput(it)
                    } else {
                        _loggerService.writeBuildProblem(it, TC_ERROR_MESSAGE_TYPE, it)
                    }
                }

                tryParseByPrefix(value, WarningPrefix)?.let {
                    value.attributes.add(CommandResultAttribute.Suppressed)
                    _loggerService.writeWarning(it)
                }
            }
        }
    }

    override fun onError(error: Exception) = Unit

    override fun onComplete() = Unit

    private fun tryParseByPrefix(event: CommandResultOutput, prefix: String): String? =
        when {
            event.output.startsWith(prefix) && event.output.length > prefix.length -> event.output.substring(prefix.length, event.output.length)
            else -> null
        }

    companion object {
        internal val ErrorPrefix = "error: "
        internal val WarningPrefix = "warn: "
    }
}