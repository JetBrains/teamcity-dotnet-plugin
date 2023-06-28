package jetbrains.buildServer.dotnet.commands.test.splitting

enum class TestsSplittingMode {
    Disabled,             // no splitting
    TestClassNameFilter,  // use filter by test classes names
    TestNameFilter,       // use filter by test names
    Suppressing;          // test suppressing by test suppressing app

    val isEnabled get() = this != Disabled

    val isFilterMode get() =
        this == TestClassNameFilter || this == TestNameFilter

    val isSuppressingMode get() = this == Suppressing
}