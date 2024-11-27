package jetbrains.buildServer.dotnet.commands.test

data class TestsFilterItem(val property: String, val value: String, val operation: String) {
    val filterExpression get() = "${property}${operation}${value}"
}