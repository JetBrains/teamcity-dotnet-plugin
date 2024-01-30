package jetbrains.buildServer.dotcover.command

enum class DotCoverCommandType(val commandName: String) {
    Cover("cover"),
    Merge("merge"),
    Report("report")
}