

package jetbrains.buildServer.dotnet

enum class ToolBitness(val description: String) {
    Any("Any"),
    X64("x64"),
    X86("x86");

    companion object {
        fun tryParse(id: String): ToolBitness? {
            return ToolBitness.values().singleOrNull { it.name.equals(id, true) }
        }
    }
}