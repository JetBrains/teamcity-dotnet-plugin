package jetbrains.buildServer.dotcover.utils

import jetbrains.buildServer.util.CloseAwareInputStream
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.XmlUtil
import org.xml.sax.*
import java.io.*
import java.util.*
import java.util.regex.Pattern

abstract class XmlXppAbstractParser {

    private var _trimInfo: XmlTextTrimInfo = object : XmlTextTrimInfo {
        override val maxSize: Long
            get() = 5 * 1024 * 1024

        override fun getTrimText(actualSize: Long): String {
            return """
                Text was trimmed. Actual size was: ${StringUtil.formatFileSize(actualSize)}
                """.trimIndent()
        }
    }

    fun setTrimInfo(info: XmlTextTrimInfo) {
        _trimInfo = info
    }

    @Throws(IOException::class)
    fun parse(file: File) {
        parse(BufferedInputStream(FileInputStream(file)))
    }

    @Throws(IOException::class)
    fun parse(file: InputStream) {
        val inpStr: InputStream = CloseAwareInputStream(file)
        try {
            parseWithSAX(InputSource(inpStr))
        } catch (e: SAXException) {
            throw object : IOException(e.message) {
                init {
                    initCause(e)
                }
            }
        } finally {
            inpStr.close()
        }
    }

    @Throws(SAXException::class, IOException::class)
    private fun parseWithSAX(input: InputSource) {
        val xmlReader = createXMLReader()

        xmlReader.contentHandler = object : ContentHandler {
            private var myState: StateInfo? = null

            inner class StateInfo(private val myElementName: String,
                                  val parentState: StateInfo?) : XmlElementVisitor {

                private val myActions: MutableList<XmlAction> = ArrayList(1)
                private var myTextHandler: Capturer? = null
                var handlers = arrayOf<XmlHandler>()

                override fun toString(): String {
                    return (if (parentState != null) "$parentState/" else "") + myElementName
                }

                fun setTextHandler(textHandler: TextHandler) {
                    myTextHandler = if (myTextHandler == null) {
                        Capturer(textHandler)
                    } else {
                        throw IllegalArgumentException()
                    }
                }

                fun finished() {
                    myTextHandler?.finished()
                    for (action in myActions) {
                        action.apply()
                    }
                    for (handler in handlers) {
                        if (handler is CloseableHandler) {
                            (handler as CloseableHandler).close()
                        }
                    }
                }

                override fun than(action: XmlAction): XmlReturn {
                    myActions.add(action)
                    return XMLRETURN
                }

                fun append(ch: CharArray?, start: Int, length: Int) {
                    myTextHandler?.append(ch, start, length) ?: return
                }
            }

            inner class Capturer(private val myHandler: TextHandler) {
                private val myText = StringBuilder()
                private var mySize: Long = 0
                private var myTextFound = false

                fun finished() {
                    if (!myTextFound) {
                        myHandler.setText("")
                    } else {
                        if (mySize > _trimInfo.maxSize) {
                            myText.append(_trimInfo.getTrimText(mySize))
                        }
                        myHandler.setText(myText.toString())
                    }
                }

                fun append(ch: CharArray?, start: Int, length: Int) {
                    myTextFound = true
                    mySize += length.toLong()
                    val pLength = Math.min(length.toLong(), _trimInfo.maxSize - myText.length)
                    if (pLength > 0) {
                        myText.appendRange(ch!!, start, start + pLength.toInt())
                    }
                }
            }

            override fun setDocumentLocator(locator: Locator) {}

            @Throws(SAXException::class)
            override fun startDocument() {
                myState = StateInfo("<root>", null)
                val list: List<XmlHandler> = ArrayList(getRootHandlers())
                myState?.handlers = list.toTypedArray()
            }

            @Throws(SAXException::class)
            override fun endDocument() {
                myState?.finished()
                myState = null
            }

            @Throws(SAXException::class)
            override fun startPrefixMapping(prefix: String, uri: String) {
            }

            @Throws(SAXException::class)
            override fun endPrefixMapping(prefix: String) {
            }

            @Throws(SAXException::class)
            override fun startElement(uri: String, localName: String, qName: String, atts: Attributes) {
                val handlers = myState?.handlers ?: return
                myState = StateInfo(localName, myState)
                for (handler in handlers) {
                    if (handler.accepts(localName)) {
                        val info: StateInfo = myState!!
                        handler.processElement(object : XmlElementInfo {
                            override fun visitChildren(vararg handlers: XmlHandler): XmlElementVisitor {
                                info.handlers = arrayOf(*handlers)
                                return info
                            }

                            override fun getAttribute(name: String): String {
                                return atts.getValue(name)
                            }

                            override val attributes: Map<String, String>
                                get() {
                                    val length = atts.length
                                    val attributes = HashMap<String, String>(length)
                                    for (i in 0 until length) {
                                        attributes[atts.getLocalName(i)] = atts.getValue(i)
                                    }
                                    return attributes
                                }

                            override fun visitText(handler: TextHandler): XmlElementVisitor {
                                info.setTextHandler(handler)
                                return info
                            }

                            override fun noDeep(): XmlReturn {
                                return XMLRETURN
                            }

                            override val localName: String
                                get() = localName
                        })
                        return
                    }
                }
            }

            @Throws(SAXException::class)
            override fun endElement(uri: String, localName: String, qName: String) {
                myState?.let {
                    it.finished()
                    myState = it.parentState
                }
            }

            @Throws(SAXException::class)
            override fun characters(ch: CharArray, start: Int, length: Int) {
                myState?.append(ch, start, length)
            }

            @Throws(SAXException::class)
            override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) {
                myState?.append(ch, start, length)
            }

            @Throws(SAXException::class)
            override fun processingInstruction(target: String, data: String) {
            }

            @Throws(SAXException::class)
            override fun skippedEntity(name: String) {
            }
        }
        xmlReader.parse(input)
    }

    @Throws(SAXException::class)
    private fun createXMLReader(): XMLReader {
        return XmlUtil.createXMLReader(false)
    }

    /**
     * Returns list of [jetbrains.buildServer.util.XmlXppAbstractParser.XmlHandler] objects to
     * be applied to the root element. First match is applied.
     * @return list of [jetbrains.buildServer.util.XmlXppAbstractParser.XmlHandler] to apply
     */
    protected abstract fun getRootHandlers(): List<XmlHandler>

    interface Handler {
        fun processElement(reader: XmlElementInfo): XmlReturn
    }

    interface XmlHandler : Handler {
        fun accepts(name: String): Boolean
    }

    protected interface CloseableHandler : Handler {
        fun close()
    }

    interface XmlElementInfo {

        fun visitChildren(vararg handlers: XmlHandler): XmlElementVisitor

        fun getAttribute(name: String): String?

        val attributes: Map<String, String>

        /**
         * Warning: this method may alter XmlElementInfo internal state, ensure you do not call #getAttribute or #getAttributes after #visitText
         */
        fun visitText(handler: TextHandler): XmlElementVisitor

        fun noDeep(): XmlReturn

        val localName: String
    }

    interface XmlElementVisitor : XmlReturn {
        fun than(action: XmlAction): XmlReturn
    }

    interface XmlAction {
        fun apply()
    }

    interface XmlReturn
    interface XmlTextTrimInfo {
        val maxSize: Long

        fun getTrimText(actualSize: Long): String?
    }

    interface TextHandler {
        /**
         * This method is called only once for all text
         * @param text
         */
        fun setText(text: String)
    }

    companion object {
        private val XMLRETURN: XmlReturn = object : XmlReturn {}

        /**
         * Create XmlHandler object that calls <tt>deeper</tt> for element matched <tt>path</tt>
         * @param deeper handler
         * @param path path to match
         * @return XmlHandler
         */
        fun elementsPath(deeper: Handler, vararg path: String): XmlHandler {
            val list: MutableList<String> = ArrayList(listOf(*path))
            val last = list[list.size - 1]
            list.removeAt(list.size - 1)
            return elementsPath(object : XmlHandler {
                override fun accepts(name: String): Boolean {
                    return last == name
                }

                override fun processElement(reader: XmlElementInfo): XmlReturn {
                    return deeper.processElement(reader)
                }

                override fun toString(): String {
                    return "$last/$deeper"
                }
            }, list)
        }

        /**
         * replacement for elementsPath(new Handler() { reader.visitText(handler)}, path)
         * @param text
         * @param path
         * @return XmlHanlder to read element(s) content under path
         */
        protected fun elementsPath(text: TextHandler, vararg path: String): XmlHandler {
            return elementsPath(object : Handler {
                override fun processElement(reader: XmlElementInfo): XmlReturn {
                    return reader.visitText(text)
                }
            }, *path)
        }

        protected fun elementsPatternPath(deeper: Handler, patternPath: String): XmlHandler {
            val pt = Pattern.compile(patternPath)
            return object : XmlHandler {
                override fun accepts(name: String): Boolean {
                    return pt.matcher(name).matches()
                }

                override fun processElement(reader: XmlElementInfo): XmlReturn {
                    return deeper.processElement(reader)
                }

                override fun toString(): String {
                    return "[ $patternPath ]/$deeper"
                }
            }
        }

        protected fun elementsPath(deeperInp: XmlHandler, list: MutableList<String>): XmlHandler {
            var deeper = deeperInp
            list.reverse()
            for (element in list) {
                val actualDeeper = deeper
                deeper = object : XmlHandler {
                    override fun accepts(name: String): Boolean {
                        return element == name
                    }

                    override fun processElement(reader: XmlElementInfo): XmlReturn {
                        return reader.visitChildren(actualDeeper)
                    }

                    override fun toString(): String {
                        return "$element/$actualDeeper"
                    }
                }
            }
            return deeper
        }
    }
}
