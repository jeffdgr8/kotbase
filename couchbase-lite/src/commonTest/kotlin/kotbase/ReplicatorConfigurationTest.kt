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

import kotlin.test.*

class ReplicatorConfigurationTest : BaseReplicatorTest() {

    @Test
    fun testIllegalMaxAttempts() {
        assertFailsWith<IllegalArgumentException> { makeSimpleReplConfig(maxAttempts = -1) }
    }

    @Test
    fun testMaxAttemptsZero() {
        makeSimpleReplConfig(maxAttempts = 0)
    }

    @Test
    fun testIllegalAttemptsWaitTime() {
        assertFailsWith<IllegalArgumentException> { makeSimpleReplConfig(maxAttemptWaitTime = -1) }
    }

    @Test
    fun testMaxAttemptsWaitTimeZero() {
        makeSimpleReplConfig(maxAttemptWaitTime = 0)
    }

    @Test
    fun testIllegalHeartbeatMin() {
        assertFailsWith<IllegalArgumentException> { makeSimpleReplConfig().heartbeat = -1 }
    }

    @Test
    fun testHeartbeatZero() {
        makeSimpleReplConfig().heartbeat = 0
    }

    @Test
    fun testIllegalHeartbeatMax() {
        assertFailsWith<IllegalArgumentException> { makeSimpleReplConfig().heartbeat = 2147484 }
    }

    // Internal API tests

    // Can't test the EE parameter (self-signed only) here
//    @Test
//    fun testCreateConfigDefaults() {
//        val config = ReplicatorConfiguration(mockURLEndpoint)
//        config.addCollection(testCollection, null)
//
//        val immutableConfig = ImmutableReplicatorConfiguration(config)
//        assertEquals(Defaults.Replicator.TYPE, immutableConfig.type)
//        assertEquals(Defaults.Replicator.CONTINUOUS, immutableConfig.isContinuous)
//
//        val opts = immutableConfig.connectionOptions
//        assertEquals(Defaults.Replicator.HEARTBEAT, opts[C4Replicator.REPLICATOR_HEARTBEAT_INTERVAL])
//        assertEquals(Defaults.Replicator.MAX_ATTEMPTS_SINGLE_SHOT - 1, opts[C4Replicator.REPLICATOR_OPTION_MAX_RETRIES])
//        assertEquals(Defaults.Replicator.MAX_ATTEMPT_WAIT_TIME, opts[C4Replicator.REPLICATOR_OPTION_MAX_RETRY_INTERVAL])
//        assertEquals(Defaults.Replicator.ENABLE_AUTO_PURGE, opts[C4Replicator.REPLICATOR_OPTION_ENABLE_AUTO_PURGE])
//        assertEquals(
//            Defaults.Replicator.ACCEPT_PARENT_COOKIES,
//            opts[C4Replicator.REPLICATOR_OPTION_ACCEPT_PARENT_COOKIES]
//        )
//    }

    // Can't test the EE parameter (self-signed only) here
//    @Test
//    fun testCreateConfigCompatibility() {
//        val config = ReplicatorConfiguration(mockURLEndpoint)
//        config.addCollection(testCollection, null)
//
//        config.heartbeat = 6
//        config.maxAttempts = 6
//        config.maxAttemptWaitTime = 6
//
//        val opts1 = ImmutableReplicatorConfiguration(config).connectionOptions
//        assertEquals(6, opts1[C4Replicator.REPLICATOR_HEARTBEAT_INTERVAL])
//        assertEquals(6, opts1[C4Replicator.REPLICATOR_OPTION_MAX_RETRY_INTERVAL])
//        assertEquals(6 - 1, opts1[C4Replicator.REPLICATOR_OPTION_MAX_RETRIES])
//
//        config.heartbeat = 0
//        config.maxAttempts = 0
//        config.maxAttemptWaitTime = 0
//
//        val opts2 = ImmutableReplicatorConfiguration(config).connectionOptions
//        assertEquals(Defaults.Replicator.HEARTBEAT, opts2[C4Replicator.REPLICATOR_HEARTBEAT_INTERVAL])
//        assertEquals(
//            Defaults.Replicator.MAX_ATTEMPT_WAIT_TIME,
//            opts2[C4Replicator.REPLICATOR_OPTION_MAX_RETRY_INTERVAL]
//        )
//        assertEquals(
//            Defaults.Replicator.MAX_ATTEMPTS_SINGLE_SHOT - 1,
//            opts2[C4Replicator.REPLICATOR_OPTION_MAX_RETRIES]
//        )
//    }

    /****************** Scopes and Collections Section 8.13 ****************/


    // 8.13.1a Create a config object with ReplicatorConfiguration.init(database: database,
    // endpoint: endpoint).
    //
    // Access collections property. The returned collections will have one collection
    // which is the default collection.
    //
    // Access database property, and the database object from the init should be
    // returned.
    @Suppress("DEPRECATION")
    @Test
    fun testCreateConfigWithDatabaseA() {
        val replConfig = ReplicatorConfiguration(testDatabase, mockURLEndpoint)
        val collections = replConfig.collections
        assertEquals(1, collections.size)
        assertTrue(collections.contains(testDatabase.defaultCollection))
        assertEquals(testDatabase, replConfig.database)
    }

    // 8.13.1b Create a config object with ReplicatorConfiguration.init(database: database,
    // endpoint: endpoint).
    //
    // Access collections property. The returned collections will have one collection
    // which is the default collection.
    //
    // Call getCollectionConfig() method with the default collection. A
    // CollectionConfiguration object should be returned.
    //
    // Check CollectionConfiguration.conflictResolver, .pushFilter, pullFilters,
    // channels, and documentIDs. The return object of those properties should be NULL.
    @Suppress("DEPRECATION")
    @Test
    fun testCreateConfigWithDatabaseB() {
        val collectionConfig = ReplicatorConfiguration(testDatabase, mockURLEndpoint)
            .getCollectionConfiguration(testDatabase.defaultCollection)
        assertNotNull(collectionConfig)
        assertNull(collectionConfig.conflictResolver)
        assertNull(collectionConfig.pushFilter)
        assertNull(collectionConfig.pullFilter)
        assertNull(collectionConfig.channels)
        assertNull(collectionConfig.documentIDs)
    }

    // 8.13.2 Create a config object with ReplicatorConfiguration.init(database: database,
    // endpoint: endpoint).
    //
    // Set ReplicatorConfiguration.conflictResolver with a conflict resolver.
    //
    // Call getCollectionConfig() method with the default collection. A
    // CollectionConfiguration object should be returned.
    //
    // Check CollectionConfiguration.conflictResolver. The returned conflict resolver
    // should be the same as ReplicatorConfiguration.conflictResolver.
    @Suppress("DEPRECATION")
    @Test
    fun testCreateConfigWithDatabaseAndConflictResolver() {
        val resolver = localResolver
        val replConfig = ReplicatorConfiguration(testDatabase, mockURLEndpoint).setConflictResolver(resolver)
        assertEquals(resolver, replConfig.conflictResolver)
        val collectionConfig = replConfig.getCollectionConfiguration(testDatabase.defaultCollection)
        assertNotNull(collectionConfig)
        assertEquals(resolver, collectionConfig.conflictResolver)
    }

    // 8.13.3Create a config object with ReplicatorConfiguration.init(database: database,
    // endpoint: endpoint).
    //
    // Set ReplicatorConfiguration.conflictResolver with a conflict resolver.
    //
    // Call getCollectionConfig() method with the default collection. Check
    // CollectionConfiguration.conflictResolver. The conflict resolver should be the
    // same as ReplicatorConfiguration.conflictResolver.
    //
    // Update ReplicatorConfiguration.conflictResolver with a new conflict resolver.
    //
    // Call getCollectionConfig() method with the default collection. Check
    // CollectionConfiguration.conflictResolver. The conflict resolver should be
    // updated accordingly.
    //
    // Update CollectionConfiguration.conflictResolver with a new conflict resolver.
    // Use addCollection() method to add the default collection with the updated
    // config.
    //
    // Check ReplicatorConfiguration.conflictResolver. The conflict resolver should be
    // updated accordingly.
    @Suppress("DEPRECATION")
    @Test
    fun testUpdateConflictResolverForDefaultCollection() {
        val resolver = localResolver
        val replConfig = ReplicatorConfiguration(testDatabase, mockURLEndpoint).setConflictResolver(resolver)
        assertEquals(
            replConfig.conflictResolver,
            replConfig.getCollectionConfiguration(testDatabase.defaultCollection)?.conflictResolver
        )
        val resolver2 = localResolver
        replConfig.conflictResolver = resolver2
        assertEquals(
            resolver2,
            replConfig.getCollectionConfiguration(testDatabase.defaultCollection)?.conflictResolver
        )
    }

    // 8.13.4 Create a config object with ReplicatorConfiguration.init(database:
    // database, endpoint: endpoint).
    //
    // Set values to ReplicatorConfiguration.pushFilter, pullFilters, channels, and
    // documentIDs
    //
    // Call getCollectionConfig() method with the default collection. A
    // CollectionConfiguration object should be returned. The filters in the config
    // should be the same ReplicatorConfiguration.pushFilter, pullFilters, channels,
    // and documentIDs.
    @Suppress("DEPRECATION")
    @Test
    fun testCreateConfigWithDatabaseAndFilters() {
        val pushFilter1: ReplicationFilter = { _, _ -> true }
        val pullFilter1: ReplicationFilter = { _, _ -> true }
        val replConfig1 = ReplicatorConfiguration(testDatabase, mockURLEndpoint)
            .setPushFilter(pushFilter1)
            .setPullFilter(pullFilter1)
            .setChannels(listOf("CNBC", "ABC"))
            .setDocumentIDs(listOf("doc1", "doc2"))
        assertEquals(pushFilter1, replConfig1.pushFilter)
        assertEquals(pullFilter1, replConfig1.pullFilter)
        assertContentEquals(arrayOf("CNBC", "ABC"), replConfig1.channels?.toTypedArray())
        assertContentEquals(arrayOf("doc1", "doc2"), replConfig1.documentIDs?.toTypedArray())

        val collectionConfig1 = replConfig1.getCollectionConfiguration(testDatabase.defaultCollection)
        assertNotNull(collectionConfig1)
        assertEquals(pushFilter1, collectionConfig1.pushFilter)
        assertEquals(pullFilter1, collectionConfig1.pullFilter)
        assertContentEquals(arrayOf("CNBC", "ABC"), collectionConfig1.channels?.toTypedArray())
        assertContentEquals(arrayOf("doc1", "doc2"), collectionConfig1.documentIDs?.toTypedArray())
    }

    // 8.13.5a Create a config object with ReplicatorConfiguration.init(database: database,
    // endpoint: endpoint).
    //
    // Set values to ReplicatorConfiguration.pushFilter, pullFilters, channels, and
    // documentIDs.
    //
    // Call getCollectionConfig() method with the default collection. A
    // CollectionConfiguration object should be returned. The filters in the config
    // should be the same ReplicatorConfiguration.pushFilter, pullFilters, channels,
    // and documentIDs.
    //
    // Update ReplicatorConfiguration.pushFilter, pullFilters, channels, and
    // documentIDs with new values.
    //
    // Call getCollectionConfig() method with the default collection object getting
    // from the database. A CollectionConfiguration object should be returned. The
    // filters in the config be updated accordingly.
    //
    // Update CollectionConfiguration.pushFilter, pullFilters, channels, and
    // documentIDs with new values. Use addCollection() method to add the default
    // collection with the updated config.
    //
    // Check ReplicatorConfiguration.pushFilter, pullFilters, channels, and
    // documentIDs. The filters should be updated accordingly.
    @Suppress("DEPRECATION")
    @Test
    fun testUpdateFiltersForDefaultCollectionA() {
        val pushFilter1: ReplicationFilter = { _, _ -> true }
        val pullFilter1: ReplicationFilter = { _, _ -> true }
        val replConfig1 = ReplicatorConfiguration(testDatabase, mockURLEndpoint)
            .setPushFilter(pushFilter1)
            .setPullFilter(pullFilter1)
            .setChannels(listOf("CNBC", "ABC"))
            .setDocumentIDs(listOf("doc1", "doc2"))
        assertEquals(pushFilter1, replConfig1.pushFilter)
        assertEquals(pullFilter1, replConfig1.pullFilter)
        assertContentEquals(arrayOf("CNBC", "ABC"), replConfig1.channels?.toTypedArray())
        assertContentEquals(arrayOf("doc1", "doc2"), replConfig1.documentIDs?.toTypedArray())

        val collectionConfig1 = replConfig1.getCollectionConfiguration(testDatabase.defaultCollection)
        assertNotNull(collectionConfig1)
        assertEquals(pushFilter1, collectionConfig1.pushFilter)
        assertEquals(pullFilter1, collectionConfig1.pullFilter)
        assertContentEquals(arrayOf("CNBC", "ABC"), collectionConfig1.channels?.toTypedArray())
        assertContentEquals(arrayOf("doc1", "doc2"), collectionConfig1.documentIDs?.toTypedArray())

        val pushFilter2: ReplicationFilter = { _, _ -> true }
        val pullFilter2: ReplicationFilter = { _, _ -> true }
        replConfig1
            .setPushFilter(pushFilter2)
            .setPullFilter(pullFilter2)
            .setChannels(listOf("Peacock", "History")).documentIDs = listOf("doc3")

        assertEquals(pushFilter1, collectionConfig1.pushFilter)
        assertEquals(pullFilter1, collectionConfig1.pullFilter)
        assertContentEquals(arrayOf("CNBC", "ABC"), collectionConfig1.channels?.toTypedArray())
        assertContentEquals(arrayOf("doc1", "doc2"), collectionConfig1.documentIDs?.toTypedArray())

        val collectionConfig2 = replConfig1.getCollectionConfiguration(testDatabase.defaultCollection)
        assertNotNull(collectionConfig2)
        assertEquals(pushFilter2, collectionConfig2.pushFilter)
        assertEquals(pullFilter2, collectionConfig2.pullFilter)
        assertContentEquals(arrayOf("Peacock", "History"), collectionConfig2.channels?.toTypedArray())
        assertContentEquals(arrayOf("doc3"), collectionConfig2.documentIDs?.toTypedArray())
    }


    // 8.13.5b Create a config object with ReplicatorConfiguration.init(database: database,
    // endpoint: endpoint).
    //
    // Set values to ReplicatorConfiguration.pushFilter, pullFilters, channels, and
    // documentIDs.
    //
    // Call getCollectionConfig() method with the default collection. A
    // CollectionConfiguration object should be returned. The filters in the config
    // should be the same ReplicatorConfiguration.pushFilter, pullFilters, channels,
    // and documentIDs.
    //
    // Call getCollectionConfig() method with the default collection object getting
    // from the database. A CollectionConfiguration object should be returned. The
    // filters in the config be updated accordingly.
    //
    // Update CollectionConfiguration.pushFilter, pullFilters, channels, and
    // documentIDs with new values. Use addCollection() method to add the default
    // collection with the updated config.
    //
    // Check ReplicatorConfiguration.pushFilter, pullFilters, channels, and
    // documentIDs. The filters should be updated accordingly.
    @Suppress("DEPRECATION")
    @Test
    fun testUpdateFiltersForDefaultCollectionB() {
        val pushFilter1: ReplicationFilter = { _, _ -> true }
        val pullFilter1: ReplicationFilter = { _, _ -> true }
        val replConfig1 = ReplicatorConfiguration(testDatabase, mockURLEndpoint)
            .setPushFilter(pushFilter1)
            .setPullFilter(pullFilter1)
            .setChannels(listOf("CNBC", "ABC"))
            .setDocumentIDs(listOf("doc1", "doc2"))
        assertEquals(pushFilter1, replConfig1.pushFilter)
        assertEquals(pullFilter1, replConfig1.pullFilter)
        assertContentEquals(arrayOf("CNBC", "ABC"), replConfig1.channels?.toTypedArray())
        assertContentEquals(arrayOf("doc1", "doc2"), replConfig1.documentIDs?.toTypedArray())

        val collectionConfig1 = replConfig1.getCollectionConfiguration(testDatabase.defaultCollection)
        assertNotNull(collectionConfig1)
        assertEquals(pushFilter1, collectionConfig1.pushFilter)
        assertEquals(pullFilter1, collectionConfig1.pullFilter)
        assertContentEquals(arrayOf("CNBC", "ABC"), collectionConfig1.channels?.toTypedArray())
        assertContentEquals(arrayOf("doc1", "doc2"), collectionConfig1.documentIDs?.toTypedArray())

        val pushFilter2: ReplicationFilter = { _, _ -> true }
        val pullFilter2: ReplicationFilter = { _, _ -> true }
        val collectionConfig2 = CollectionConfiguration()
            .setPushFilter(pushFilter2)
            .setPullFilter(pullFilter2)
            .setChannels(listOf("Peacock", "History"))
            .setDocumentIDs(listOf("doc3"))
        replConfig1.addCollection(testDatabase.defaultCollection, collectionConfig2)

        assertEquals(pushFilter2, replConfig1.pushFilter)
        assertEquals(pullFilter2, replConfig1.pullFilter)
        assertContentEquals(arrayOf("Peacock", "History"), replConfig1.channels?.toTypedArray())
        assertContentEquals(arrayOf("doc3"), replConfig1.documentIDs?.toTypedArray())

        val collectionConfig3 = replConfig1.getCollectionConfiguration(testDatabase.defaultCollection)
        assertNotNull(collectionConfig3)
        assertEquals(pushFilter2, collectionConfig3.pushFilter)
        assertEquals(pullFilter2, collectionConfig3.pullFilter)
        assertContentEquals(arrayOf("Peacock", "History"), collectionConfig3.channels?.toTypedArray())
        assertContentEquals(arrayOf("doc3"), collectionConfig3.documentIDs?.toTypedArray())
    }

    // 8.13.6a Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
    // Access collections property and an empty collection list should be returned.\
    @Test
    fun testCreateConfigWithEndpointOnly1() {
        val replConfig1 = ReplicatorConfiguration(mockURLEndpoint)

        val collections = replConfig1.collections
        assertNotNull(collections)
        assertTrue(collections.isEmpty())
    }

    // 8.13.6b Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
    // Access collections property and an empty collection list should be returned.
    // Access database property and Illegal State Exception will be thrown.
    @Suppress("DEPRECATION")
    @Test
    fun testCreateConfigWithEndpointOnly2() {
        val replConfig1 = ReplicatorConfiguration(mockURLEndpoint)
        assertFailsWith<IllegalStateException> { replConfig1.database }
    }

    // 8.13.7 Create Collection "colA" and "colB" in the scope "scopeA".
    //
    // Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
    //
    // Use addCollections() to add both colA and colB to the config without specifying
    // a collection config.
    //
    // Check  ReplicatorConfiguration.collections. The collections should have colA and
    // colB.
    //
    // Use getCollectionConfig() to get the collection config for colA and colB. Check
    // the returned configs of both collections. The returned configs should be
    // different instances. The conflict resolver and filters of both configs should be
    // all NULL.
    @Test
    fun testAddCollectionsWithoutCollectionConfig() {
        val collectionA = testDatabase.createCollection("colA", "scopeA")
        val collectionB = testDatabase.createCollection("colB", "scopeA")

        val replConfig1 = ReplicatorConfiguration(mockURLEndpoint)
        replConfig1.addCollections(setOf(collectionA, collectionB), null)

        val collectionConfig1 = replConfig1.getCollectionConfiguration(collectionA)
        assertNotNull(collectionConfig1)
        assertNull(collectionConfig1.conflictResolver)
        assertNull(collectionConfig1.pushFilter)
        assertNull(collectionConfig1.pullFilter)
        assertNull(collectionConfig1.channels)
        assertNull(collectionConfig1.documentIDs)

        val collectionConfig2 = replConfig1.getCollectionConfiguration(collectionB)
        assertNotNull(collectionConfig2)
        assertNull(collectionConfig1.conflictResolver)
        assertNull(collectionConfig1.pushFilter)
        assertNull(collectionConfig1.pullFilter)
        assertNull(collectionConfig1.channels)
        assertNull(collectionConfig1.documentIDs)

        assertNotSame(collectionConfig1, collectionConfig2)
    }

    // 8.13.8 Create Collection "colA" and "colB" in the scope "scopeA".
    //
    // Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
    //
    // Create a CollectionConfiguration object, and set a conflictResolver and all
    // filters.
    //
    // Use addCollections() to add both colA and colB to the config created from the
    // previous step.
    //
    // Check  ReplicatorConfiguration.collections. The collections should have colA and
    // colB.
    //
    // Use getCollectionConfig() to get the collection config for colA and colB. The
    // returned configs of both collections should be different instances. The conflict
    // resolver and filters of both configs should be the same as what was specified
    // when calling addCollections().
    @Test
    fun testAddCollectionsWithCollectionConfig() {
        val collectionA = testDatabase.createCollection("colA", "scopeA")
        val collectionB = testDatabase.createCollection("colB", "scopeA")

        val replConfig1 = ReplicatorConfiguration(mockURLEndpoint)

        val pushFilter1: ReplicationFilter = { _, _ -> true }
        val pullFilter1: ReplicationFilter = { _, _ -> true }
        val resolver = localResolver
        val collectionConfig0 = CollectionConfiguration()
            .setPushFilter(pushFilter1)
            .setPullFilter(pullFilter1)
            .setConflictResolver(resolver)
        replConfig1.addCollections(setOf(collectionA, collectionB), collectionConfig0)

        val collectionConfig1 = replConfig1.getCollectionConfiguration(collectionA)
        assertNotNull(collectionConfig1)
        assertEquals(pushFilter1, collectionConfig1.pushFilter)
        assertEquals(pullFilter1, collectionConfig1.pullFilter)
        assertEquals(resolver, collectionConfig1.conflictResolver)

        val collectionConfig2 = replConfig1.getCollectionConfiguration(collectionB)
        assertNotNull(collectionConfig2)
        assertEquals(pushFilter1, collectionConfig2.pushFilter)
        assertEquals(pullFilter1, collectionConfig2.pullFilter)
        assertEquals(resolver, collectionConfig2.conflictResolver)

        assertNotSame(collectionConfig1, collectionConfig2)
    }

    // 8.13.9 Create Collection "colA" and "colB" in the scope "scopeA".
    //
    // Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
    //
    // Use addCollection() to add the colA without specifying a collection config.
    //
    // Create a CollectionConfiguration object, and set a conflictResolver and all
    // filters.
    //
    // Use addCollection() to add the colB with the collection config created from the
    // previous step.
    //
    // Check  ReplicatorConfiguration.collections. The collections should have colA and
    // colB.
    //
    // Use getCollectionConfig() to get the collection config for colA and colB. The
    // returned config of the colA should contain all NULL values. The returned config
    // of the colB should contain the values according to the config used when adding
    // the collection.
    @Test
    fun testAddCollection() {
        val collectionA = testDatabase.createCollection("colA", "scopeA")
        val collectionB = testDatabase.createCollection("colB", "scopeA")

        val replConfig1 = ReplicatorConfiguration(mockURLEndpoint)

        replConfig1.addCollection(collectionA, null)

        val pushFilter1: ReplicationFilter = { _, _ -> true }
        val pullFilter1: ReplicationFilter = { _, _ -> true }
        val resolver = localResolver
        val collectionConfig0 = CollectionConfiguration()
            .setPushFilter(pushFilter1)
            .setPullFilter(pullFilter1)
            .setConflictResolver(resolver)
        replConfig1.addCollection(collectionB, collectionConfig0)

        assertTrue(replConfig1.collections.contains(collectionA))
        assertTrue(replConfig1.collections.contains(collectionB))

        val collectionConfig1 = replConfig1.getCollectionConfiguration(collectionA)
        assertNotNull(collectionConfig1)
        assertNull(collectionConfig1.pushFilter)
        assertNull(collectionConfig1.pullFilter)
        assertNull(collectionConfig1.conflictResolver)

        val collectionConfig2 = replConfig1.getCollectionConfiguration(collectionB)
        assertNotNull(collectionConfig2)
        assertEquals(pushFilter1, collectionConfig2.pushFilter)
        assertEquals(pullFilter1, collectionConfig2.pullFilter)
        assertEquals(resolver, collectionConfig2.conflictResolver)
    }

    // 8.13.10a Create Collection "colA" and "colB" in the scope "scopeA".
    //
    // Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
    //
    // Create a CollectionConfiguration object, and set a conflictResolver and all
    // filters.
    //
    // Use addCollection() to add the colA and colB with the collection config created
    // from the previous step.
    //
    // Check  ReplicatorConfiguration.collections. The collections should have colA and
    // colB.
    //
    // Use getCollectionConfig() to get the collection config for colA and colB. Check
    // the returned configs of both collections and ensure that both configs contain
    // the values correctly.
    //
    // Use addCollection() to add colA again without specifying collection config.
    //
    // Create a new CollectionConfiguration object, and set a conflictResolver and all
    // filters.
    //
    // Use addCollection() to add colB again with the updated collection config created
    // from the previous step.
    //
    // Use getCollectionConfig() to get the collection config for colA and colB. Check
    // the returned configs of both collections and ensure that both configs contain
    // the updated values correctly.
    @Test
    fun testUpdateCollectionConfigA() {
        val collectionA = testDatabase.createCollection("colA", "scopeA")
        val collectionB = testDatabase.createCollection("colB", "scopeA")

        val replConfig1 = ReplicatorConfiguration(mockURLEndpoint)

        val pushFilter1: ReplicationFilter = { _, _ -> true }
        val pullFilter1: ReplicationFilter = { _, _ -> true }
        val resolver1 = localResolver
        val collectionConfig0 = CollectionConfiguration()
            .setPushFilter(pushFilter1)
            .setPullFilter(pullFilter1)
            .setConflictResolver(resolver1)

        replConfig1.addCollection(collectionA, collectionConfig0)
        replConfig1.addCollection(collectionB, collectionConfig0)


        assertTrue(replConfig1.collections.contains(collectionA))
        assertTrue(replConfig1.collections.contains(collectionB))

        val collectionConfig1 = replConfig1.getCollectionConfiguration(collectionA)
        assertNotNull(collectionConfig1)
        assertEquals(pushFilter1, collectionConfig1.pushFilter)
        assertEquals(pullFilter1, collectionConfig1.pullFilter)
        assertEquals(resolver1, collectionConfig1.conflictResolver)

        val collectionConfig2 = replConfig1.getCollectionConfiguration(collectionB)
        assertNotNull(collectionConfig2)
        assertEquals(pushFilter1, collectionConfig2.pushFilter)
        assertEquals(pullFilter1, collectionConfig2.pullFilter)
        assertEquals(resolver1, collectionConfig2.conflictResolver)

        val pushFilter2: ReplicationFilter = { _, _ -> true }
        val pullFilter2: ReplicationFilter = { _, _ -> true }
        val resolver2 = localResolver
        val collectionConfig3 = CollectionConfiguration()
            .setPushFilter(pushFilter2)
            .setPullFilter(pullFilter2)
            .setConflictResolver(resolver2)

        replConfig1.addCollection(collectionA, null)
        replConfig1.addCollection(collectionB, collectionConfig3)

        val collectionConfig4 = replConfig1.getCollectionConfiguration(collectionA)
        assertNotNull(collectionConfig3)
        assertNull(collectionConfig4!!.pushFilter)
        assertNull(collectionConfig4.pullFilter)
        assertNull(collectionConfig4.conflictResolver)

        val collectionConfig5 = replConfig1.getCollectionConfiguration(collectionB)
        assertNotNull(collectionConfig5)
        assertEquals(pushFilter2, collectionConfig5.pushFilter)
        assertEquals(pullFilter2, collectionConfig5.pullFilter)
        assertEquals(resolver2, collectionConfig5.conflictResolver)
    }


    // 8.13.10a Create Collection "colA" and "colB" in the scope "scopeA".
    //
    // Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
    //
    // Create a CollectionConfiguration object, and set a conflictResolver and all
    // filters.
    //
    // Use addCollection() to add the colA and colB with the collection config created
    // from the previous step.
    //
    // Check  ReplicatorConfiguration.collections. The collections should have colA and
    // colB.
    //
    // Use getCollectionConfig() to get the collection config for colA and colB. Check
    // the returned configs of both collections and ensure that both configs contain
    // the values correctly.
    //
    // Use addCollection() to add colA again without specifying collection config.
    //
    // Create a new CollectionConfiguration object, and set a conflictResolver and all
    // filters.
    //
    // Use addCollection() to add colB again with the updated collection config created
    // from the previous step.
    //
    // Use getCollectionConfig() to get the collection config for colA and colB. Check
    // the returned configs of both collections and ensure that both configs contain
    // the updated values correctly.
    @Suppress("DEPRECATION")
    @Test
    fun testUpdateCollectionConfigB() {
        val defaultCollection = testDatabase.defaultCollection
        val collectionA = testDatabase.createCollection("colA", "scopeA")

        val filter: ReplicationFilter = { _, _ -> true }

        val replConfig = ReplicatorConfiguration(mockURLEndpoint)
        var collConfig = CollectionConfiguration(null, null, filter, null, null)

        replConfig.addCollections(listOf(defaultCollection, collectionA), collConfig)

        collConfig = replConfig.getCollectionConfiguration(defaultCollection)!!
        assertEquals(filter, collConfig.pullFilter)
        assertEquals(null, collConfig.pushFilter)

        collConfig = replConfig.getCollectionConfiguration(collectionA)!!
        assertEquals(filter, collConfig.pullFilter)
        assertEquals(null, collConfig.pushFilter)

        replConfig.pushFilter = filter

        collConfig = replConfig.getCollectionConfiguration(defaultCollection)!!
        assertEquals(filter, collConfig.pullFilter)
        assertEquals(filter, collConfig.pushFilter)

        collConfig = replConfig.getCollectionConfiguration(collectionA)!!
        assertEquals(filter, collConfig.pullFilter)
        assertEquals(null, collConfig.pushFilter)
    }

    // 8.13.11 Create Collection "colA" and "colB" in the scope "scopeA".
    //
    // Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
    //
    // Create a CollectionConfiguration object, and set a conflictResolvers and all
    // filters.
    //
    // Use addCollections() to add both colA and colB to the config with the
    // CollectionConfiguration created from the previous step.
    //
    // Check  ReplicatorConfiguration.collections. The collections should have colA and
    // colB.
    //
    // Use getCollectionConfig() to get the collection config for colA and colB. Check
    // the returned config of both collections and ensure that both configs contain the
    // values correctly.
    //
    // Remove "colB" by calling removeCollection().
    //
    // Check  ReplicatorConfiguration.collections. The collections should have only
    // colA.
    //
    // Use getCollectionConfig() to get the collection config for colA and colB. The
    // returned config for the colB should be NULL.
    @Test
    fun testRemoveCollection() {
        val collectionA = testDatabase.createCollection("colA", "scopeA")
        val collectionB = testDatabase.createCollection("colB", "scopeA")

        val replConfig1 = ReplicatorConfiguration(mockURLEndpoint)

        val pushFilter1: ReplicationFilter = { _, _ -> true }
        val pullFilter1: ReplicationFilter = { _, _ -> true }
        val resolver1 = localResolver
        val collectionConfig0 = CollectionConfiguration()
            .setPushFilter(pushFilter1)
            .setPullFilter(pullFilter1)
            .setConflictResolver(resolver1)

        replConfig1.addCollection(collectionA, collectionConfig0)
        replConfig1.addCollection(collectionB, collectionConfig0)

        assertTrue(replConfig1.collections.contains(collectionA))
        assertTrue(replConfig1.collections.contains(collectionB))

        replConfig1.removeCollection(collectionB)
        assertTrue(replConfig1.collections.contains(collectionA))
        assertFalse(replConfig1.collections.contains(collectionB))

        assertNotNull(replConfig1.getCollectionConfiguration(collectionA))
        assertNull(replConfig1.getCollectionConfiguration(collectionB))
    }

    // 8.13.12a Create collection "colA" in the scope "scopeA" using database instance A.
    //
    // Create collection "colB" in the scope "scopeA" using database instance B.
    //
    // Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
    //
    // Use addCollections() to add both colA and colB. An invalid argument exception
    // should be thrown as the collections are from different database instances.
    //
    // Use addCollection() to add colA. Ensure that the colA has been added correctly.
    //
    // Use addCollection() to add colB. An invalid argument exception should be thrown
    // as the collections are from different database instances.
    @Test
    fun testAddCollectionsFromDifferentDatabaseInstancesA() {
        val collectionA = testDatabase.createCollection("colA", "scopeA")
        val collectionB = targetDatabase.createCollection("colB", "scopeA")

        val replConfig1 = ReplicatorConfiguration(mockURLEndpoint)

        assertFailsWith<IllegalArgumentException> {
            replConfig1.addCollections(setOf(collectionA, collectionB), null)
        }
    }

    // 8.13.12b Create collection "colA" in the scope "scopeA" using database instance A.
    //
    // Create collection "colB" in the scope "scopeA" using database instance B.
    //
    // Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
    //
    // Use addCollections() to add both colA and colB. An invalid argument exception
    // should be thrown as the collections are from different database instances.
    //
    // Use addCollection() to add colA. Ensure that the colA has been added correctly.
    //
    // Use addCollection() to add colB. An invalid argument exception should be thrown
    // as the collections are from different database instances.
    @Test
    fun testAddCollectionsFromDifferentDatabaseInstancesB() {
        val collectionA = testDatabase.createCollection("colA", "scopeA")
        val collectionB = targetDatabase.createCollection("colB", "scopeA")

        val replConfig1 = ReplicatorConfiguration(mockURLEndpoint)

        replConfig1.addCollection(collectionA, null)

        assertFailsWith<IllegalArgumentException> { replConfig1.addCollection(collectionB, null) }
    }

    // 8.13.13a Create collection "colA" in the scope "scopeA" using database instance A.
    //
    // Create collection "colB" in the scope "scopeA" using database instance B.
    //
    // Delete collection colB.
    //
    // Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
    //
    // Use addCollections() to add both colA and colB. An invalid argument exception should be thrown as an added collection has been deleted.
    //
    // Use addCollection() to add colA. Ensure that the colA has been added correctly.
    //
    // Use addCollection() to add colB. An invalid argument exception should be thrown as an added collection has been deleted.
    @Test
    fun testAddDeletedCollectionsA() {
        val collectionA = testDatabase.createCollection("colA", "scopeA")
        val collectionB = targetDatabase.createCollection("colB", "scopeA")

        testDatabase.deleteCollection("colB", "scopeA")

        assertFailsWith<IllegalArgumentException> {
            ReplicatorConfiguration(mockURLEndpoint).addCollections(setOf(collectionA, collectionB), null)
        }
    }


    // 8.13.13a Create collection "colA" in the scope "scopeA" using database instance A.
    //
    // Create collection "colB" in the scope "scopeA" using database instance B.
    //
    // Delete collection colB.
    //
    // Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
    //
    // Use addCollections() to add both colA and colB. An invalid argument exception should be thrown as an added collection has been deleted.
    //
    // Use addCollection() to add colA. Ensure that the colA has been added correctly.
    //
    // Use addCollection() to add colB. An invalid argument exception should be thrown as an added collection has been deleted.
    @Test
    fun testAddDeletedCollectionsB() {
        val collectionA = testDatabase.createCollection("colA", "scopeA")
        targetDatabase.createCollection("colB", "scopeA")

        testDatabase.deleteCollection("colB", "scopeA")

        val replConfig1 = ReplicatorConfiguration(mockURLEndpoint)

        replConfig1.addCollection(collectionA, null)

        val collections = replConfig1.collections
        assertEquals(1, collections.size)
        assertTrue(collections.contains(collectionA))
    }


    // 8.13.13c Create collection "colA" in the scope "scopeA" using database instance A.
    //
    // Create collection "colB" in the scope "scopeA" using database instance B.
    //
    // Delete collection colB.
    //
    // Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
    //
    // Use addCollections() to add both colA and colB. An invalid argument exception should be thrown as an added collection has been deleted.
    //
    // Use addCollection() to add colA. Ensure that the colA has been added correctly.
    //
    // Use addCollection() to add colB. An invalid argument exception should be thrown as an added collection has been deleted.
    @Test
    fun testAddDeletedCollectionsC() {
        testDatabase.createCollection("colA", "scopeA")
        val collectionB = targetDatabase.createCollection("colB", "scopeA")

        targetDatabase.deleteCollection("colB", "scopeA")

        assertFailsWith<IllegalArgumentException> {
            ReplicatorConfiguration(mockURLEndpoint).addCollection(collectionB, null)
        }
    }

    // CBL-3736
    // Attempting to configure a replicator with no collection
    // should throw an illegal argument exception.
    @Test
    fun testCreateReplicatorWithNoCollections() {
        assertFailsWith<IllegalArgumentException> { Replicator(ReplicatorConfiguration(mockURLEndpoint)) }
    }

    // CBL-3736
    // After the last collection from a scope is deleted,
    // an attempt to get the scope should return null
    @Test
    fun testUseScopeAfterScopeDeleted() {
        assertNotNull(testDatabase.createCollection("colA", "scopeA"))

        testDatabase.deleteCollection("colA", "scopeA")

        assertNull(testDatabase.getScope("scopeA"))
    }

    // CBL-3736
    // An attempt to get a collection from a closed database
    // should throw a CouchbaseLiteException
    @Test
    fun testUseScopeAfterDBClosed() {
        assertNotNull(testDatabase.createCollection("colA", "scopeA"))

        testDatabase.close()

        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.NOT_OPEN) {
            testDatabase.getCollection("colA", "scopeA")
        }
    }
}
