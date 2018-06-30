package jetbrains.buildServer.dotnet

enum class VisualStudioAction(val id: String, val description: String) {
    VisualStudioActionClean("clean", "clean"),
    VisualStudioActionRebuild("rebuild", "rebuild"),
    VisualStudioActionBuild("build", "build"),
    VisualStudioActionDeploy("deploy", "deploy");
}