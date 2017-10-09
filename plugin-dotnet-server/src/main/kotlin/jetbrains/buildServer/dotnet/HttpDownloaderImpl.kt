package jetbrains.buildServer.dotnet

import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpDownloaderImpl : HttpDownloader {
    override fun download(url: URL, outputStream: OutputStream) {
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.inputStream.use {
                val buffer = ByteArray(1024)
                var count: Int
                do {
                    count = it.read(buffer)
                    if(count > 0) {
                        outputStream.write(buffer, 0, count)
                    }
                } while (count > 0)

                outputStream.flush()
            }
        } finally {
            connection.disconnect()
        }
    }
}