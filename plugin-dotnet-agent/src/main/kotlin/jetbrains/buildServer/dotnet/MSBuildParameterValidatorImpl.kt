package jetbrains.buildServer.dotnet

class MSBuildParameterValidatorImpl : MSBuildParameterValidator {
    public override fun isValid(parameter: MSBuildParameter): Boolean =
            parameter.name.isNotBlank() &&
            parameter.value.isNotBlank() &&
            parameter.name
                .mapIndexed { index: Int, c: Char -> if (index == 0) isValidInitialElementNameCharacter(c) else isValidSubsequentElementNameCharacter(c) }
                .all { it }

    private fun isValidInitialElementNameCharacter(c: Char) =
        (c >= 'A' && c <= 'Z') ||
        (c >= 'a' && c <= 'z') ||
        (c == '_')

    private fun isValidSubsequentElementNameCharacter(c: Char) =
        (c >= 'A' && c <= 'Z') ||
        (c >= 'a' && c <= 'z') ||
        (c >= '0' && c <= '9') ||
        (c == '_') ||
        (c == '-')
}