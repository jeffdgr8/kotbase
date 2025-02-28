/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

const val CONFIG_FACTORY_TEST_STRING = "midway down the midway"

class CommonConfigFactoryTest : BaseTest() {
    @Test
    fun testFullTextIndexConfigurationFactory() {
        val config = FullTextIndexConfigurationFactory.newConfig(CONFIG_FACTORY_TEST_STRING)
        assertEquals(1, config.expressions.size)
        assertEquals(CONFIG_FACTORY_TEST_STRING, config.expressions[0])
    }

    @Test
    fun testFullTextIndexConfigurationFactoryWithProps() {
        val config = FullTextIndexConfigurationFactory.newConfig(
            CONFIG_FACTORY_TEST_STRING,
            language = "fr",
            ignoreAccents = true
        )
        assertEquals(1, config.expressions.size)
        assertEquals(CONFIG_FACTORY_TEST_STRING, config.expressions[0])
        assertEquals(true, config.isIgnoringAccents)
        assertEquals("fr", config.language)
    }

    @Test
    fun testFullTextIndexConfigurationFactoryNullExp() {
        assertFailsWith<IllegalArgumentException> { FullTextIndexConfigurationFactory.newConfig() }
    }

    @Test
    fun testFullTextIndexConfigurationFactoryCopy() {
        val config1 = FullTextIndexConfigurationFactory.newConfig(CONFIG_FACTORY_TEST_STRING)
        val config2 = config1.newConfig()
        assertNotEquals(config1, config2)
        assertEquals(1, config2.expressions.size)
        assertEquals(CONFIG_FACTORY_TEST_STRING, config2.expressions[0])
    }

    @Test
    fun testValueIndexConfigurationFactory() {
        val config = ValueIndexConfigurationFactory.newConfig(CONFIG_FACTORY_TEST_STRING)
        assertEquals(1, config.expressions.size)
        assertEquals(CONFIG_FACTORY_TEST_STRING, config.expressions[0])
    }

    @Test
    fun testValueIndexConfigurationFactoryNullExp() {
        assertFailsWith<IllegalArgumentException> { ValueIndexConfigurationFactory.newConfig() }
    }

    @Test
    fun testValueIndexConfigurationFactoryCopy() {
        val config1 = ValueIndexConfigurationFactory.newConfig(CONFIG_FACTORY_TEST_STRING)
        val config2 = config1.newConfig()
        assertNotEquals(config1, config2)
        assertEquals(1, config2.expressions.size)
        assertEquals(CONFIG_FACTORY_TEST_STRING, config2.expressions[0])
    }

    @Test
    fun testLogFileConfigurationFactory() {
        val config = LogFileConfigurationFactory.newConfig(directory = CONFIG_FACTORY_TEST_STRING, maxSize = 4096L)
        assertEquals(CONFIG_FACTORY_TEST_STRING, config.directory)
        assertEquals(4096L, config.maxSize)
    }

    @Test
    fun testLogFileConfigurationFactoryNullDir() {
        assertFailsWith<IllegalArgumentException> { LogFileConfigurationFactory.newConfig() }
    }

    @Test
    fun testLogFileConfigurationFactoryCopy() {
        val config1 = LogFileConfigurationFactory.newConfig(directory = CONFIG_FACTORY_TEST_STRING, maxSize = 4096L)
        val config2 = config1.newConfig(maxSize = 1024L)
        assertNotEquals(config1, config2)
        assertEquals(CONFIG_FACTORY_TEST_STRING, config2.directory)
        assertEquals(1024L, config2.maxSize)
    }
}
