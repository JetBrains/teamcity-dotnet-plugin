package jetbrains.buildServer.dotnet.discovery

import java.io.BufferedInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

class ReaderFactoryImpl : ReaderFactory {
    override fun create(inputStream: InputStream): Reader = getInputStreamReader(BufferedInputStream(inputStream))

    private fun getInputStreamReader(inputStream: InputStream): Reader {
        inputStream.mark(3)
        val byte1 = inputStream.read()
        val byte2 = inputStream.read()
        return if (byte1 == 0xFF && byte2 == 0xFE) {
            InputStreamReader(inputStream, "UTF-16LE")
        } else if (byte1 == 0xFF && byte2 == 0xFF) {
            InputStreamReader(inputStream, "UTF-16BE")
        } else {
            val byte3 = inputStream.read()
            if (byte1 == 0xEF && byte2 == 0xBB && byte3 == 0xBF) {
                InputStreamReader(inputStream, "UTF-8")
            } else {
                inputStream.reset()
                InputStreamReader(inputStream)
            }
        }
    }
}