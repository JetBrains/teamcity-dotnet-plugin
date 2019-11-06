package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.DotnetWorkflowComposer
import jetbrains.buildServer.rx.*
import org.apache.log4j.Logger
import java.io.File

class CommandExecutionAdapter(
        private val _buildStepContext: BuildStepContext,
        private val _loggerService: LoggerService,
        private val _commandLinePresentationService: CommandLinePresentationService,
        private val _argumentsService: ArgumentsService,
        private val _virtualContext: VirtualContext)
    : CommandExecutionFactory, CommandExecution, BuildProgressLoggerAware {
    private var _eventObserver: Observer<CommandResultEvent> = emptyObserver<CommandResultEvent>()
    private var _commandLine: CommandLine = CommandLine(null, TargetType.NotApplicable, Path(""), Path(""))
    private var _blockToken: Disposable = emptyDisposable()

    override fun create(commandLine: CommandLine, eventObserver: Observer<CommandResultEvent>): CommandExecution {
        _commandLine = commandLine
        _eventObserver = eventObserver
        return this
    }

    override fun beforeProcessStarted() = Unit

    override fun processStarted(programCommandLine: String, workingDirectory: File) {
        if (!_commandLine.title.isNullOrBlank())
        {
            if (_isHiddenInBuidLog) {
                if (_virtualContext.isVirtual) {
                    _loggerService.writeStandardOutput(_commandLine.title)
                }
                else {
                    writeStandardOutput(_commandLine.title)
                }
            }
            else {
                _blockToken = _loggerService.writeBlock(_commandLine.title)
            }
        }

        if (_commandLine.description.any()) {
            writeStandardOutput(*_commandLine.description.toTypedArray())
        }

        val executableFilePresentation = _commandLinePresentationService.buildExecutablePresentation(_commandLine.executableFile)
        val argsPresentation = _commandLinePresentationService.buildArgsPresentation(_commandLine.arguments)

        writeStandardOutput(*(listOf(StdOutText("Starting: ")) + executableFilePresentation + argsPresentation).toTypedArray())
        val virtualWorkingDirectory = _virtualContext.resolvePath(_commandLine.workingDirectory.path)
        writeStandardOutput(StdOutText("in directory: "), StdOutText(virtualWorkingDirectory))
    }

    override fun processFinished(exitCode: Int) {
        _eventObserver.onNext(CommandResultExitCode(exitCode))
        _blockToken.dispose()
    }

    override fun makeProgramCommandLine(): ProgramCommandLine = ProgramCommandLineAdapter(
            _argumentsService,
            _commandLine,
            _buildStepContext.runnerContext.buildParameters.environmentVariables)

    override fun onStandardOutput(text: String) {
        _eventObserver.onNext(CommandResultOutput(text))
        writeStandardOutput(text)
    }

    override fun onErrorOutput(error: String) {
        _eventObserver.onNext(CommandResultOutput(error))
        _loggerService.writeErrorOutput(error)
    }

    override fun interruptRequested(): TerminationAction = TerminationAction.KILL_PROCESS_TREE

    override fun isCommandLineLoggingEnabled(): Boolean = false

    override fun getLogger(): BuildProgressLogger = SuppressingLogger(_buildStepContext.runnerContext.build.buildLogger, _isHiddenInBuidLog)

    private val _isHiddenInBuidLog get() = _commandLine.chain.any { it.target == TargetType.SystemDiagnostics }

    private fun writeStandardOutput(text: String) {
        if (!_isHiddenInBuidLog) {
            _loggerService.writeStandardOutput(text)
        }
        else {
            LOG.info(text)
        }
    }

    private fun writeStandardOutput(vararg text: StdOutText) {
        if (!_isHiddenInBuidLog) {
            _loggerService.writeStandardOutput(*text)
        }
        else {
            LOG.info(text.map { it.text }.joinToString(" "))
        }
    }

    class SuppressingLogger(
            private val _baseLogger: BuildProgressLogger,
            private val _isHiddenInBuidLog: Boolean):
            BuildProgressLogger by _baseLogger {

        override fun warning(message: String?) {
            LOG.warn(message)
        }

        override fun message(message: String?) {
            if (!_isHiddenInBuidLog) {
                _baseLogger.message(message)
            } else {
                LOG.info(message)
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

    companion object {
        private val LOG = Logger.getLogger(CommandExecutionAdapter::class.java)
    }
}