package jetbrains.buildServer.inspect

import jetbrains.buildServer.E
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamReader

class XmlReaderImpl : XmlReader {
    override fun read(xmlStream: InputStream) = sequence<E> {
        InputStreamReader(xmlStream, "UTF8").use {
            BufferedReader(it).use {
                val xmlInFact = XMLInputFactory.newInstance()
                xmlInFact.setProperty(XMLInputFactory.IS_COALESCING, true);
                var reader: XMLStreamReader? = null
                try {
                    reader = xmlInFact.createXMLStreamReader(it)
                    var nextElement: E? = null
                    while (reader.hasNext()) {
                        try {
                            when (reader.eventType) {
                                XMLStreamConstants.START_ELEMENT -> {
                                    if (nextElement != null) {
                                        yield(nextElement)
                                    }

                                    nextElement = E(if (reader.hasName()) reader.getLocalName() else "")
                                    for (index in 0 until reader.getAttributeCount()) {
                                        nextElement.a(reader.getAttributeLocalName(index), reader.getAttributeValue(index))
                                    }
                                }

                                XMLStreamConstants.CHARACTERS -> {
                                    if(!reader.isWhiteSpace && nextElement != null) {
                                        var atrs = nextElement.attributes
                                        nextElement = E(nextElement.name, reader.text)
                                        for(atr in atrs) {
                                            val value = atr.value
                                            if(value != null) {
                                                nextElement.a(atr.name, value)
                                            }
                                        }
                                    }
                                }

                                XMLStreamConstants.END_ELEMENT -> {
                                    if (nextElement != null) {
                                        yield(nextElement)
                                        nextElement = null
                                    }
                                }
                            }
                        } finally {
                            reader.next()
                        }
                    }
                } finally {
                    reader?.close()
                }
            }
        }
    }
}