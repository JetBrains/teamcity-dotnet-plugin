package jetbrains.buildServer.dotnet

public enum class SdkType(val description: String, val order: Int) {
    Dotnet(".NET SDK", 0),

    DotnetCore(".NET Core SDK", 1),

    FullDotnetTargetingPack(".NET Targeting Pack", 2),

    DotnetFramework(".NET Framework", 3)
}