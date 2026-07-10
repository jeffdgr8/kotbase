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

    /****************** Scopes and Collections Section 8.13 ****************/

    // 8.13.2 Create a config object with
    // ReplicatorConfiguration(Collection<CollectionConfiguration>, Endpoint).
    //
    // Set CollectionConfiguration.conflictResolver with a conflict resolver.
    //
    // Check CollectionConfiguration.conflictResolver. The returned conflict resolver
    // should be the same as the one set.
    @Test
    fun testCreateWithCollectionAndConflictResolver() {
        val resolver = localResolver
        val collectConfigs = ReplicatorConfiguration(
            CollectionConfiguration.fromCollections(setOf(testDatabase.defaultCollection)),
            mockURLEndpoint
        ).collections

        collectConfigs.forEach { it.conflictResolver = resolver }

        collectConfigs.forEach {
            assertNotNull(it)
            assertEquals(resolver, it.conflictResolver)
        }
    }

    // 8.13.4 Create a config object with
    // ReplicatorConfiguration(Collection<CollectionConfiguration>, Endpoint).
    //
    // Set values to CollectionConfiguration.pushFilter, pullFilter, channels, and
    // documentIDs.
    //
    // The filters in the config should be the same as the ones set.
    @Test
    fun testCreateWithCollectionAndReplicationFilters() {
        val pushFilter1: ReplicationFilter = { _, _ -> true }
        val pullFilter1: ReplicationFilter = { _, _ -> true }
        val collectConfigs = CollectionConfiguration.fromCollections(setOf(testDatabase.defaultCollection))
        val replConfig1 = ReplicatorConfiguration(collectConfigs, mockURLEndpoint)
        replConfig1.collections.forEach { collectConfig ->
            collectConfig
                .setPushFilter(pushFilter1)
                .setPullFilter(pullFilter1)
                .setChannels(listOf("CNBC", "ABC"))
                .setDocumentIDs(listOf("doc1", "doc2"))
        }

        replConfig1.collections.forEach { collectConfig ->
            assertEquals(pushFilter1, collectConfig.pushFilter)
            assertEquals(pullFilter1, collectConfig.pullFilter)
            assertContentEquals(arrayOf("CNBC", "ABC"), collectConfig.channels?.toTypedArray())
            assertContentEquals(arrayOf("doc1", "doc2"), collectConfig.documentIDs?.toTypedArray())
        }
    }

    // 8.13.12b Create collection "colA" in the scope "scopeA" using database instance A.
    //
    // Create collection "colB" in the scope "scopeA" using database instance B.
    //
    // Create a config object with
    // ReplicatorConfiguration(collections: Set<CollectionConfiguration>, endpoint: endpoint).
    // An invalid argument exception should be thrown as the collections are from
    // different database instances.
    @Test
    fun testUseCollectionsFromDifferentDatabaseInstancesB() {
        val collectionA = testDatabase.createCollection("colA", "scopeA")
        val collectionB = targetDatabase.createCollection("colB", "scopeA")

        val collectConfig = CollectionConfiguration.fromCollections(setOf(collectionA, collectionB))

        assertFailsWith<IllegalArgumentException> {
            ReplicatorConfiguration(collectConfig, mockURLEndpoint)
        }
    }

    // 8.13.13a Create collection "colA" in the scope "scopeA" using database instance A.
    //
    // Create collection "colB" in the scope "scopeA" using database instance B.
    //
    // Delete collection colB.
    //
    // Create a config object with
    // ReplicatorConfiguration(Collection<CollectionConfiguration>, Endpoint).
    //
    // Check Collection Config size and whether config contains config of collection A.
    @Test
    fun testUseDeletedCollectionsB() {
        val collectionA = testDatabase.createCollection("colA", "scopeA")
        targetDatabase.createCollection("colB", "scopeA")

        testDatabase.deleteCollection("colB", "scopeA")

        val collectConfigA = CollectionConfiguration(collectionA)
        val replConfig1 = ReplicatorConfiguration(setOf(collectConfigA), mockURLEndpoint)

        val collectionConfigs = replConfig1.collections
        assertEquals(1, collectionConfigs.size)
        assertTrue(collectionConfigs.contains(collectConfigA))
    }

    // 8.13.13c Create collection "colB" in the scope "scopeA" using database instance B.
    //
    // Delete collection colB.
    //
    // Create a config object with
    // ReplicatorConfiguration(Collection<CollectionConfiguration>, Endpoint).
    //
    // As of CBL 4.1, creating a replicator configuration with a deleted collection
    // no longer throws: collection validity is verified when the replicator is used.
    // (The upstream Java test now only verifies that a null collection is rejected,
    // which the Kotlin API prevents at compile time.)
    @Test
    fun testUseDeletedCollectionsC() {
        val collectionB = targetDatabase.createCollection("colB", "scopeA")

        targetDatabase.deleteCollection("colB", "scopeA")

        val replConfig = ReplicatorConfiguration(
            CollectionConfiguration.fromCollections(setOf(collectionB)),
            mockURLEndpoint
        )
        assertEquals(1, replConfig.collections.size)
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
