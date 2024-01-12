

package jetbrains.buildServer.agent.runner

interface ColorTheme {
    fun getAnsiColor(color: Color): String
}