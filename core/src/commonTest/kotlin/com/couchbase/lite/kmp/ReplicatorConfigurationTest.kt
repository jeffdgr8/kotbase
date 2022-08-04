//// TODO: 3.1 API
////
//// Copyright (c) 2022 Couchbase, Inc All rights reserved.
////
//// Licensed under the Apache License, Version 2.0 (the "License");
//// you may not use this file except in compliance with the License.
//// You may obtain a copy of the License at
////
//// http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing, software
//// distributed under the License is distributed on an "AS IS" BASIS,
//// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//// See the License for the specific language governing permissions and
//// limitations under the License.
////
//package com.couchbase.lite.kmp
//
//import kotlin.test.*
//
//class ReplicatorConfigurationTest : BaseReplicatorTest() {
//
//    //     1: Create a config object with ReplicatorConfiguration.init(database, endpoint).
//    //     2: Access collections property. It must have one collection which is the default collection.
//    //     6: ReplicatorConfiguration.database should be the database with which the configuration was created
//    @Test
//    fun testCreateConfigWithDatabase1() {
//        val replConfig = ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint)
//        val collections = replConfig.collections
//        assertEquals(1, collections?.size)
//        assertTrue(collections?.contains(baseTestDb.defaultCollection) ?: false)
//        assertEquals(baseTestDb, replConfig.database)
//    }
//
//    //     1: Create a config object with ReplicatorConfiguration.init(database, endpoint).
//    //     3: Calling getCollectionConfig() with the default collection should produce a CollectionConfiguration
//    //     4: CollectionConfiguration.collection should be the default collection.
//    //     5: CollectionConfiguration.conflictResolver, pushFilter, pullFilter, channels, and documentIDs
//    //        should be null.
//    @Test
//    fun testCreateConfigWithDatabase2() {
//        val collectionConfig = ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint)
//            .getCollectionConfiguration(baseTestDb.defaultCollection!!)
//        assertNotNull(collectionConfig)
//        assertNull(collectionConfig!!.conflictResolver)
//        assertNull(collectionConfig.pushFilter)
//        assertNull(collectionConfig.pullFilter)
//        assertNull(collectionConfig.channels)
//        assertNull(collectionConfig.documentIDs)
//    }
//
//    //     1: Create a config object with ReplicatorConfiguration.init(database: database, endpoint: endpoint).
//    //     2: Set ReplicatorConfiguration.conflictResolver with a conflict resolver.
//    //     3: Calling getCollectionConfig() with the default collection should produce a CollectionConfiguration
//    //     4: CollectionConfiguration.conflictResolver should be the same as ReplicatorConfiguration.conflictResolver.
//    @Test
//    fun testCreateConfigWithDatabaseAndConflictResolver() {
//        val resolver: ConflictResolver = { conflict -> conflict.localDocument!! }
//        val replConfig = ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint).setConflictResolver(resolver)
//        assertEquals(resolver, replConfig.conflictResolver)
//        val collectionConfig = replConfig.getCollectionConfiguration(baseTestDb.defaultCollection!!)
//        assertNotNull(collectionConfig)
//        assertEquals(resolver, collectionConfig?.conflictResolver)
//    }
//
//    //     1: Create a config object with ReplicatorConfiguration.init(database: database, endpoint: endpoint).
//    //     2: Set ReplicatorConfiguration.conflictResolver with a conflict resolver.
//    //     3: Verify that CollectionConfiguration.conflictResolver and ReplicatorConfiguration.conflictResolver
//    //        are the same resolver..
//    //     4: Update ReplicatorConfiguration.conflictResolver with a new conflict resolver.
//    //     5-7: Verify that CollectionConfiguration.conflictResolver is still the same
//    //          as ReplicatorConfiguration.conflictResolver..
//    @Test
//    fun testUpdateConflictResolverForDefaultCollection() {
//        val resolver: ConflictResolver = { conflict -> conflict.localDocument!! }
//        val replConfig = ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint).setConflictResolver(resolver)
//        assertEquals(
//            replConfig.conflictResolver,
//            replConfig.getCollectionConfiguration(baseTestDb.defaultCollection!!)?.conflictResolver
//        )
//        val resolver2: ConflictResolver = { conflict -> conflict.localDocument }
//        replConfig.conflictResolver = resolver2
//        assertEquals(
//            resolver2,
//            replConfig.getCollectionConfiguration(baseTestDb.defaultCollection!!)?.conflictResolver
//        )
//    }
//
//    //     1: Create a config object with ReplicatorConfiguration.init(database: database, endpoint: endpoint).
//    //     2: Set values to ReplicatorConfiguration.pushFilter, pullFilters, channels, and documentIDs
//    //     3: Call getCollectionConfig() method with the default collection.
//    //       A CollectionConfiguration object should be returned and the properties in the config should
//    //       be the same as the corresponding properties in ReplicatorConfiguration
//    @Test
//    fun testCreateConfigWithDatabaseAndFilters() {
//        val pushFilter1: ReplicationFilter = { _, _ -> true }
//        val pullFilter1: ReplicationFilter = { _, _ -> true }
//        val replConfig1 = ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint)
//            .setPushFilter(pushFilter1)
//            .setPullFilter(pullFilter1)
//            .setChannels(listOf("CNBC", "ABC"))
//            .setDocumentIDs(listOf("doc1", "doc2"))
//        assertEquals(pushFilter1, replConfig1.pushFilter)
//        assertEquals(pullFilter1, replConfig1.pullFilter)
//        assertContentEquals(listOf("CNBC", "ABC"), replConfig1.channels)
//        assertContentEquals(listOf("doc1", "doc2"), replConfig1.documentIDs)
//
//        val collectionConfig1 = replConfig1.getCollectionConfiguration(baseTestDb.defaultCollection!!)
//        assertNotNull(collectionConfig1)
//        assertEquals(pushFilter1, collectionConfig1!!.pushFilter)
//        assertEquals(pullFilter1, collectionConfig1.pullFilter)
//        assertContentEquals(listOf("CNBC", "ABC"), collectionConfig1.channels)
//        assertContentEquals(listOf("doc1", "doc2"), collectionConfig1.documentIDs)
//    }
//
//    //     1: Create a config object with ReplicatorConfiguration.init(database: database, endpoint: endpoint).
//    //     2: Set values to ReplicatorConfiguration.pushFilter, pullFilters, channels, and documentIDs.
//    //     3: Call getCollectionConfig() method with the default collection.
//    //       A CollectionConfiguration object should be returned and the properties in the config should
//    //       be the same as the corresponding properties in ReplicatorConfiguration
//    //     4: Update ReplicatorConfiguration.pushFilter, pullFilters, channels, and documentIDs with new values.
//    //     5: Repeat #3. The previously obtains ReplicatorConfiguration should not change.
//    //        The new one should have the new values.
//    //     6: Update CollectionConfiguration.pushFilter, pullFilters, channels, and documentIDs with new values.
//    //        Use addCollection() method to add the default collection with the updated config.
//    //     7: Check ReplicatorConfiguration.pushFilter, pullFilters, channels, and documentIDs.
//    //        The filters should be updated accordingly.
//    @Test
//    fun testUpdateFiltersForDefaultCollection1() {
//        val pushFilter1: ReplicationFilter = { _, _ -> true }
//        val pullFilter1: ReplicationFilter = { _, _ -> true }
//        val replConfig1 = ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint)
//            .setPushFilter(pushFilter1)
//            .setPullFilter(pullFilter1)
//            .setChannels(listOf("CNBC", "ABC"))
//            .setDocumentIDs(listOf("doc1", "doc2"))
//        assertEquals(pushFilter1, replConfig1.pushFilter)
//        assertEquals(pullFilter1, replConfig1.pullFilter)
//        assertContentEquals(listOf("CNBC", "ABC"), replConfig1.channels)
//        assertContentEquals(listOf("doc1", "doc2"), replConfig1.documentIDs)
//
//        val collectionConfig1 = replConfig1.getCollectionConfiguration(baseTestDb.defaultCollection!!)
//        assertNotNull(collectionConfig1)
//        assertEquals(pushFilter1, collectionConfig1!!.pushFilter)
//        assertEquals(pullFilter1, collectionConfig1.pullFilter)
//        assertContentEquals(listOf("CNBC", "ABC"), collectionConfig1.channels)
//        assertContentEquals(listOf("doc1", "doc2"), collectionConfig1.documentIDs)
//
//        val pushFilter2: ReplicationFilter = { _, _ -> true }
//        val pullFilter2: ReplicationFilter = { _, _ -> true }
//        replConfig1
//            .setPushFilter(pushFilter2)
//            .setPullFilter(pullFilter2)
//            .setChannels(listOf("Peacock", "History")).documentIDs = listOf("doc3")
//
//        assertEquals(pushFilter1, collectionConfig1.pushFilter)
//        assertEquals(pullFilter1, collectionConfig1.pullFilter)
//        assertContentEquals(listOf("CNBC", "ABC"), collectionConfig1.channels)
//        assertContentEquals(listOf("doc1", "doc2"), collectionConfig1.documentIDs)
//
//        val collectionConfig2 = replConfig1.getCollectionConfiguration(baseTestDb.defaultCollection!!)
//        assertNotNull(collectionConfig2)
//        assertEquals(pushFilter2, collectionConfig2.pushFilter)
//        assertEquals(pullFilter2, collectionConfig2.pullFilter)
//        assertContentEquals(listOf("Peacock", "History"), collectionConfig2.channels)
//        assertContentEquals(listOf("doc3"), collectionConfig2.documentIDs)
//    }
//
//    //     1: Create a config object with ReplicatorConfiguration.init(database: database, endpoint: endpoint).
//    //     2: Set values to ReplicatorConfiguration.pushFilter, pullFilters, channels, and documentIDs.
//    //     3: Call getCollectionConfig() method with the default collection.
//    //       A CollectionConfiguration object should be returned and the properties in the config should
//    //       be the same as the corresponding properties in ReplicatorConfiguration
//    //     6: Update CollectionConfiguration.pushFilter, pullFilters, channels, and documentIDs with new values.
//    //        Use addCollection() method to add the default collection with the updated config.
//    //     7: Check ReplicatorConfiguration.pushFilter, pullFilters, channels, and documentIDs.
//    //        The filters should be updated accordingly.
//    @Test
//    fun testUpdateFiltersForDefaultCollection2() {
//        val pushFilter1: ReplicationFilter = { _, _ -> true }
//        val pullFilter1: ReplicationFilter = { _, _ -> true }
//        val replConfig1 = ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint)
//            .setPushFilter(pushFilter1)
//            .setPullFilter(pullFilter1)
//            .setChannels(listOf("CNBC", "ABC"))
//            .setDocumentIDs(listOf("doc1", "doc2"))
//        assertEquals(pushFilter1, replConfig1.pushFilter)
//        assertEquals(pullFilter1, replConfig1.pullFilter)
//        assertContentEquals(listOf("CNBC", "ABC"), replConfig1.channels)
//        assertContentEquals(listOf("doc1", "doc2"), replConfig1.documentIDs)
//
//        val collectionConfig1 = replConfig1.getCollectionConfiguration(baseTestDb.defaultCollection!!)
//        assertNotNull(collectionConfig1)
//        assertEquals(pushFilter1, collectionConfig1!!.pushFilter)
//        assertEquals(pullFilter1, collectionConfig1.pullFilter)
//        assertContentEquals(listOf("CNBC", "ABC"), collectionConfig1.channels)
//        assertContentEquals(listOf("doc1", "doc2"), collectionConfig1.documentIDs)
//
//        val pushFilter2: ReplicationFilter = { _, _ -> true }
//        val pullFilter2: ReplicationFilter = { _, _ -> true }
//        val collectionConfig2 = CollectionConfiguration()
//            .setPushFilter(pushFilter2)
//            .setPullFilter(pullFilter2)
//            .setChannels(listOf("Peacock", "History"))
//            .setDocumentIDs(listOf("doc3"))
//        replConfig1.addCollection(baseTestDb.defaultCollection!!, collectionConfig2)
//
//        assertEquals(pushFilter2, replConfig1.pushFilter)
//        assertEquals(pullFilter2, replConfig1.pullFilter)
//        assertContentEquals(listOf("Peacock", "History"), replConfig1.channels)
//        assertContentEquals(listOf("doc3"), replConfig1.documentIDs)
//
//        val collectionConfig3 = replConfig1.getCollectionConfiguration(baseTestDb.defaultCollection!!)
//        assertNotNull(collectionConfig3)
//        assertEquals(pushFilter2, collectionConfig3!!.pushFilter)
//        assertEquals(pullFilter2, collectionConfig3.pullFilter)
//        assertContentEquals(listOf("Peacock", "History"), collectionConfig3.channels)
//        assertContentEquals(listOf("doc3"), collectionConfig3.documentIDs)
//    }
//
//    //     1: Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
//    //     2: Access collections property and an empty collection list should be returned.
//    @Test
//    fun testCreateConfigWithEndpointOnly1() {
//        val replConfig1 = ReplicatorConfiguration(remoteTargetEndpoint)
//
//        val collections = replConfig1.collections
//        assertNotNull(collections)
//        assertTrue(collections!!.isEmpty())
//    }
//
//    //     1: Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
//    //     3: Access database property and Illegal State Exception will be thrown.
//    @Test
//    fun testCreateConfigWithEndpointOnly2() {
//        assertFailsWith<IllegalStateException> {
//            val replConfig1 = ReplicatorConfiguration(remoteTargetEndpoint)
//
//            replConfig1.database
//        }
//    }
//
//    //     1: Create Collection "colA" and "colB" in the scope "scopeA".
//    //     2: Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
//    //     3: Use addCollections() to add both colA and colB to the config without specifying a collection config.
//    //     4: Check  ReplicatorConfiguration.collections. The collections should have colA and colB.
//    //     5: Use getCollectionConfig() to get the collection config for colA and colB.
//    //        Both should be non-null, they should be different instances and he conflict resolver and filters be null.
//    @Test
//    fun testAddCollectionsWithoutCollectionConfig() {
//        val collectionA = baseTestDb.createCollection("colA", "scopeA")
//        val collectionB = baseTestDb.createCollection("colB", "scopeA")
//
//        val replConfig1 = ReplicatorConfiguration(remoteTargetEndpoint)
//        replConfig1.addCollections(setOf(collectionA, collectionB), null)
//
//        val collectionConfig1 = replConfig1.getCollectionConfiguration(collectionA)
//        assertNotNull(collectionConfig1)
//        assertNull(collectionConfig1!!.conflictResolver)
//        assertNull(collectionConfig1.pushFilter)
//        assertNull(collectionConfig1.pullFilter)
//        assertNull(collectionConfig1.channels)
//        assertNull(collectionConfig1.documentIDs)
//
//        val collectionConfig2 = replConfig1.getCollectionConfiguration(collectionB)
//        assertNotNull(collectionConfig2)
//        assertNull(collectionConfig1.conflictResolver)
//        assertNull(collectionConfig1.pushFilter)
//        assertNull(collectionConfig1.pullFilter)
//        assertNull(collectionConfig1.channels)
//        assertNull(collectionConfig1.documentIDs)
//
//        assertNotSame(collectionConfig1, collectionConfig2)
//    }
//
//    //     1: Create Collection "colA" and "colB" in the scope "scopeA".
//    //     2: Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
//    //     3: Create a CollectionConfiguration object, and set a conflictResolver and all filters.
//    //     4: Use addCollections() to add both colA and colB to the config created in the previous step.
//    //     5: Check  ReplicatorConfiguration.collections. The collections should have colA and colB.
//    //     6: Use getCollectionConfig() to get the collection config for colA and colB.
//    //        Both should be non-null, they should be different instances and he conflict resolver and filters
//    //        should be as assigned.
//    @Test
//    fun testAddCollectionsWithCollectionConfig() {
//        val collectionA = baseTestDb.createCollection("colA", "scopeA")
//        val collectionB = baseTestDb.createCollection("colB", "scopeA")
//
//        val replConfig1 = ReplicatorConfiguration(remoteTargetEndpoint)
//
//        val pushFilter1: ReplicationFilter = { _, _ -> true }
//        val pullFilter1: ReplicationFilter = { _, _ -> true }
//        val resolver: ConflictResolver = { conflict -> conflict.localDocument!! }
//        val collectionConfig0 = CollectionConfiguration()
//            .setPushFilter(pushFilter1)
//            .setPullFilter(pullFilter1)
//            .setConflictResolver(resolver)
//        replConfig1.addCollections(setOf(collectionA, collectionB), collectionConfig0)
//
//        val collectionConfig1 = replConfig1.getCollectionConfiguration(collectionA)
//        assertNotNull(collectionConfig1)
//        assertEquals(pushFilter1, collectionConfig1!!.pushFilter)
//        assertEquals(pullFilter1, collectionConfig1.pullFilter)
//        assertEquals(resolver, collectionConfig1.conflictResolver)
//
//        val collectionConfig2 = replConfig1.getCollectionConfiguration(collectionB)
//        assertNotNull(collectionConfig2)
//        assertEquals(pushFilter1, collectionConfig2!!.pushFilter)
//        assertEquals(pullFilter1, collectionConfig2.pullFilter)
//        assertEquals(resolver, collectionConfig2.conflictResolver)
//
//        assertNotSame(collectionConfig1, collectionConfig2)
//    }
//
//    //     1: Create Collection "colA" and "colB" in the scope "scopeA".
//    //     2: Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
//    //     3: Use addCollection() to add the colA without specifying a collection config.
//    //     4: Create a CollectionConfiguration object, and set a conflictResolver and all filters.
//    //     5: Use addCollection() to add the colB with the collection config created from the previous step.
//    //     6: Check  ReplicatorConfiguration.collections. The collections should have colA and colB.
//    //     7: Use getCollectionConfig() to get the collection config for colA and colB.
//    //        All of the properties for colA's config should be null. The properties for colB should
//    //        be be those passed in the configuration used to add it..
//    @Test
//    fun testAddCollection() {
//        val collectionA = baseTestDb.createCollection("colA", "scopeA")
//        val collectionB = baseTestDb.createCollection("colB", "scopeA")
//
//        val replConfig1 = ReplicatorConfiguration(remoteTargetEndpoint)
//
//        replConfig1.addCollection(collectionA, null)
//
//        val pushFilter1: ReplicationFilter = { _, _ -> true }
//        val pullFilter1: ReplicationFilter = { _, _ -> true }
//        val resolver: ConflictResolver = { conflict -> conflict.localDocument!! }
//        val collectionConfig0 = CollectionConfiguration()
//            .setPushFilter(pushFilter1)
//            .setPullFilter(pullFilter1)
//            .setConflictResolver(resolver)
//        replConfig1.addCollection(collectionB, collectionConfig0)
//
//        assertTrue(replConfig1.collections?.contains(collectionA) ?: false)
//        assertTrue(replConfig1.collections?.contains(collectionB) ?: false)
//
//        val collectionConfig1 = replConfig1.getCollectionConfiguration(collectionA)
//        assertNotNull(collectionConfig1)
//        assertNull(collectionConfig1!!.pushFilter)
//        assertNull(collectionConfig1.pullFilter)
//        assertNull(collectionConfig1.conflictResolver)
//
//        val collectionConfig2 = replConfig1.getCollectionConfiguration(collectionB)
//        assertNotNull(collectionConfig2)
//        assertEquals(pushFilter1, collectionConfig2!!.pushFilter)
//        assertEquals(pullFilter1, collectionConfig2.pullFilter)
//        assertEquals(resolver, collectionConfig2.conflictResolver)
//    }
//
//    //     1: Create Collection "colA" and "colB" in the scope "scopeA".
//    //     2: Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
//    //     3: Create a CollectionConfiguration object, and set a conflictResolver and all filters.
//    //     4: Use addCollection() to add the colA and colB with the collection config created from the previous step.
//    //     5: Check  ReplicatorConfiguration.collections. The collections should have colA and colB.
//    //     6: Use getCollectionConfig() to get the collection config for colA and colB. Check the returned configs
//    //        for both collections and ensure that both configs contain the correct values.
//    //     7: Use addCollection() to add colA again without specifying collection config.
//    //     8: Create a new CollectionConfiguration object, and set a conflictResolver and all filters.
//    //     9: Use addCollection() to add colB again with the updated collection config created from the previous step.
//    //     10: Use getCollectionConfig() to get the collection config for colA and colB.
//    //         Check the configs for both collections and ensure that they contain the updated values.
//    @Test
//    fun testUpdateCollectionConfig() {
//        val collectionA = baseTestDb.createCollection("colA", "scopeA")
//        val collectionB = baseTestDb.createCollection("colB", "scopeA")
//
//        val replConfig1 = ReplicatorConfiguration(remoteTargetEndpoint)
//
//        val pushFilter1: ReplicationFilter = { _, _ -> true }
//        val pullFilter1: ReplicationFilter = { _, _ -> true }
//        val resolver1: ConflictResolver = { conflict -> conflict.localDocument!! }
//        val collectionConfig0 = CollectionConfiguration()
//            .setPushFilter(pushFilter1)
//            .setPullFilter(pullFilter1)
//            .setConflictResolver(resolver1)
//
//        replConfig1.addCollection(collectionA, collectionConfig0)
//        replConfig1.addCollection(collectionB, collectionConfig0)
//
//
//        assertTrue(replConfig1.collections?.contains(collectionA) ?: false)
//        assertTrue(replConfig1.collections?.contains(collectionB) ?: false)
//
//        val collectionConfig1 = replConfig1.getCollectionConfiguration(collectionA)
//        assertNotNull(collectionConfig1)
//        assertEquals(pushFilter1, collectionConfig1!!.pushFilter)
//        assertEquals(pullFilter1, collectionConfig1.pullFilter)
//        assertEquals(resolver1, collectionConfig1.conflictResolver)
//
//        val collectionConfig2 = replConfig1.getCollectionConfiguration(collectionB)
//        assertNotNull(collectionConfig2)
//        assertEquals(pushFilter1, collectionConfig2!!.pushFilter)
//        assertEquals(pullFilter1, collectionConfig2.pullFilter)
//        assertEquals(resolver1, collectionConfig2.conflictResolver)
//
//        val pushFilter2: ReplicationFilter = { _, _ -> true }
//        val pullFilter2: ReplicationFilter = { _, _ -> true }
//        val resolver2: ConflictResolver = { conflict -> conflict.localDocument!! }
//        val collectionConfig3 = CollectionConfiguration()
//            .setPushFilter(pushFilter2)
//            .setPullFilter(pullFilter2)
//            .setConflictResolver(resolver2)
//
//        replConfig1.addCollection(collectionA, null)
//        replConfig1.addCollection(collectionB, collectionConfig3)
//
//        val collectionConfig4 = replConfig1.getCollectionConfiguration(collectionA)
//        assertNotNull(collectionConfig3)
//        assertNull(collectionConfig4!!.pushFilter)
//        assertNull(collectionConfig4.pullFilter)
//        assertNull(collectionConfig4.conflictResolver)
//
//        val collectionConfig5 = replConfig1.getCollectionConfiguration(collectionB)
//        assertNotNull(collectionConfig5)
//        assertEquals(pushFilter2, collectionConfig5!!.pushFilter)
//        assertEquals(pullFilter2, collectionConfig5.pullFilter)
//        assertEquals(resolver2, collectionConfig5.conflictResolver)
//    }
//
//    //     1: Create Collection "colA" and "colB" in the scope "scopeA".
//    //     2: Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
//    //     3: Create a CollectionConfiguration object, and set a conflictResolvers and all filters.
//    //     4: Use addCollections() to add both colA and colB to the config with the CollectionConfiguration.
//    //     5: Check ReplicatorConfiguration.collections. The collections should have colA and colB.
//    //     6: ...
//    //     7: Remove "colB" by calling removeCollection().
//    //     8: Check  ReplicatorConfiguration.collections. The collections should have only colA.
//    //     9: Use getCollectionConfig() to get the collection config for colA and colB.
//    //        All of colB's properties should be null.
//    @Test
//    fun testRemoveCollection() {
//        val collectionA = baseTestDb.createCollection("colA", "scopeA")
//        val collectionB = baseTestDb.createCollection("colB", "scopeA")
//
//        val replConfig1 = ReplicatorConfiguration(remoteTargetEndpoint)
//
//        val pushFilter1: ReplicationFilter = { _, _ -> true }
//        val pullFilter1: ReplicationFilter = { _, _ -> true }
//        val resolver1: ConflictResolver = { conflict -> conflict.localDocument!! }
//        val collectionConfig0 = CollectionConfiguration()
//            .setPushFilter(pushFilter1)
//            .setPullFilter(pullFilter1)
//            .setConflictResolver(resolver1)
//
//        replConfig1.addCollection(collectionA, collectionConfig0)
//        replConfig1.addCollection(collectionB, collectionConfig0)
//
//        assertTrue(replConfig1.collections?.contains(collectionA) ?: false)
//        assertTrue(replConfig1.collections?.contains(collectionB) ?: false)
//
//        replConfig1.removeCollection(collectionB)
//        assertTrue(replConfig1.collections?.contains(collectionA) ?: false)
//        assertFalse(replConfig1.collections?.contains(collectionB) ?: false)
//
//        assertNotNull(replConfig1.getCollectionConfiguration(collectionA))
//        assertNull(replConfig1.getCollectionConfiguration(collectionB))
//    }
//
//    //     1: Create collection "colA" in the scope "scopeA" using database instance A.
//    //     2: Create collection "colB" in the scope "scopeA" using database instance B.
//    //     3: Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
//    //     4: Use addCollections() to add both colA and colB. This should cause an InvalidArgumentException.
//    @Test
//    fun testAddCollectionsFromDifferentDatabaseInstances1() {
//        assertFailsWith<IllegalArgumentException> {
//            val collectionA = baseTestDb.createCollection("colA", "scopeA")
//            val collectionB = otherDB.createCollection("colB", "scopeA")
//
//            val replConfig1 = ReplicatorConfiguration(remoteTargetEndpoint)
//
//            replConfig1.addCollections(setOf(collectionA, collectionB), null)
//        }
//    }
//
//    //     1: Create collection "colA" in the scope "scopeA" using database instance A.
//    //     2: Create collection "colB" in the scope "scopeA" using database instance B.
//    //     3: Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
//    //     4: Use addCollections() to add both colA and colB. This should cause an InvalidArgumentException.
//    //     5: Use addCollection() to add colA. Ensure that the colA has been added correctly.
//    //     6: Use addCollection() to add colB. This should cause an InvalidArgumentException.
//    @Test
//    fun testAddCollectionsFromDifferentDatabaseInstances2() {
//        assertFailsWith<IllegalArgumentException> {
//            val collectionA = baseTestDb.createCollection("colA", "scopeA")
//            val collectionB = otherDB.createCollection("colB", "scopeA")
//
//            val replConfig1 = ReplicatorConfiguration(remoteTargetEndpoint)
//
//            replConfig1.addCollection(collectionA, null)
//            replConfig1.addCollection(collectionB, null)
//        }
//    }
//
//    //     1: Create collection "colA" in the scope "scopeA" using database instance A.
//    //     2: Create collection "colB" in the scope "scopeA" using database instance B.
//    //     3: Delete collection colB
//    //     4: Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
//    //     5: Use addCollections() to add both colA and colB. This should cause an InvalidArgumentException.
//    @Test
//    fun testAddDeletedCollections1() {
//        assertFailsWith<IllegalArgumentException> {
//            val collectionA = baseTestDb.createCollection("colA", "scopeA")
//            val collectionB = baseTestDb.createCollection("colB", "scopeA")
//
//            baseTestDb.deleteCollection("colB", "scopeA")
//
//            ReplicatorConfiguration(remoteTargetEndpoint)
//                .addCollections(setOf(collectionA, collectionB), null)
//        }
//    }
//
//    //     1: Create collection "colA" in the scope "scopeA" using database instance A.
//    //     2: Create collection "colB" in the scope "scopeA" using database instance B.
//    //     3: Delete collection colB
//    //     4: Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
//    //     6: Use addCollection() to add colA. Ensure that the colA has been added correctly.
//    fun testAddDeletedCollections2() {
//        val collectionA = baseTestDb.createCollection("colA", "scopeA")
//        val collectionB = baseTestDb.createCollection("colB", "scopeA")
//
//        baseTestDb.deleteCollection("colB", "scopeA")
//
//        val replConfig1 = ReplicatorConfiguration(remoteTargetEndpoint)
//
//        replConfig1.addCollection(collectionA, null)
//
//        val collections = baseTestDb.collections
//        assertEquals(1, collections.size)
//        assertTrue(collections.contains(collectionA))
//    }
//
//
//    //     1: Create collection "colA" in the scope "scopeA" using database instance A.
//    //     2: Create collection "colB" in the scope "scopeA" using database instance B.
//    //     3: Delete collection colB
//    //     4: Create a config object with ReplicatorConfiguration.init(endpoint: endpoint).
//    //     7: Use addCollection() to add colB. This should cause an InvalidArgumentException.
//    @Test
//    fun testAddDeletedCollections3() {
//        assertFailsWith<IllegalArgumentException> {
//            val collectionA = baseTestDb.createCollection("colA", "scopeA")
//            val collectionB = baseTestDb.createCollection("colB", "scopeA")
//
//            baseTestDb.deleteCollection("colB", "scopeA")
//
//            ReplicatorConfiguration(remoteTargetEndpoint)
//                .addCollection(collectionB, null)
//        }
//    }
//}
