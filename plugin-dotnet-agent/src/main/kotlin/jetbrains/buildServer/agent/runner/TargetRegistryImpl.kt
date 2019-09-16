package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.disposableOf

class TargetRegistryImpl : TargetRegistry {
    private val _activeTargets = mutableListOf<TargetHolder>()

    override val activeTargets: Sequence<TargetType>
        get() = _activeTargets.map { it.targetType }.distinct().asSequence()

    override fun register(targetType: TargetType): Disposable {
        val holder = TargetHolder(targetType)
        _activeTargets.add(holder)
        return disposableOf { _activeTargets.remove(holder) }
    }

    private class TargetHolder(val targetType: TargetType)
}