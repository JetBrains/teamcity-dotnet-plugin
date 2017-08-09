package jetbrains.buildServer.dotnet

import java.io.File
import java.io.FileNotFoundException
import java.io.OutputStream

interface FileSystemService {
    @Throws(FileNotFoundException::class) fun createOutputFile(file: File): OutputStream
}