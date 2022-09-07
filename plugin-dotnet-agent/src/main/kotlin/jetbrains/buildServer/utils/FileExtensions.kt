package jetbrains.buildServer.utils

import java.io.File

// there are wrappers over inline functions
// for mocking purposes only
fun File.getBufferedReader() = this.bufferedReader()
fun File.getBufferedWriter() = this.bufferedWriter()
