/*
 * Copyright 2025 Jeff Lockhart
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

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress("DEPRECATION")
class ConfigFactoryTest : BaseDbTest() {
    private val testAuthenticator = SessionAuthenticator("mysessionId")
    private val testHeaders = mapOf("Cookies" to "region=nw; city=sf")
    private val testChannels = listOf("channel1", "channel2")
    private val testResolver = ReplicatorConfiguration.DEFAULT_CONFLICT_RESOLVER
    private val testDocIds = listOf("doc1", "doc2")
    private val testPushFilter: ReplicationFilter = { _, _ -> true }
    private val testPullFilter: ReplicationFilter = { _, _ -> true }

    private var testEndpoint = URLEndpoint("ws://foo.couchbase.com/db")
    private lateinit var testPath: String

    @BeforeTest
    fun setUpEEConfigFactoryTest() {
        testPath = getScratchDirectoryPath(getUniqueName("confgFactoryTest"))
    }

    ///// Database Configuration

    @Test
    fun testDatabaseConfigurationFactoryDefaults() {
        val config = DatabaseConfigurationFactory.newConfig()
        assertEquals(Defaults.Database.FULL_SYNC, config.isFullSync)
    }

    @Test
    fun testDatabaseConfigurationFactory() {
        val config = DatabaseConfigurationFactory.newConfig(
            databasePath = testPath,
            fullSync = true
        )
        assertEquals(testPath, config.directory)
        assertTrue(config.isFullSync)
    }

    @Test
    fun testDatabaseConfigurationFactoryCopyWithChanges() {
        val config1 = DatabaseConfigurationFactory.newConfig(
            databasePath = testPath,
            fullSync = true
        )

        val config2 = config1.newConfig()

        assertEquals(testPath, config1.directory)
        assertTrue(config1.isFullSync)

        assertEquals(testPath, config2.directory)
        assertTrue(config2.isFullSync)
    }

    ///// ReplicatorConfiguration

    // Create config a config with no collection
    @Test
    fun testReplConfigNullCollections() {
        val target = testEndpoint
        val config = ReplicatorConfigurationFactory.newConfig(
            target = target,
            type = ReplicatorType.PUSH,
            continuous = true,
            authenticator = testAuthenticator,
            headers = testHeaders,
            maxAttempts = 20,
            heartbeat = 100,
            enableAutoPurge = false
        )

        assertEquals(target, config.target)
        assertEquals(ReplicatorType.PUSH, config.type)
        assertTrue(config.isContinuous)
        assertEquals(testAuthenticator, config.authenticator)
        assertEquals(testHeaders, config.headers)
        assertEquals(20, config.maxAttempts)
        assertEquals(100, config.heartbeat)
        assertEquals(false, config.isAutoPurgeEnabled)
    }

    // Create config with an empty collection
    @Test
    fun testReplConfigEmptyCollections() {
        val target = testEndpoint
        val config = ReplicatorConfigurationFactory.newConfig(
            collections = emptyMap(),
            target = target,
            type = ReplicatorType.PUSH,
            continuous = true,
            authenticator = testAuthenticator,
            headers = testHeaders,
            maxAttempts = 20,
            heartbeat = 100,
            enableAutoPurge = false
        )

        assertEquals(target, config.target)
        assertEquals(ReplicatorType.PUSH, config.type)
        assertTrue(config.isContinuous)
        assertEquals(testAuthenticator, config.authenticator)
        assertEquals(testHeaders, config.headers)
        assertEquals(20, config.maxAttempts)
        assertEquals(100, config.heartbeat)
        assertEquals(false, config.isAutoPurgeEnabled)
    }

    // Create config with explicitly configured default collection
    @Test
    fun testReplConfigCollectionsWithDefault() {
        val target = testEndpoint

        val collConfig1 = CollectionConfigurationFactory.newConfig(
            channels = testChannels,
            conflictResolver = testResolver,
            documentIDs = testDocIds,
            pushFilter = testPushFilter,
            pullFilter = testPullFilter
        )

        val collectionConfig = mapOf(setOf(testDatabase.defaultCollection) to collConfig1)
        val config = ReplicatorConfigurationFactory.newConfig(
            collections = collectionConfig,
            target = target,
            type = ReplicatorType.PUSH,
            continuous = true,
            authenticator = testAuthenticator,
            headers = testHeaders,
            maxAttempts = 20,
            heartbeat = 100,
            enableAutoPurge = false
        )

        assertEquals(collectionConfig.keys.first(), config.collections)
        assertEquals(target, config.target)
        assertEquals(ReplicatorType.PUSH, config.type)
        assertTrue(config.isContinuous)
        assertEquals(testAuthenticator, config.authenticator)
        assertEquals(testHeaders, config.headers)
        assertEquals(20, config.maxAttempts)
        assertEquals(100, config.heartbeat)
        assertEquals(false, config.isAutoPurgeEnabled)

        val collConfig2 = config.getCollectionConfiguration(testDatabase.defaultCollection)
        assertNotNull(collConfig2)
        assertNotSame(collConfig1, collConfig2)
        assertEquals(testChannels, collConfig2.channels)
        assertEquals(testResolver, collConfig2.conflictResolver)
        assertEquals(testDocIds, collConfig2.documentIDs)
        assertEquals(testPushFilter, collConfig2.pushFilter)
        assertEquals(testPullFilter, collConfig2.pullFilter)
    }

    // Create config with a configured non-default collection
    @Test
    fun testReplConfigCollectionsWithoutDefault() {
        val collConfig1 = CollectionConfigurationFactory.newConfig(
            channels = testChannels,
            conflictResolver = testResolver,
            documentIDs = testDocIds,
            pushFilter = testPushFilter,
            pullFilter = testPullFilter
        )
        val config = ReplicatorConfigurationFactory.newConfig(
            testEndpoint,
            mapOf(listOf(testCollection) to collConfig1),
            type = ReplicatorType.PUSH,
            continuous = true,
            authenticator = testAuthenticator,
            headers = testHeaders,
            maxAttempts = 20,
            heartbeat = 100,
            enableAutoPurge = false
        )

        assertEquals(testEndpoint, config.target)
        assertEquals(ReplicatorType.PUSH, config.type)
        assertTrue(config.isContinuous)
        assertEquals(testAuthenticator, config.authenticator)
        assertEquals(testHeaders, config.headers)
        assertEquals(20, config.maxAttempts)
        assertEquals(100, config.heartbeat)
        assertEquals(false, config.isAutoPurgeEnabled)

        val colls = config.collections
        assertNotNull(colls)
        assertEquals(1, colls.size)
        assertTrue(colls.contains(testCollection))

        val collConfig2 = config.getCollectionConfiguration(testCollection)

        assertNotNull(collConfig2)
        assertNotSame(collConfig1, collConfig2)
        assertEquals(testChannels, collConfig2.channels)
        assertEquals(testResolver, collConfig2.conflictResolver)
        assertEquals(testDocIds, collConfig2.documentIDs)
        assertEquals(testPushFilter, collConfig2.pushFilter)
        assertEquals(testPullFilter, collConfig2.pullFilter)
    }

    @Test
    fun testReplicatorConfigurationFactoryDataSources() {
        val config1 = ReplicatorConfigurationFactory.newConfig(testEndpoint)
        // config1 contains all default value

        assertEquals(testEndpoint, config1.target)
        assertEquals(ReplicatorType.PUSH_AND_PULL, config1.type)
        assertFalse(config1.isContinuous)
        assertNull(config1.authenticator)
        assertNull(config1.headers)
        assertEquals(Defaults.Replicator.MAX_ATTEMPTS_SINGLE_SHOT, config1.maxAttempts)
        assertEquals(Defaults.Replicator.HEARTBEAT, config1.heartbeat)
        // changing this property only affects the iOS platform, always the default value on other platforms with no effect
        assertEquals(Defaults.Replicator.ALLOW_REPLICATING_IN_BACKGROUND, config1.allowReplicatingInBackground)
        assertTrue(config1.isAutoPurgeEnabled)

        val config2 = config1.newConfig(maxAttempts = 200)

        assertNotSame(config1, config2)
        assertEquals(Defaults.Replicator.MAX_ATTEMPTS_SINGLE_SHOT, config1.maxAttempts)
        assertEquals(200, config2.maxAttempts)
    }
}
