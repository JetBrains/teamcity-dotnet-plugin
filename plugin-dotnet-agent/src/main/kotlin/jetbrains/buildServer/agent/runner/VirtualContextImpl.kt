package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.VirtualContext
import java.io.File

class VirtualContextImpl(private val _base: VirtualContext): VirtualContext by _base {
    constructor(buildStepContext: BuildStepContext): this(buildStepContext.runnerContext.virtualContext) { }

    override fun resolvePath(path: String): String {
        try {
            var originalPath = File(path).canonicalPath
            val resolvedPath = _base.resolvePath(originalPath)
            if (originalPath == resolvedPath) {
                return path
            } else {
                return resolvedPath
            }
        } catch (ex: Throwable) {
            return path
        }
    }
}