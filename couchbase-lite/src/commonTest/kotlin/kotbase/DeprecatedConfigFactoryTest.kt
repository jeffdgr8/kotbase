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
@file:Suppress("DEPRECATION")

package kotbase

import kotlin.test.*

// The suite of tests that verifies behavior
// with a deleted default collection are in
// cbl-java-common @ a2de0d43d09ce64fd3a1301dc35
class DeprecatedConfigFactoryTest : BaseDbTest() {
    private val testEndpoint = URLEndpoint("ws://foo.couchbase.com/db")

    ///// Test ReplicatorConfiguration Factory

    @Test
    fun testReplicatorConfigNoArgs() {
        assertFailsWith<IllegalArgumentException> { ReplicatorConfigurationFactory.newConfig() }
    }

    // Create on factory with no db should fail
    @Test
    fun testReplicatorConfigNoDb() {
        assertFailsWith<IllegalArgumentException> {
            ReplicatorConfigurationFactory.newConfig(database = null, target = testEndpoint, type = ReplicatorType.PULL)
        }
    }

    // Create on factory with no target should fail
    @Test
    fun testReplicatorConfigNoProtocol() {
        assertFailsWith<IllegalArgumentException> {
            ReplicatorConfigurationFactory.newConfig(testDatabase, type = ReplicatorType.PULL)
        }
    }

    // Create with db and endpoint should succeed
    @Test
    fun testReplicatorConfigWithGoodArgs() {
        val config = ReplicatorConfigurationFactory.newConfig(testDatabase, testEndpoint)
        assertEquals(testDatabase, config.database)
        assertEquals(testEndpoint, config.target)
    }

    // Create should copy source
    @Test
    fun testReplicatorConfigCopy() {
        val config1 = ReplicatorConfigurationFactory.newConfig(testDatabase, testEndpoint, type = ReplicatorType.PULL)
        val config2 = config1.newConfig()
        assertNotSame(config1, config2)
        assertEquals(config1.database, config2.database)
        assertEquals(config1.target, config2.target)
        assertEquals(config1.type, config2.type)
    }

    // Create should replace source
    @Test
    fun testReplicatorConfigReplace() {
        val config1 = ReplicatorConfigurationFactory.newConfig(testDatabase, testEndpoint, type = ReplicatorType.PULL)
        val config2 = config1.newConfig(type = ReplicatorType.PUSH)
        assertNotSame(config1, config2)
        assertEquals(config1.database, config2.database)
        assertEquals(config1.target, config2.target)
        assertEquals(ReplicatorType.PUSH, config2.type)
    }

    // Create from a source explicitly specifying a default collection
    @Test
    fun testReplicatorConfigFromCollectionWithDefault() {
        val config1 = ReplicatorConfigurationFactory
            .newConfig(testEndpoint, mapOf(listOf(testDatabase.defaultCollection) to CollectionConfiguration()))
        val config2 = config1.newConfig()
        assertNotSame(config1, config2)
        assertEquals(config1.database, config2.database)
        assertEquals(setOf(testCollection.database.defaultCollection), config2.collections)
    }

    // Create from a source with default collection, explicitly specifying a non-default collection
    @Test
    fun testReplicatorConfigFromCollectionWithDefaultAndOther() {
        val config1 = ReplicatorConfigurationFactory
            .newConfig(testEndpoint, mapOf(listOf(testCollection) to CollectionConfiguration()))
        val filter: ReplicationFilter = { _, _ -> true }

        // Information gets lost here (the configuration of testCollection): should be a log message
        val config2 = config1.newConfig(pushFilter = filter)

        assertNotSame(config1, config2)
        assertEquals(config1.database, config2.database)

        val db = config1.database
        val defaultCollection = db.defaultCollection

        assertEquals(setOf(defaultCollection), config2.collections)
        assertEquals(filter, config2.getCollectionConfiguration(defaultCollection)?.pushFilter)
    }

    // Create with one of the parameters that has migrated to the collection configuration
    @Test
    fun testReplicatorFromCollectionWithLegacyParameter() {
        val config = ReplicatorConfigurationFactory.newConfig(testDatabase, testEndpoint, channels = listOf("boop"))
        assertEquals(testDatabase, config.database)
        assertEquals(testEndpoint, config.target)
        assertEquals(listOf("boop"), config.getCollectionConfiguration(testDatabase.defaultCollection)!!.channels)
    }

    // Create a collection style config from one built with the legacy call
    @Test
    fun testReplicatorConfigFromLegacy() {
        val config1 = ReplicatorConfigurationFactory.newConfig(testDatabase, testEndpoint, channels = listOf("boop"))
        val config2 = config1.newConfig(continuous = true)
        assertEquals(testDatabase, config2.database)
        assertEquals(testEndpoint, config2.target)
        val colls = config2.collections
        assertEquals(1, colls.size)
        val defaultCollection = testDatabase.defaultCollection
        assertTrue(colls.contains(defaultCollection))
        assertEquals(listOf("boop"), config2.getCollectionConfiguration(defaultCollection)!!.channels)
    }
}
