

package jetbrains.buildServer.agent

import jetbrains.buildServer.util.pathMatcher.AntPatternFileCollector
import java.io.File

class AntPathMatcher : PathMatcher {
    override fun match(path: File, includeRules: List<String>): List<File> =
            AntPatternFileCollector.scanDir(path, includeRules.toTypedArray(), emptyArray(), ScanOptions)

    override fun match(path: File, includeRules: List<String>, excludeRules: List<String>): List<File> =
            AntPatternFileCollector.scanDir(path, includeRules.toTypedArray(),excludeRules.toTypedArray(), ScanOptions)

    companion object {
        private val ScanOptions = arrayOf(
                AntPatternFileCollector.ScanOption.PRIORITIZE_EXCLUDES,
                AntPatternFileCollector.ScanOption.ALLOW_EXTERNAL_SCAN)
    }
}