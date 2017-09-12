package jetbrains.buildServer.agent.runner

class DefaultColorTheme : ColorTheme {
    override fun getAnsiColor(color: Color): String {
        when(color) {
            Color.Success -> return "32;1"
            Color.Warning -> return "33;1"
            Color.Error -> return "31;1"
            Color.Details -> return "34"
            else -> return ""
        }
    }
}