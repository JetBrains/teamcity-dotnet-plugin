/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package jetbrains.buildServer.rx

import java.util.concurrent.atomic.AtomicBoolean

inline fun disposableOf(crossinline action: () -> Unit): Disposable = object : Disposable {
    var isDisposed = AtomicBoolean(false)
    override fun dispose() {
        if (isDisposed.compareAndSet(false, true)) {
            action()
        }
    }
}

fun disposableOf(vararg disposables: Disposable): Disposable = disposableOf { disposables.forEach { it.dispose() } }

fun Sequence<Disposable>.toDisposable(): Disposable = disposableOf { forEach { it.dispose() } }

fun emptyDisposable(): Disposable = object : Disposable {
    override fun dispose() = Unit
}

inline fun <T : Disposable?, R> T.use(block: (T) -> R): R {
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        when {
            this == null -> {
            }
            exception == null -> dispose()
            else ->
                try {
                    dispose()
                } catch (ex: Throwable) {
                }
        }
    }
}