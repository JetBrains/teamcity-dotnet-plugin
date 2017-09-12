package jetbrains.buildServer.dotnet

import org.springframework.context.annotation.Description

enum class Verbosity(val id: String, val description: String) {
    Quiet("Quiet", "Quiet"),
    Minimal("Minimal", "Minimal"),
    Normal("Normal", "Normal"),
    Detailed("Detailed", "Detailed"),
    Diagnostic("Diagnostic", "Diagnostic");

    companion object {
        public fun tryParse(id: String): Verbosity? {
            return Verbosity.values().filter { it.id.equals(id, true) }.singleOrNull()
        }
    }
}