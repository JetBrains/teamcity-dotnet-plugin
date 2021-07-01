package jetbrains.buildServer.script

import jetbrains.buildServer.dotnet.TargetDotNetFramework

class CSharpScriptConstantsBean {
    val scriptType = ScriptConstants.SCRIPT_TYPE
    val scriptContent = ScriptConstants.SCRIPT_CONTENT
    val scriptFile = ScriptConstants.SCRIPT_FILE
    val toolArgs = ScriptConstants.TOOL_ARGS
    val toolPath = ScriptConstants.TOOL_PATH

    val typeFile = ScriptTypes.FILE
    val typeCustom = ScriptTypes.CUSTOM
}