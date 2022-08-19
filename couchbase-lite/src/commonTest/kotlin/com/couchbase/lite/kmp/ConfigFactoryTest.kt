package com.couchbase.lite.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

const val CONFIG_FACTORY_TEST_STRING = "midway down the midway"

class ConfigFactoryTest : BaseTest() {

    @Test
    fun testFullTextIndexConfigurationFactory() {
        val config = FullTextIndexConfigurationFactory.create(CONFIG_FACTORY_TEST_STRING)
        assertEquals(1, config.expressions.size)
        assertEquals(CONFIG_FACTORY_TEST_STRING, config.expressions[0])
    }

    @Test
    fun testFullTextIndexConfigurationFactoryWithProps() {
        val config = FullTextIndexConfigurationFactory.create(
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
        assertFailsWith<IllegalArgumentException> {
            FullTextIndexConfigurationFactory.create()
        }
    }

    @Test
    fun testFullTextIndexConfigurationFactoryCopy() {
        val config1 = FullTextIndexConfigurationFactory.create(CONFIG_FACTORY_TEST_STRING)
        val config2 = config1.create()
        assertNotEquals(config1, config2)
        assertEquals(1, config2.expressions.size)
        assertEquals(CONFIG_FACTORY_TEST_STRING, config2.expressions[0])
    }

    @Test
    fun testValueIndexConfigurationFactory() {
        val config = ValueIndexConfigurationFactory.create(CONFIG_FACTORY_TEST_STRING)
        assertEquals(1, config.expressions.size)
        assertEquals(CONFIG_FACTORY_TEST_STRING, config.expressions[0])
    }

    @Test
    fun testValueIndexConfigurationFactoryNullExp() {
        assertFailsWith<IllegalArgumentException> {
            ValueIndexConfigurationFactory.create()
        }
    }

    @Test
    fun testValueIndexConfigurationFactoryCopy() {
        val config1 = ValueIndexConfigurationFactory.create(CONFIG_FACTORY_TEST_STRING)
        val config2 = config1.create()
        assertNotEquals(config1, config2)
        assertEquals(1, config2.expressions.size)
        assertEquals(CONFIG_FACTORY_TEST_STRING, config2.expressions[0])
    }

    @Test
    fun testLogFileConfigurationFactory() {
        val config = LogFileConfigurationFactory.create(
            directory = CONFIG_FACTORY_TEST_STRING,
            maxSize = 4096L
        )
        assertEquals(CONFIG_FACTORY_TEST_STRING, config.directory)
        assertEquals(4096L, config.maxSize)
    }

    @Test
    fun testLogFileConfigurationFactoryNullDir() {
        assertFailsWith<IllegalArgumentException> {
            LogFileConfigurationFactory.create()
        }
    }

    @Test
    fun testLogFileConfigurationFactoryCopy() {
        val config1 = LogFileConfigurationFactory.create(
            directory = CONFIG_FACTORY_TEST_STRING,
            maxSize = 4096L
        )
        val config2 = config1.create(maxSize = 1024L)
        assertNotEquals(config1, config2)
        assertEquals(CONFIG_FACTORY_TEST_STRING, config2.directory)
        assertEquals(1024L, config2.maxSize)
    }
}
