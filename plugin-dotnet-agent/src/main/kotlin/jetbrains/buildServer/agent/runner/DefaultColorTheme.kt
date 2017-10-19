package jetbrains.buildServer.agent.runner

class DefaultColorTheme : ColorTheme {
    override fun getAnsiColor(color: Color) = when (color) {
        Color.Header -> "0;1"
        Color.Success -> "32;1"
        Color.Warning -> "33;1"
        Color.Error -> "31;1"
        Color.Details -> "34"
        else -> ""
    }
}