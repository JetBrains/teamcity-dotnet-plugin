package jetbrains.buildServer.agent

import WindowsRegistryValueType

data class WindowsRegistryValue(
        val key: WindowsRegistryKey,
        val type: WindowsRegistryValueType,
        private val _value: Any) {

    val text: String get() =
        when(type) {
            WindowsRegistryValueType.Str -> _value as String
            WindowsRegistryValueType.Text -> _value as String
            WindowsRegistryValueType.ExpandText -> _value as String
            else -> ""
        }

    val number: Long get() =
        when(type) {
            WindowsRegistryValueType.Int -> (_value as Int).toLong()
            WindowsRegistryValueType.Long -> _value as Long
            else -> 0L
        }

    override fun toString(): String {
        return "$type $key = $_value"
    }
}