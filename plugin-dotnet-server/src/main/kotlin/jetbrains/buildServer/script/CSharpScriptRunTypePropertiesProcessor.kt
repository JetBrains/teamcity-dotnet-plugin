package jetbrains.buildServer.script

import jetbrains.buildServer.inspect.ToolVersionProvider
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor

class CSharpScriptRunTypePropertiesProcessor(
        private val _toolVersionProvider: ToolVersionProvider)
    : PropertiesProcessor {
    override fun process(properties: Map<String, String>) =
        validate(properties).toCollection(mutableListOf())

    private fun validate(properties: Map<String, String>) = sequence {
        if (properties[ScriptConstants.CLT_PATH].isNullOrBlank()) {
            yield(InvalidProperty(ScriptConstants.CLT_PATH, "The path to ${ScriptConstants.RUNNER_DESCRIPTION} must be specified"))
        }

        val scriptType = properties[ScriptConstants.SCRIPT_TYPE]?.let { ScriptType.tryParse(it) }
        if (scriptType == null) {
            yield(InvalidProperty(ScriptConstants.SCRIPT_TYPE, "Script type is not specified"))
        }

        when(scriptType) {
            ScriptType.Custom ->
                if(properties[ScriptConstants.SCRIPT_CONTENT].isNullOrBlank()) {
                    yield(InvalidProperty(ScriptConstants.SCRIPT_CONTENT, "Custom script content is not provided"))
                }

            ScriptType.File ->
                if(properties[ScriptConstants.SCRIPT_FILE].isNullOrBlank()) {
                    yield(InvalidProperty(ScriptConstants.SCRIPT_FILE, "Script file path is not specified"))
                }

            else -> { }
        }
    }
}