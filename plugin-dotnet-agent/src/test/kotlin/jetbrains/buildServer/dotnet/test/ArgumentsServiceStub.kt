package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.runners.ArgumentsService

class ArgumentsServiceStub : ArgumentsService {
    override fun parseToStrings(text: String): Sequence<String> {
        return jetbrains.buildServer.util.StringUtil.splitCommandArgumentsAndUnquote(text)
                .asSequence()
                .filter { !it.isNullOrBlank() }
    }

}