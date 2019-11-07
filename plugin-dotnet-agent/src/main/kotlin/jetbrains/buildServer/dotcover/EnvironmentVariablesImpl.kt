package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.util.OSType
import org.apache.log4j.Logger

class EnvironmentVariablesImpl(
        private val _virtualContext: VirtualContext)
    : EnvironmentVariables {
    override fun getVariables(): Sequence<CommandLineEnvironmentVariable> = sequence {
        if (_virtualContext.targetOSType == OSType.UNIX) {
            yieldAll(linuxDefaultVariables)
        }
    }

    companion object {
        private val LOG = Logger.getLogger(EnvironmentVariablesImpl::class.java)

        internal val linuxDefaultVariables = sequenceOf(
                CommandLineEnvironmentVariable("LC_ALL", "C"))
    }
}