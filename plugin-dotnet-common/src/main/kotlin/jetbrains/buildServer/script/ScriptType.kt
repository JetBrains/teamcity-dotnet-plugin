package jetbrains.buildServer.script

import jetbrains.buildServer.dotnet.Tool

enum class ScriptType(
        val id: String,
        val description: String) {

    Custom("customScript", "Custom Script"),
    File("file", "Script File");

    companion object {
        fun tryParse(id: String): ScriptType? {
            return ScriptType.values().singleOrNull { it.id.equals(id, true) }
        }
    }
}