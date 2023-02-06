/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet.commands.targeting

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