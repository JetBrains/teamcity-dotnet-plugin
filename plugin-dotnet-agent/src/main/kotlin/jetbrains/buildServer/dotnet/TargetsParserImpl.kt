package jetbrains.buildServer.dotnet

class TargetsParserImpl : TargetsParser {
    override fun parse(targets: String) =
        tokenize(targets.asSequence()).joinToString(";")

    private fun tokenize(chars: Sequence<Char>) = sequence<String> {
        val token = mutableListOf<Char>()
        for (c in split(chars)) {
            if (c == null) {
                if (token.size > 0) {
                    yield(String(token.toCharArray()));
                    token.clear()
                }
            } else {
                token.add(c);
            }
        }

        if (token.size > 0) {
            yield(String(token.toCharArray()));
            token.clear()
        }
    }

    private fun split(chars: Sequence<Char>) : Sequence<Char?> = sequence {
        var quotes = false
        for (c in chars) {
            when {
                c == '"' -> {
                    quotes = !quotes;
                    if (!quotes) {
                        yield(null)
                    }
                }

                c == ' ' || c == ';' -> {
                    if (!quotes) {
                        yield(null)
                    }
                    else {
                        yield(c)
                    }
                }

                else -> yield(c)
            }
        }
    }
}