package jetbrains.buildServer.dotcover

enum class DotCoverMode {
    Disabled,
    Wrapper,
    Runner;

    val isDisabled get() = this == Disabled
    val isEnabled get() = !isDisabled
}
