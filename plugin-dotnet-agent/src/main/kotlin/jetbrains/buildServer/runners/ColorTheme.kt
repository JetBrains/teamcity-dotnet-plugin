package jetbrains.buildServer.runners

interface ColorTheme {
    fun getAnsiColor(color: Color): String
}