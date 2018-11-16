package jetbrains.buildServer.dotnet

interface LoggerParameters {
    val paramVerbosity: Verbosity?

    val msBuildLoggerVerbosity: Verbosity?

    val vsTestVerbosity: Verbosity

    val msBuildParameters: String
}