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

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageParserCallback
import java.text.ParseException

// TODO the guard was only used for test report service messages, remove it in a few releases after releasing filestreaming-based test reporting
class MessagesGuard(
        private val _messagesSource: Source<String>)
    : OutputReplacer {
    private val _indices = mutableMapOf<String, Long>()

    override fun replace(text: String): Sequence<String> {
        val aggregator = TextAggregator(text, _messagesSource, _indices)
        ServiceMessage.parse(text, aggregator);
        return aggregator.Compose()
    }

    companion object {
        private val LOG = Logger.getLogger(MessagesGuard::class.java)
        public const val SourceAttribute = "source"
        public const val IndexAttribute = "index"
    }

    private class TextAggregator(
            private val _text: String,
            private val _messagesSource: Source<String>,
            private val _indices: MutableMap<String, Long>)
        : ServiceMessageParserCallback {
        private val _stringBuilders = mutableListOf<StringBuilder>()
        private var isModified = false

        public fun Compose() =
                if (isModified || _stringBuilders.size > 0)
                    _stringBuilders.asSequence().map { it.toString() }
                else
                    sequenceOf(_text)

        override fun regularText(text: String) {
            GetNewBuilder().append(text)
        }

        override fun serviceMessage(message: ServiceMessage) {
            try {
                message.attributes.get(IndexAttribute)?.toLongOrNull()?.let { index ->
                    message.attributes.get(SourceAttribute)?.let { source ->
                        if (source.isNotBlank()) {
                            val estimatedIndex = _indices[source]?.let { it + 1L } ?: 0L
                            when {
                                // Missed message(s)
                                index > estimatedIndex -> {
                                    val count = index - estimatedIndex
                                    LOG.warn("Missed message(s) found in \"$source\" from position $estimatedIndex by the amount of $count.")
                                    var counter = 0;
                                    for (newMessage in _messagesSource.read(source, estimatedIndex, count)) {
                                        LOG.warn("Restored: $newMessage")
                                        GetNewBuilder().append(newMessage)
                                        counter++;
                                    }

                                    LOG.warn("Restored $counter message(s) for indices in the range: [${estimatedIndex}, ${index - 1}].")
                                    _indices[source] = index
                                    GetNewBuilder().append(message)
                                }

                                // Everything is fine
                                index == estimatedIndex -> {
                                    _indices[source] = index
                                    GetNewBuilder().append(message)
                                }

                                // Duplicate message
                                else -> {
                                    LOG.warn("Duplicate message: $message")
                                    isModified = true
                                }
                            }
                        }
                        else {
                            LOG.debug("The source is blank.")
                        }
                    }
                }
            }
            catch (_: Exception) { }
        }

        override fun parseException(parseException: ParseException, text: String) {
            isModified = true
            LOG.debug("Error while parsing a message. This message will be suppressed: \"$text\": ${parseException.message}.")
        }

        private fun GetCurrentBuilder() = _stringBuilders.lastOrNull() ?: GetNewBuilder()

        private fun GetNewBuilder(): StringBuilder {
            var stringBuilder = StringBuilder();
            _stringBuilders.add(stringBuilder)
            return stringBuilder
        }
    }
}
