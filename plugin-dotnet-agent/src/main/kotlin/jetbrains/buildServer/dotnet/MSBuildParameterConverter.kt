package jetbrains.buildServer.dotnet

interface MSBuildParameterConverter {
    fun convert(parameters: Sequence<MSBuildParameter>): Sequence<String>

    fun normalizeName(name: String): String

    fun normalizeValue(value: String): String
}