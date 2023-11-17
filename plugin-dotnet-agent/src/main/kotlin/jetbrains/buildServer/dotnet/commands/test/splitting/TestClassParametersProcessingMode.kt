package jetbrains.buildServer.dotnet.commands.test.splitting

import jetbrains.buildServer.dotnet.commands.test.splitting.TestClassParametersProcessingMode.EscapeSpecialCharacters

enum class TestClassParametersProcessingMode {
    Trim,                    // deprecated, remove after testing of EscapeSpecialCharacters mode
    NoProcessing,            // deprecated, remove after testing of EscapeSpecialCharacters mode
    EscapeSpecialCharacters, // default
}

fun String?.toTestClassParametersProcessingMode() =
    TestClassParametersProcessingMode.values()
        .find { it.toString().lowercase() == this?.trim()?.lowercase() }
        ?: EscapeSpecialCharacters