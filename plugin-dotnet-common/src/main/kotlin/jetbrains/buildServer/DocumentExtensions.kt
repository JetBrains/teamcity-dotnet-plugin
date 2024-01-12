

package jetbrains.buildServer

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

inline fun <reified T : Node> NodeList.asSequence() = sequence {
    for (i in 0 until this@asSequence.length) {
        val node = this@asSequence.item(i)
        if (node is T) {
            yield(node)
        }
    }
}

inline fun <reified T : Node> Node.find(xpath: String) =
    XPathFactory.newInstance().newXPath().evaluate(xpath, this, XPathConstants.NODESET).let {
        if (it is NodeList) it.asSequence<T>() else emptySequence<T>()
    }

fun Node.build(element: XmlElement): Element {
    val owner = if (this is Document) this else this.ownerDocument!!
    val newElement = owner.createElement(element.name)
    element.value?.let {
        newElement.textContent = it
    }

    this.appendChild(newElement)
    for (attr in element.attributes) {
        if (attr.name.isNotBlank() && attr.value?.isNotBlank() ?: false) {
            newElement.setAttribute(attr.name, attr.value)
        }
    }

    for (nestedElement in element.elements.mapNotNull { it }.filter { !it.isEmpty }) {
        newElement.build(nestedElement)
    }

    return newElement
}

data class XmlElement(val name: String, val elements: Sequence<XmlElement?>, val value: String? = null) {
    private val _attrs = mutableListOf<XmlElementAttribute>()

    public val attributes get() = _attrs.asSequence()

    constructor(name: String, vararg nestedElements: XmlElement?)
            : this(name, nestedElements.asSequence())

    constructor(name: String, value: String?)
            : this(name, emptySequence(), value)

    val isEmpty: Boolean get() = value.isNullOrEmpty() && !attributes.any() && !elements.any()

    fun withAttribute(name: String, value: String): XmlElement {
        _attrs.add(XmlElementAttribute(name, value))
        return this
    }

    operator fun get(name: String): String? =
        attributes.filter { it.name.equals(name, true) }.firstOrNull()?.value

    override fun toString() =
        when {
            elements.filter { !(it?.isEmpty ?: true) }.any() -> {
                val nested = elements.filter { !(it?.isEmpty ?: true) }.joinToString("\n")
                "<$name${if (attributes.any()) " " else ""}${attributes.joinToString(" ")}>\n$nested\n</$name>"
            }

            value.isNullOrEmpty() -> ""
            else -> "<$name${if (attributes.any()) " " else ""}${attributes.joinToString(" ")}>$value</$name>"
        }
}

data class XmlElementAttribute(val name: String, val value: String?) {
    override fun toString() = if (value != null) "$name='$value'" else ""
}