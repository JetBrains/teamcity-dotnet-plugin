

package jetbrains.buildServer.dotnet.logging

import jetbrains.buildServer.dotnet.*
import java.io.File

private const val MSBuildLoggerAssemblyName = "TeamCity.MSBuild.Logger.dll"
private const val MSBuildLoggerSubDir = "msbuild"

private const val VSTestLoggerAssemblyName = "TeamCity.VSTest.TestAdapter.dll"
private const val VSTestSubDir = "vstest"

enum class Logger(val msbuildTool: Tool, val vstestTool: Tool, val msbuildLogger: File, val vstestLogger: File) {
    // MSBuild loggers
    DotnetCore(Tool.MSBuildCrossPlatform, Tool.VSTestCrossPlatform, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),
    Mono(Tool.MSBuildMono, Tool.VSTestCrossPlatform, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),

    V10Windows(Tool.MSBuild4Windows, Tool.VSTest12Windows, File("${MSBuildLoggerSubDir}12", MSBuildLoggerAssemblyName), File("${VSTestSubDir}12", VSTestLoggerAssemblyName)),
    V10WindowsX64(Tool.MSBuild4WindowsX64, Tool.VSTest12Windows, File("${MSBuildLoggerSubDir}12", MSBuildLoggerAssemblyName), File("${VSTestSubDir}12", VSTestLoggerAssemblyName)),
    V10WindowsX86(Tool.MSBuild4WindowsX86, Tool.VSTest12Windows, File("${MSBuildLoggerSubDir}12", MSBuildLoggerAssemblyName), File("${VSTestSubDir}12", VSTestLoggerAssemblyName)),

    V12Windows(Tool.MSBuild12Windows, Tool.VSTest12Windows, File("${MSBuildLoggerSubDir}12", MSBuildLoggerAssemblyName), File("${VSTestSubDir}12", VSTestLoggerAssemblyName)),
    V12WindowsX64(Tool.MSBuild12WindowsX64, Tool.VSTest12Windows, File("${MSBuildLoggerSubDir}12", MSBuildLoggerAssemblyName), File("${VSTestSubDir}12", VSTestLoggerAssemblyName)),
    V12WindowsX86(Tool.MSBuild12WindowsX86, Tool.VSTest12Windows, File("${MSBuildLoggerSubDir}12", MSBuildLoggerAssemblyName), File("${VSTestSubDir}12", VSTestLoggerAssemblyName)),

    V14Windows(Tool.MSBuild14Windows, Tool.VSTest14Windows, File("${MSBuildLoggerSubDir}14", MSBuildLoggerAssemblyName), File("${VSTestSubDir}14", VSTestLoggerAssemblyName)),
    V14WindowsX64(Tool.MSBuild14WindowsX64, Tool.VSTest14Windows, File("${MSBuildLoggerSubDir}14", MSBuildLoggerAssemblyName), File("${VSTestSubDir}14", VSTestLoggerAssemblyName)),
    V14WindowsX86(Tool.MSBuild14WindowsX86, Tool.VSTest14Windows, File("${MSBuildLoggerSubDir}14", MSBuildLoggerAssemblyName), File("${VSTestSubDir}14", VSTestLoggerAssemblyName)),

    V15Windows(Tool.MSBuild15Windows, Tool.VSTest15Windows, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),
    V15WindowsX64(Tool.MSBuild15WindowsX64, Tool.VSTest15Windows, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),
    V15WindowsX86(Tool.MSBuild15WindowsX86, Tool.VSTest15Windows, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),

    V16Windows(Tool.MSBuild16Windows, Tool.VSTest16Windows, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),
    V16WindowsX64(Tool.MSBuild16WindowsX64, Tool.VSTest16Windows, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),
    V16WindowsX86(Tool.MSBuild16WindowsX86, Tool.VSTest16Windows, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),

    V17Windows(Tool.MSBuild17Windows, Tool.VSTest17Windows, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),
    V17WindowsX64(Tool.MSBuild17WindowsX64, Tool.VSTest17Windows, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),
    V17WindowsX86(Tool.MSBuild17WindowsX86, Tool.VSTest17Windows, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),

    V18Windows(Tool.MSBuild18Windows, Tool.VSTest18Windows, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),
    V18WindowsX64(Tool.MSBuild18WindowsX64, Tool.VSTest18Windows, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),
    V18WindowsX86(Tool.MSBuild18WindowsX86, Tool.VSTest18Windows, File("${MSBuildLoggerSubDir}15", MSBuildLoggerAssemblyName), File("${VSTestSubDir}15", VSTestLoggerAssemblyName)),
}