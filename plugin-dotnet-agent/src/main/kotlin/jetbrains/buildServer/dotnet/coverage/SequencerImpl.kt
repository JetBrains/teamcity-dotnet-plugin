package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.util.StringUtil

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
class SequencerImpl : Sequencer<String> {

    override fun nextFrom(value: String): String {
        return if (StringUtil.isEmptyOrSpaces(value)) {
            "1"
        } else try {
            val next = value.toLong() + 1
            return next.toString()
        } catch (ex: NumberFormatException) {
            throw IllegalArgumentException(
                "The 'value' argument should be string representation of a number or an empty string.", ex)
        }
    }
}