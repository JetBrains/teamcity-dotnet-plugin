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

infix fun <T: Comparable<T>>Bound<T>.to(that: Bound<T>): Range<T> = jetbrains.buildServer.SimpleRange<T>(this, that)
fun <T>combineOf(vararg ranges: Range<T>): Range<T> = MultiRange(*ranges)

private data class SimpleRange<T: Comparable<T>>(
        private val from: Bound<T>,
        private val to: Bound<T>)
    : Range<T> {
    override fun contains(value: T) =
            isAbove(value, from) && isLess(value, to)

    private fun isAbove(value: T, bound: Bound<T>) =
            if (bound.include) value.compareTo(bound.version) >= 0 else value.compareTo(bound.version) > 0

    private fun isLess(value: T, bound: Bound<T>) =
            if (bound.include) value.compareTo(bound.version) <= 0 else value.compareTo(bound.version) < 0

    override fun toString() =
        "${if(from.include) "[" else "("}$from, $to${if(to.include) "]" else ")"}"
}

private class MultiRange<T>(
        vararg val ranges: Range<T>)
    : Range<T> {
    override fun contains(value: T) =
            ranges.any { it.contains(value) }

    override fun toString() = ranges.map { it.toString() }.joinToString(", ")
}