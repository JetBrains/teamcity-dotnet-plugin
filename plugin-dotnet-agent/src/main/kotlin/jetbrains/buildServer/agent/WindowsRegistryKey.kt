package jetbrains.buildServer.agent

class WindowsRegistryKey private constructor(
        val bitness: WindowsRegistryBitness,
        val hive: WindowsRegistryHive,
        val parts: Array<String>) {

    val regKey: String

    init {
        regKey = "${hive.rootKey}\\${parts.joinToString("\\")}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WindowsRegistryKey

        return regKey.contentEquals(other.regKey)
    }

    override fun hashCode(): Int = regKey.hashCode()

    override fun toString() = "$regKey(${bitness.id})"

    companion object {
        public fun create(bitness: WindowsRegistryBitness, hive: WindowsRegistryHive, vararg key: String) =
                WindowsRegistryKey(bitness, hive, arrayOf(*key))

    }
}

public operator fun WindowsRegistryKey.plus(key: String): WindowsRegistryKey =
    WindowsRegistryKey.create(this.bitness, this.hive, *(this.parts + key))

public operator fun WindowsRegistryKey.plus(keys: Array<String>): WindowsRegistryKey =
        WindowsRegistryKey.create(this.bitness, this.hive, *(this.parts + keys))

public operator fun WindowsRegistryKey.plus(keys: List<String>): WindowsRegistryKey =
        this + keys.toTypedArray()