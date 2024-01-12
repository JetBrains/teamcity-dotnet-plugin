

package jetbrains.buildServer.dotnet

import java.util.*

enum class TestReportingMode(val id: String) {
    On("On"),
    MultiAdapterPath("MultiAdapterPath"),
    MultiAdapterPath_5_0_103("MultiAdapterPath_5_0_103"),
    Off("Off");

    companion object {
        fun parse(text: String): EnumSet<TestReportingMode> {
            val modes = text.splitToSequence('|').map { id ->
                TestReportingMode.values().singleOrNull { it.id.equals(id.trim(), true) }
            }.toList()

            if (modes.filter { it == null }.any()) {
                return EnumSet.noneOf<TestReportingMode>(TestReportingMode::class.java)
            }

            return EnumSet.copyOf<TestReportingMode>(modes);
        }
    }
}