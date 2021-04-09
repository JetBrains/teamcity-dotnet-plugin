package jetbrains.buildServer.agent

import jetbrains.buildServer.rx.Disposable
import java.io.Serializable

class Logger(
        private val _logger: org.apache.log4j.Logger) {

    val isDebugEnabled: Boolean get() = _logger.isDebugEnabled

    fun block(message: String, action: (message: String) -> Unit): Disposable {
        action("Start: $message")
        val start = System.currentTimeMillis()
        return object: Disposable {
            override fun dispose() {
                val finish = System.currentTimeMillis() - start
                action("Finish: $message ($finish ms)")
            }
        }
    }
    fun info(info: String) = _logger.info(formatMessage(info))

    fun infoBlock(info: String): Disposable = block(info) { info(it) }

    fun warn(warning: String, exception: Throwable? = null) =
        _logger.warn(formatMessage(warning), exception)

    fun warn(exception: Throwable) = warn("", exception)

    fun debug(debug: String, exception: Throwable? = null) =
            _logger.debug(formatMessage(debug), exception)

    fun debug(exception: Throwable) = debug("", exception)

    fun debugBlock(debug: String): Disposable = block(debug) { debug(it) }

    fun error(error: String, exception: Throwable?) =
            _logger.error(formatMessage(error), exception)

    fun error(exception: Throwable) = error("", exception)

    private fun formatMessage(message: String): String {
        if (!isDebugEnabled) {
            return message
        }

        val threadId = Thread.currentThread().getId()
        val prefix = "#${String.format("%05d", threadId)}"
        if (message.isNullOrBlank()) {
           return prefix
        }

        return "$prefix $message"
    }

    companion object {
        public fun getLogger(clazz: Class<*>) = Logger(org.apache.log4j.LogManager.getLogger(clazz))
    }
}