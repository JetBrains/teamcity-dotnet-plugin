package jetbrains.buildServer.dotnet

enum class DotnetCommandType(val id: String) {
    Build("build"),
    Pack("pack"),
    Publish("publish"),
    Restore("restore"),
    Test("test"),
    Run("run"),
    NuGetPush("nuget-push"),
    NuGetDelete("nuget-delete"),
    Clean("clean"),
    MSBuild("msbuild"),
    VSTest("vstest"),
    VisualStudio("devenv")
}