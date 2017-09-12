package jetbrains.buildServer.dotnet

import java.io.File

private val MSBuildLoggerAssemblyName = "TeamCity.MSBuild.Logger.dll"
private val MSBuildLoggerSubDir = "msbuild"

private val VSTestLoggerAssemblyName = "TeamCity.VSTest.TestAdapter.dll"
private val VSTestSubDir = "vstest"

enum class Logger(val msbuildTool: Tool, val vstestTool: Tool, val msbuildLogger: File, val vstestLogger: File) {
    // MSBuild loggers
    V15DotnetCore(Tool.MSBuild15DotnetCore, Tool.VSTest15CrossPlatform, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),
    V15Mono(Tool.MSBuild15Mono, Tool.VSTest15CrossPlatform, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),

    V12Windows(Tool.MSBuild12Windows, Tool.VSTest12Windows, File("${MSBuildLoggerSubDir}12", MSBuildLoggerAssemblyName), File("${VSTestSubDir}12", VSTestLoggerAssemblyName)),
    V12WindowsX64(Tool.MSBuild12WindowsX64, Tool.VSTest12Windows, File("${MSBuildLoggerSubDir}12", MSBuildLoggerAssemblyName), File("${VSTestSubDir}12", VSTestLoggerAssemblyName)),
    V12WindowsX86(Tool.MSBuild12WindowsX86, Tool.VSTest12Windows, File("${MSBuildLoggerSubDir}12", MSBuildLoggerAssemblyName), File("${VSTestSubDir}12", VSTestLoggerAssemblyName)),

    V14Windows(Tool.MSBuild14Windows, Tool.VSTest14Windows, File("${MSBuildLoggerSubDir}14", MSBuildLoggerAssemblyName), File("${VSTestSubDir}14", VSTestLoggerAssemblyName)),
    V14WindowsX64(Tool.MSBuild14WindowsX64, Tool.VSTest14Windows, File("${MSBuildLoggerSubDir}14", MSBuildLoggerAssemblyName), File("${VSTestSubDir}14", VSTestLoggerAssemblyName)),
    V14WindowsX86(Tool.MSBuild14WindowsX86, Tool.VSTest14Windows, File("${MSBuildLoggerSubDir}14", MSBuildLoggerAssemblyName), File("${VSTestSubDir}14", VSTestLoggerAssemblyName)),

    V15Windows(Tool.MSBuild15Windows, Tool.VSTest15Windows, File("${MSBuildLoggerSubDir}14", MSBuildLoggerAssemblyName), File("${VSTestSubDir}14", VSTestLoggerAssemblyName)),
    V15WindowsX64(Tool.MSBuild15WindowsX64, Tool.VSTest15Windows, File("${MSBuildLoggerSubDir}14", MSBuildLoggerAssemblyName), File("${VSTestSubDir}14", VSTestLoggerAssemblyName)),
    V15WindowsX86(Tool.MSBuild15WindowsX86, Tool.VSTest15Windows, File("${MSBuildLoggerSubDir}14", MSBuildLoggerAssemblyName), File("${VSTestSubDir}14", VSTestLoggerAssemblyName)),
}