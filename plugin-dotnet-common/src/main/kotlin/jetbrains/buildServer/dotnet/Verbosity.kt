

package jetbrains.buildServer.dotnet

enum class Verbosity(val id: String, val description: String) {
    Quiet("Quiet", "Quiet"),
    Minimal("Minimal", "Minimal"),
    Normal("Normal", "Normal"),
    Detailed("Detailed", "Detailed"),
    Diagnostic("Diagnostic", "Diagnostic");

    companion object {
        fun tryParse(id: String): Verbosity? {
            return Verbosity.values().singleOrNull { it.id.equals(id, true) }
        }
    }
}