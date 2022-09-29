package jetbrains.buildServer.dotnet.commands.test

interface TestsFilterProvider {
    val filterExpression: String
}