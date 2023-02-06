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

package jetbrains.buildServer

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

inline fun <reified T: Node> NodeList.asSequence() = sequence {
    for (i in 0 until this@asSequence.length) {
        val node = this@asSequence.item(i)
        if(node is T) {
            yield(node)
        }
    }
}

inline fun <reified T: Node> Node.find(xpath: String) =
        XPathFactory.newInstance().newXPath().evaluate(xpath, this, XPathConstants.NODESET).let {
            if(it is NodeList) it.asSequence<T>() else emptySequence<T>()
        }

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
    override fun toString() = if(value != null) "$name='$value'" else ""
}
