package jetbrains.buildServer.dotnet

import java.time.OffsetDateTime
import java.time.Duration

class TimeEvictStrategy(refreshDuration: Duration): EvictStrategy {
    private var _validTo: OffsetDateTime

    init {
        _validTo = OffsetDateTime.now().plus(refreshDuration)
    }

    override val isEvicting: Boolean
    get() = _validTo < OffsetDateTime.now()
}