package jetbrains.buildServer.inspect

object DotNetConstants {
    const val DOTNET_FRAMEWORK = "DotNetFramework"
    const val DOTNET_FRAMEWORK_TARGETING_PACK = DOTNET_FRAMEWORK + "TargetingPack"
    const val MS_BUILD = "MSBuildTools"
    const val v2_0 = "2.0"
    const val v3_0 = "3.0"
    const val v3_5 = "3.5"
    const val v4 = "4"
    const val v4_0 = "4.0"
    const val v4_5 = "4.5"
    const val v4_5_1 = "4.5.1"
    const val v4_5_2 = "4.5.2"
    const val v4_6 = "4.6"
    const val v4_6_1 = "4.6.1"
    const val v4_6_2 = "4.6.2"
    const val v4_7 = "4.7"
    const val v4_7_1 = "4.7.1"
    const val v4_7_2 = "4.7.2"
    const val v4_8 = "4.8"
    const val v12_0 = "12.0"
    const val v14_0 = "14.0"
    const val DOTNET_FRAMEWORK_2_0 = DOTNET_FRAMEWORK_TARGETING_PACK + v2_0
    const val DOTNET_FRAMEWORK_3_0 = DOTNET_FRAMEWORK_TARGETING_PACK + v3_0
    const val DOTNET_FRAMEWORK_3_5 = DOTNET_FRAMEWORK_TARGETING_PACK + v3_5
    const val DOTNET_FRAMEWORK_4_0 = DOTNET_FRAMEWORK_TARGETING_PACK + v4_0
    const val DOTNET_FRAMEWORK_4_5 = DOTNET_FRAMEWORK_TARGETING_PACK + v4_5
    const val DOTNET_FRAMEWORK_4_5_1 = DOTNET_FRAMEWORK_TARGETING_PACK + v4_5_1
    const val DOTNET_FRAMEWORK_4_5_2 = DOTNET_FRAMEWORK_TARGETING_PACK + v4_5_2
    const val DOTNET_FRAMEWORK_4_6 = DOTNET_FRAMEWORK_TARGETING_PACK + v4_6
    const val DOTNET_FRAMEWORK_4_6_1 = DOTNET_FRAMEWORK_TARGETING_PACK + v4_6_1
    const val DOTNET_FRAMEWORK_4_6_2 = DOTNET_FRAMEWORK_TARGETING_PACK + v4_6_2
    const val DOTNET_FRAMEWORK_4_7 = DOTNET_FRAMEWORK_TARGETING_PACK + v4_7
    const val DOTNET_FRAMEWORK_4_7_1 = DOTNET_FRAMEWORK_TARGETING_PACK + v4_7_1
    const val DOTNET_FRAMEWORK_4_7_2 = DOTNET_FRAMEWORK_TARGETING_PACK + v4_7_2
    const val DOTNET_FRAMEWORK_4_8 = DOTNET_FRAMEWORK_TARGETING_PACK + v4_8
    const val MS_BUILD_12_0 = MS_BUILD + v12_0
    const val MS_BUILD_14_0 = MS_BUILD + v14_0
    const val OK = 0
    const val GENERAL_ERROR = 1
    const val LICENSE_CHECK_ERROR = 2
    const val NOTHING_TO_ANALYSE_ERROR = 3
}
