package jetbrains.buildServer.dotnet

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType

public enum class TargetDotNetFramework(val version: String) {
    v2_0("2.0"),
    v3_0("3.0"),
    v3_5("3.5"),
    v4_0("4.0"),
    v4_5("4.5"),
    v4_5_1("4.5.1"),
    v4_5_2("4.5.2"),
    v4_6("4.6"),
    v4_6_1("4.6.1"),
    v4_6_2("4.6.2"),
    v4_7("4.7"),
    v4_7_1("4.7.1"),
    v4_7_2("4.7.2"),
    v4_8("4.8");
    // Don't forget to update DSL while adding new version

    val id: String get() = "TargetDotNetFramework_$version"
    val description: String get() = ".NET Framework $version"
    val propertyName: String get() = "DotNetFrameworkTargetingPack$version"

    fun createExistsRequirement(): Requirement =
            Requirement(RequirementQualifier.EXISTS_QUALIFIER + propertyName + "_.*", null, RequirementType.EXISTS)
}