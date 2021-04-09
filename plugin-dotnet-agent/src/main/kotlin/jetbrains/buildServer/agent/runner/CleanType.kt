package jetbrains.buildServer.agent.runner

public enum class CleanType(val weight: Long) {
    Deep(0),
    Medium(7),
    Light(14);
}