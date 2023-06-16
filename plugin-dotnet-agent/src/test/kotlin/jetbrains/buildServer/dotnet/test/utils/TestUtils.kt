package jetbrains.buildServer.dotnet.test.utils

import jetbrains.buildServer.TempFiles

class TestUtils {
    companion object {
        val myTempFiles = TempFiles()

        @Suppress("UNCHECKED_CAST")
        fun <T> uninitialized(value: T?): T = value as T
    }
}