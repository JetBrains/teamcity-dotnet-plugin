package jetbrains.buildServer.dotnet.commands.custom

enum class RollForwardOption(val option: String) {
    LatestPatch("LatestPatch"),
    Minor("Minor"),
    Major("Major"),
    LatestMinor("LatestMinor"),
    LatestMajor("LatestMajor"),
    Disable("Disable"),
}