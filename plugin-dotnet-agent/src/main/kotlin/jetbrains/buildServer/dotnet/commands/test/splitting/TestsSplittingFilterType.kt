

package jetbrains.buildServer.dotnet.commands.test.splitting

enum class TestsSplittingFilterType(val id: String) {
    Includes("includes"),
    Excludes("excludes"),
}