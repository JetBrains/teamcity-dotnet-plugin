package jetbrains.buildServer.script

object ScriptConstants {
    const val RUNNER_TYPE = "csharpScript"
    const val RUNNER_DISPLAY_NAME = "C# Script"
    const val RUNNER_DESCRIPTION = "C# Script runner"

    const val SCRIPT_TYPE = "scriptType"
    const val SCRIPT_CONTENT = "scriptContent"
    const val SCRIPT_FILE = "scriptFile"
    const val ARGS = "scriptArgs"
    const val CLT_PATH = "csharpToolPath"
    const val FRAMEWORK = "framework"
    const val NUGET_PACKAGE_SOURCES = "nuget.packageSources"
    const val TOOL_PATH = "scriptToolPath"

    const val CLT_TOOL_TYPE_ID = "TeamCity.csi"
    const val CLT_TOOL_TYPE_NAME = "C# script"

    const val RUNNER_ENABLED = "teamcity.internal.csharp.script"
}