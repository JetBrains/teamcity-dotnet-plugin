package jetbrains.buildServer.dotnet.commands.test.splitting

enum class TestsSplittingMode {
    Disabled,             // no splitting
    TestClassNameFilter,  // use filter by test classes names
    TestNameFilter,       // use filter by test names
    Suppression;          // test suppression by TestSuppressor app

    val isFilterMode get() =
        this == TestClassNameFilter || this == TestNameFilter
}