package jetbrains.buildServer.dotnet

import java.io.File

private val MSBuildLoggerAssemblyName = "TeamCity.MSBuild.Logger.dll"
private val MSBuildLoggerSubDir = "msbuild"

private val VSTestLoggerAssemblyName = "TeamCity.VSTest.TestAdapter.dll"
private val VSTestSubDir = "vstest"

enum class Logger(public val relativePath: File) {
    // MSBuild loggers
    MSBuildLogger12(File("${MSBuildLoggerSubDir}12", MSBuildLoggerAssemblyName)),
    MSBuildLogger14(File("${MSBuildLoggerSubDir}14", MSBuildLoggerAssemblyName)),
    MSBuildLogger15(File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName)),

    // VSTest loggers
    VSTestLogger12(File("${VSTestSubDir}12", VSTestLoggerAssemblyName)),
    VSTestLogger14(File("${VSTestSubDir}14", VSTestLoggerAssemblyName)),
    VSTestLogger15(File("${VSTestSubDir}15", VSTestLoggerAssemblyName))
}