package jetbrains.buildServer

import java.time.Duration
import java.time.OffsetDateTime

class TimeEvictStrategy(refreshDuration: Duration) : EvictStrategy {
    private var _validTo: OffsetDateTime = OffsetDateTime.now().plus(refreshDuration)

    override val isEvicting: Boolean
        get() = _validTo < OffsetDateTime.now()
}