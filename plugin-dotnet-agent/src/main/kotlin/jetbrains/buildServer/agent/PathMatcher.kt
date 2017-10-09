package jetbrains.buildServer.agent

import java.io.File

interface PathMatcher {
    fun match(path: File, includeRules: Sequence<String>, excludeRules: Sequence<String>): Sequence<File>
}