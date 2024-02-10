package jetbrains.buildServer.dotcover

enum class DotCoverMode(val id: String) {
    Wrapper("wrapper"),
    Runner("runner");

    companion object {
        fun fromString(value: String) =
            values().firstOrNull { it.id.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("No DotCoverMode enum constant for value '$value'")
    }
}
