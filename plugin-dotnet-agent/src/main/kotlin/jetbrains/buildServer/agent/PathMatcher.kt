

package jetbrains.buildServer.agent

import java.io.File

interface PathMatcher {
    fun match(path: File, includeRules: List<String>): List<File>
    fun match(path: File, includeRules: List<String>, excludeRules: List<String>): List<File>
}