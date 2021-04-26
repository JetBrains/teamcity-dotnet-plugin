package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_DISPLAY_NAME

class InspectionOutputObserver(
        private val _loggerService: LoggerService)
    : OutputObserver {
    override fun onNext(value: kotlin.String) {
        if (NoFiles.equals(value.trim(), false)) {
            _loggerService.writeErrorOutput("$NoFiles If you have C++ projects in your solution, specify the x86 ReSharper CLT platform in the $RUNNER_DISPLAY_NAME build step.")
        }
    }

    override fun onError(error: Exception) { }

    override fun onComplete() { }

    companion object {
        const val NoFiles = "No files to inspect were found."
    }
}