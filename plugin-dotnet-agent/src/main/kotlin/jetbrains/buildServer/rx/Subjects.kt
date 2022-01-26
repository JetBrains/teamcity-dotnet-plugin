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

fun <T> subjectOf(): Subject<T> = object : Subject<T> {
    private val observers: MutableList<Observer<T>> = mutableListOf()
    private var isCompleted: Boolean = false

    override fun subscribe(observer: Observer<T>): Disposable {
        synchronized(observers) {
            if (isCompleted) {
                observer.onComplete()
                return@synchronized
            }

            observers.add(observer)
        }

        return disposableOf {
            synchronized(observers) {
                observers.remove(observer)
            }
        }
    }

    override fun onNext(value: T) =
            synchronized(observers) {
                for (observer in observers) {
                    observer.onNext(value)
                }
            }

    override fun onError(error: Exception) =
            synchronized(observers) {
                for (observer in observers) {
                    observer.onError(error)
                }

                finish()
            }

    override fun onComplete() =
            synchronized(observers) {
                for (observer in observers) {
                    observer.onComplete()
                }

                finish()
            }

    private fun finish() {
        observers.clear()
        isCompleted = true
    }
}

fun <T> emptySubject(): Subject<T> = object : Subject<T> {
    override fun subscribe(observer: Observer<T>): Disposable = emptyDisposable()
    override fun onNext(value: T) = Unit
    override fun onError(error: Exception) = Unit
    override fun onComplete() = Unit
}