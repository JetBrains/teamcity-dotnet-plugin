package jetbrains.buildServer.dotnet

interface LoggerParameters {
    val ParamVerbosity: Verbosity?

    val MSBuildLoggerVerbosity: Verbosity?

    val VSTestVerbosity: Verbosity
}