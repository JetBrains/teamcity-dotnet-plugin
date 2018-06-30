package jetbrains.buildServer.dotnet

import java.io.IOException
import java.io.OutputStream
import java.net.URL

interface HttpDownloader {
    @Throws(IOException::class)
    fun download(url: URL, outputStream: OutputStream)
}