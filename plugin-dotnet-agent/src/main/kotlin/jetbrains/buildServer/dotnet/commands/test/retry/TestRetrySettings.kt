package jetbrains.buildServer.dotnet.commands.test.retry

interface TestRetrySettings {
    val isEnabled: Boolean
    val maxFailures: Int
    val maxRetries: Int
    val reportPath: String
}