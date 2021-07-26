package jetbrains.buildServer.script

class CSharpScriptConstantsBean {
    val scriptType = ScriptConstants.SCRIPT_TYPE
    val scriptContent = ScriptConstants.SCRIPT_CONTENT
    val scriptFile = ScriptConstants.SCRIPT_FILE
    val cltPath = ScriptConstants.CLT_PATH
    /*val frameworkVersion = ScriptConstants.FRAMEWORK
    val frameworkVersions = Framework.values()*/
    val nugetPackageSources = ScriptConstants.NUGET_PACKAGE_SOURCES
    val args = ScriptConstants.ARGS
    val toolPath = ScriptConstants.TOOL_PATH

    val typeFile = ScriptType.File.id
    val typeFileDescription = ScriptType.File.description
    val typeCustom = ScriptType.Custom.id
    val typeCustomDescription = ScriptType.Custom.description

    val cltToolTypeName = ScriptConstants.CLT_TOOL_TYPE_ID
}