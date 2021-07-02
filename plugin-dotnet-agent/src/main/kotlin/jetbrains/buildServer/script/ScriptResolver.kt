package jetbrains.buildServer.script

import java.io.File

interface ScriptResolver {
    fun resolve(): File
}