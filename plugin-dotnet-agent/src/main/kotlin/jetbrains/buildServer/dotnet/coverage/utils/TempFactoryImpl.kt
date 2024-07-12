package jetbrains.buildServer.dotnet.coverage.utils

import jetbrains.buildServer.dotnet.coverage.Sequencer
import java.io.File
import java.io.IOException

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
class TempFactoryImpl(
    private val _fileService: FileService,
    private val _sequencer: Sequencer<String>) : TempFactory {

    @Throws(IOException::class)
    override fun createTempFile(path: File,
                                prefix: String,
                                extension: String,
                                maxAttempts: Int): File {
        _fileService.createDirectory(path)
        var sequenceValue = ""
        var attempts = maxAttempts
        do {
            val fileName = _fileService.sanitizeFileName(prefix + sequenceValue) + extension
            val file = File(path, fileName)
            if (!_fileService.exists(file) && !_fileService.isDirectory(file)) {
                try {
                    if (_fileService.createFile(file)) {
                        return file
                    }
                } catch (e: IOException) {
                    if (attempts-- < 0) {
                        throw IOException("Error creating temporary file '" + file + "' (multiple attempts): " + e.message, e)
                    }
                }
            }
            sequenceValue = _sequencer.nextFrom(sequenceValue)
        } while (true)
    }

    override fun createTempDirectory(path: File,
                                     maxAttempts: Int): File {
        val sequenceValue = ""
        var tempDirectory = path
        do {
            if (_fileService.exists(tempDirectory)) {
                if (!_fileService.isDirectory(tempDirectory)) {
                    tempDirectory = File(path.path + _sequencer.nextFrom(sequenceValue))
                    continue
                }
            } else {
                _fileService.createDirectory(tempDirectory)
            }
            return tempDirectory
        } while (true)
    }
}
