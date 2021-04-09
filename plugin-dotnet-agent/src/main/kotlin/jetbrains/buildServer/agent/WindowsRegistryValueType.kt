enum class WindowsRegistryValueType(val id: String) {
    Str("REG_SZ"),
    Bin("REG_BINARY"),
    Int("REG_DWORD"),
    Long("REG_QWORD"),
    Text("REG_MULTI_SZ"),
    ExpandText("REG_EXPAND_SZ");

    companion object {
        fun tryParse(id: kotlin.String): WindowsRegistryValueType? {
            return WindowsRegistryValueType.values().singleOrNull { it.id.equals(id, true) }
        }
    }
}