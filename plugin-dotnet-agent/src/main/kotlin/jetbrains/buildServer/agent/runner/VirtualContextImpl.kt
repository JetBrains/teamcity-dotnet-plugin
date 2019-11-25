package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.util.OSType
import java.io.File

class VirtualContextImpl(private val _buildStepContext: BuildStepContext): VirtualContext {
    private val _baseVirtaulContext
            get() = _buildStepContext.runnerContext.virtualContext

    override fun isVirtual() = _baseVirtaulContext.isVirtual

    override fun getTargetOSType(): OSType = _baseVirtaulContext.targetOSType

    override fun resolvePath(path: String): String {
        if (!isVirtual) {
           return path
        }

        try {
            var originalPath = File(path).canonicalPath
            val resolvedPath = _baseVirtaulContext.resolvePath(originalPath)
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