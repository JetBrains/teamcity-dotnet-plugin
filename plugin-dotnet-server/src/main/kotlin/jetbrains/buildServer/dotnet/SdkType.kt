package jetbrains.buildServer.dotnet

public enum class SdkType(val description: String) {
    Dotnet(".NET SDK"),

    DotnetCore(".NET Core SDK"),

    FullDotnet(".NET Targeting Pack")
}