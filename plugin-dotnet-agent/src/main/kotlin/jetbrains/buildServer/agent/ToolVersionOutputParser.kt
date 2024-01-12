

package jetbrains.buildServer.agent

/**
 * Tries to find a version among tool output lines.
 */
interface ToolVersionOutputParser {
    fun parse(output: Collection<String>): Version
}