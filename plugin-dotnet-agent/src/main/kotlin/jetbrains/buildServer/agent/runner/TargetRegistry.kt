package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.rx.Disposable

interface TargetRegistry {
    val activeTargets: Sequence<TargetType>

    fun register(targetType: TargetType): Disposable
}