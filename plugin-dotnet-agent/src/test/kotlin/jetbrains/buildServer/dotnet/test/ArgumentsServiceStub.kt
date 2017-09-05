package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.runners.ArgumentsService

class ArgumentsServiceStub : ArgumentsService {
    override fun escape(text: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun split(text: String): Sequence<String> {
        return jetbrains.buildServer.util.StringUtil.splitCommandArgumentsAndUnquote(text)
                .asSequence()
                .filter { !it.isNullOrBlank() }
    }

    override fun combine(arguments: Sequence<String>): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}