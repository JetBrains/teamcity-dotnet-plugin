package jetbrains.buildServer.agent

import org.jetbrains.annotations.NotNull
import java.io.File

interface PathMatcher {
    fun match(path: File, includeRules: Sequence<String>, excludeRules: Sequence<String>): Sequence<File>
}