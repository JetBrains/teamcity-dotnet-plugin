package jetbrains.buildServer

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

fun Node.build(elemen: E): Element {
    val owner = if(this is Document) this else this.ownerDocument!!
    val newElement = owner.createElement(elemen.name)
    elemen.value?.let {
        newElement.textContent = it
    }

    this.appendChild(newElement)
    for (attr in elemen.attributes) {
        if (attr.name.isNotBlank() && attr.value?.isNotBlank() ?: false) {
            newElement.setAttribute(attr.name, attr.value)
        }
    }

    for (nestedElement in elemen.elements.mapNotNull { it }.filter { !it.isEmpty }) {
        newElement.build(nestedElement)
    }

    return newElement
}

data class E(val name: String, val elements: Sequence<E?>, val value: String? = null) {
    private val _attrs = mutableListOf<A>()

    public val attributes get() = _attrs.asSequence()

    constructor(name: String, vararg nestedElements: E?)
            : this(name, nestedElements.asSequence())

    constructor(name: String, value: String?)
            : this(name, emptySequence(), value)

    val isEmpty: Boolean get() = value.isNullOrEmpty() && !attributes.any() && !elements.any()

    fun a(name: String, value: String):E {
        _attrs.add(A(name, value))
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

data class A(val name: String, val value: String?) {
    override fun toString() = if(value == null) "$name='$value'" else ""
}
