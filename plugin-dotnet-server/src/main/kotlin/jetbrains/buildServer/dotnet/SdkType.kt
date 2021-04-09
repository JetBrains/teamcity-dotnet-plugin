package jetbrains.buildServer.dotnet

public enum class SdkType(val description: String, val order: Int, val shortDescription: String) {
    Dotnet(".NET SDK", 0, "net"),

    DotnetCore(".NET Core SDK", 1, "core"),

    FullDotnetTargetingPack(".NET Targeting Pack", 2, "pack"),

    DotnetFramework(".NET Framework", 3, "")
}