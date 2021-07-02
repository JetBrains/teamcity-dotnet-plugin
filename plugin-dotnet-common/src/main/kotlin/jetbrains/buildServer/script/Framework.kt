package jetbrains.buildServer.script

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.SemanticVersion
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType

object FrameworkRequirements {
    fun create(vararg versions: String): Requirement {
        return Requirement(RequirementQualifier.EXISTS_QUALIFIER + "${DotnetConstants.CONFIG_PREFIX_CORE_RUNTIME}(${versions.joinToString("|")})[\\d\\.]+${DotnetConstants.CONFIG_SUFFIX_PATH}", null, RequirementType.EXISTS)
    }
}

enum class Framework(val tfm: String, val description: String, val requirement: Requirement) {
    Any("any", "Any", FrameworkRequirements.create("6\\.", "5\\.", "3\\.1")),
    Net60("net6.0", ".NET 6.0", FrameworkRequirements.create("6\\.")),
    Net50("net5.0", ".NET 5.0", FrameworkRequirements.create("5\\.")),
    NetCore31("netcoreapp3.1", ".NET Core 3.1", FrameworkRequirements.create("3\\.1"));

    companion object {
        fun tryParse(tfm: String) = Framework.values().singleOrNull { it.tfm.equals(tfm, true) }
    }
}