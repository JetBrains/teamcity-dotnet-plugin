package jetbrains.buildServer.dotnet

enum class DotnetCommandType(
        val id: String,
        val args: Sequence<String>) {
    Build("build", sequenceOf("build")),
    Pack("pack", sequenceOf("pack")),
    Publish("publish", sequenceOf("publish")),
    Restore("restore", sequenceOf("restore")),
    Test("test", sequenceOf("test")),
    Run("run", sequenceOf("run")),
    NuGetPush("nuget push", sequenceOf("nuget", "push")),
    NuGetDelete("nuget delete", sequenceOf("nuget", "delete")),
    Clean("clean", sequenceOf("clean")),
    MSBuild("msbuild", sequenceOf("msbuild")),
    VSTest("vstest", sequenceOf("vstest"))
}