package jetbrains.buildServer.dotnet.discovery

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.util.browser.Browser
import java.io.InputStream

class StreamFactoryImpl(private val _browser: Browser) : StreamFactory {
    override fun tryCreate(path: String): InputStream? {
        val element = _browser.getElement(path)
        if (element != null) {
            return element.inputStream
        }

        LOG.debug("Element \"#path\" was not found")
        return null
    }

    companion object {
        private val LOG: Logger = Logger.getInstance(StreamFactoryImpl::class.java.name)
    }
}