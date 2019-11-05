package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.rx.emptyDisposable
import java.io.File

class CommandExecutionAdapterImpl(
        private val _buildStepContext: BuildStepContext,
        private val _loggerService: LoggerService,
        private val _commandLinePresentationService: CommandLinePresentationService,
        private val _argumentsService: ArgumentsService,
        private val _virtualContext: VirtualContext)
    : CommandExecutionAdapter {
    private var _eventObserver: Observer<CommandResultEvent>? = null
    private val eventObserver: Observer<CommandResultEvent> get() = _eventObserver ?: throw RunBuildException("The instance was not initialized")
    private var _commandLine: CommandLine? = null
    private val commandLine: CommandLine get() = _commandLine ?: throw RunBuildException("The instance was not initialized")
    private val _logger = SuppressingLogger(_buildStepContext.runnerContext.build.buildLogger, _isHiddenInBuidLog)
    private var _blockToken: Disposable = emptyDisposable()

    override fun initialize(commandLine: CommandLine, eventObserver: Observer<CommandResultEvent>) {
        _commandLine = commandLine
        _eventObserver = eventObserver
    }

    override fun beforeProcessStarted() = Unit

    override fun processStarted(programCommandLine: String, workingDirectory: File) {
        if (!commandLine.title.isNullOrBlank())
        {
            if (_isHiddenInBuidLog) {
                _loggerService.writeStandardOutput(commandLine.title)
            }
            else {
                _blockToken = _loggerService.writeBlock(commandLine.title)
            }
        }

        if (commandLine.description.any()) {
            writeStandardOutput(*commandLine.description.toTypedArray())
        }

        val executableFilePresentation = _commandLinePresentationService.buildExecutablePresentation(commandLine.executableFile)
        val argsPresentation = _commandLinePresentationService.buildArgsPresentation(commandLine.arguments)

        writeStandardOutput(*(listOf(StdOutText("Starting: ")) + executableFilePresentation + argsPresentation).toTypedArray())
        val virtualWorkingDirectory = _virtualContext.resolvePath(commandLine.workingDirectory.path)
        writeStandardOutput(StdOutText("in directory: "), StdOutText(virtualWorkingDirectory))
    }

    override fun processFinished(exitCode: Int) {
        eventObserver.onNext(CommandResultExitCode(exitCode))
        _blockToken.dispose()
    }

    override fun makeProgramCommandLine(): ProgramCommandLine = ProgramCommandLineAdapter(
            _argumentsService,
            commandLine,
            _buildStepContext.runnerContext.buildParameters.environmentVariables)

    override fun onStandardOutput(text: String) {
        eventObserver.onNext(CommandResultOutput(text))
        writeStandardOutput(text)
    }

    override fun onErrorOutput(error: String) {
        eventObserver.onNext(CommandResultOutput(error))
        _loggerService.writeErrorOutput(error)
    }

    override fun interruptRequested(): TerminationAction = TerminationAction.KILL_PROCESS_TREE

    override fun isCommandLineLoggingEnabled(): Boolean = false

    override fun getLogger(): BuildProgressLogger = _logger

    private val _isHiddenInBuidLog get() = commandLine.target == TargetType.SystemDiagnostics

    private fun writeStandardOutput(text: String) {
        if (!_isHiddenInBuidLog) {
            _loggerService.writeStandardOutput(text)
        }
        else {
            _loggerService.writeTrace(text)
        }
    }

    private fun writeStandardOutput(vararg text: StdOutText) {
        if (!_isHiddenInBuidLog) {
            _loggerService.writeStandardOutput(*text)
        }
        else {
            _loggerService.writeTrace(text.map { it.text }.joinToString(" "))
        }
    }

    class SuppressingLogger(
            private val _baseLogger: BuildProgressLogger,
            private val _isHiddenInBuidLog: Boolean):
            BuildProgressLogger by _baseLogger {

        override fun warning(message: String?) {
            _baseLogger.debug(message)
        }

        override fun message(message: String?) {
            if (!_isHiddenInBuidLog) {
                _baseLogger.message(message)
            } else {
                _baseLogger.debug(message)
            }
        }
    }

    private class ProgramCommandLineAdapter(
            private val _argumentsService: ArgumentsService,
            private val _commandLine: CommandLine,
            private val _environmentVariables: Map<String, String>)
        : ProgramCommandLine {

        override fun getExecutablePath(): String = _commandLine.executableFile.path

        override fun getWorkingDirectory(): String = _commandLine.workingDirectory.path

        override fun getArguments(): MutableList<String> = _commandLine.arguments.map { _argumentsService.normalize(it.value) }.toMutableList()

        override fun getEnvironment(): MutableMap<String, String> {
            val environmentVariables = _environmentVariables.toMutableMap()
            _commandLine.environmentVariables.forEach { environmentVariables[it.name] = it.value }
            return environmentVariables
        }
    }
}