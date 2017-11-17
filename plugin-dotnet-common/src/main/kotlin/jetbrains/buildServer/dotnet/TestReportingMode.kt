package jetbrains.buildServer.dotnet

enum class TestReportingMode(val id: String) {
    On("on"),
    Off("off");

    companion object {
        fun tryParse(id: String): TestReportingMode? {
            return TestReportingMode.values().singleOrNull { it.id.equals(id, true) }
        }
    }
}