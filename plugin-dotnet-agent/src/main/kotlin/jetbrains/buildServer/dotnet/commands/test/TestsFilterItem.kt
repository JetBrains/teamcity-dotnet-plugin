package jetbrains.buildServer.dotnet.commands.test

data class TestsFilterItem(val property: Property, val operation: Operation, val value: String) {
    val filterExpression get() = "${property.value}${operation.value}${value}"

    enum class Property(val value: String) {
        FullyQualifiedName("FullyQualifiedName")
    }

    enum class Operation(val value: String) {
        Equals("="),
        NotEquals("!="),
        Contains("~"),
        NotContains("!~")
    }
}