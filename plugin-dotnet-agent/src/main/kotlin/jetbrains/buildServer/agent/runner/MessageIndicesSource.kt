package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.Logger

class MessageIndicesSource(
        private val _positionsSource: Source<Long>)
    : Source<Index> {
    override fun read(source: String, fromPosition: Long, count: Long) = sequence {
        if (source.isNullOrBlank()) {
            throw IllegalArgumentException()
        }

        var initPosition = fromPosition
        var mesageCount = count
        var firstPosition = true
        if (initPosition > 0L) {
            initPosition--
            mesageCount++
        }

        var prevMesssagePosotion = 0L
        for (messagePosition in _positionsSource.read(source, initPosition, mesageCount)) {
            if (firstPosition && initPosition != fromPosition) {
                prevMesssagePosotion = messagePosition
                firstPosition = false
                continue
            }

            if (messagePosition > prevMesssagePosotion) {
                yield(Index(prevMesssagePosotion, messagePosition - prevMesssagePosotion))
                prevMesssagePosotion = messagePosition
            }
            else {
                LOG.warn("Invalid position $messagePosition, the previous position was $prevMesssagePosotion.")
                break
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(MessageIndicesSource::class.java)
    }
}