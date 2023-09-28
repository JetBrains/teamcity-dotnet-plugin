package jetbrains.buildServer.inspect

enum class PluginDescriptorType(val regex: Regex) {
    // Plugin descriptors consisting of non-whitespace plugin ids like "StyleCop.StyleCop/2021.2.3" or just "StyleCop.StyleCop"
    ID(Regex("^(\\S+)$")),

    // Deprecated. Plugin descriptors that start with a source keyword followed by a whitespace(s) and the value with possible
    // whitespaces like "Download StyleCop.StyleCop" or "File C:\Program Files\rs-plugins\StyleCop.StyleCop.2021.2.3.nupkg"
    SOURCE(Regex("^(\\S+)\\s+(\\S.*)$"));
}