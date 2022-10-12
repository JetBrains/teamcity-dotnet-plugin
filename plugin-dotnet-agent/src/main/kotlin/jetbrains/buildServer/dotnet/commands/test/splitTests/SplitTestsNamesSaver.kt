package jetbrains.buildServer.dotnet.commands.test.splitTests

interface SplitTestsNamesSaver {
    fun tryToSave(presumablyTestNameLine: String)
}