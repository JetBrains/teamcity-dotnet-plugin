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

package jetbrains.buildServer.dotnet.discovery

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.util.browser.Browser
import java.io.InputStream

class StreamFactoryImpl(private val _browser: Browser) : StreamFactory {
    override fun tryCreate(path: String): InputStream? {
        val element = _browser.getElement(path)
        if (element != null) {
            return element.inputStream
        }

        LOG.debug("Element \"#path\" was not found")
        return null
    }

    companion object {
        private val LOG: Logger = Logger.getInstance(StreamFactoryImpl::class.java.name)
    }
}