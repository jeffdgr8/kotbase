//
//  CBLMultipeerCollectionConfiguration.h
//  CouchbaseLite
//
//  Copyright (c) 2025 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>
#import <CouchbaseLite/CBLDocumentFlags.h>

@class CBLCollection;
@class CBLConflict;
@class CBLDocument;
@class CBLPeerID;

@protocol CBLConflictResolver;

NS_ASSUME_NONNULL_BEGIN

/** Multipeer Replication Filter Function. */
typedef BOOL (^CBLMultipeerReplicationFilter) (CBLPeerID* peerID,
                                               CBLDocument* document,
                                               CBLDocumentFlags flags);

/** Multipeer Conflict Resolver Protocol. */
@protocol CBLMultipeerConflictResolver <NSObject>

/**
 Resolves a conflict for a document received from a peer.

 @param conflict The conflict object.
 @param peerID The peer that the conflicting document came from.
 @return The resolved document, or nil to delete the document.
 */
- (nullable CBLDocument*) resolveConflict: (CBLConflict*)conflict forPeer: (CBLPeerID*)peerID;

@end

/** Configuration for specifying a collection to replicate, including optional filters and a custom conflict resolver. */
@interface CBLMultipeerCollectionConfiguration : NSObject

/** The collection. */
@property (nonatomic, readonly) CBLCollection* collection;

/** Optional list of document IDs to replicate. Only documents with the specified IDs will be replicated. */
@property (nonatomic, nullable) NSArray<NSString*>* documentIDs;

/** Optional push filter function */
@property (nonatomic, nullable) CBLMultipeerReplicationFilter pushFilter;

/** Optional pull filter function */
@property (nonatomic, nullable) CBLMultipeerReplicationFilter pullFilter;

/** Optional Custom conflict resolver. If not specified, the default conflict resolver will be used. */
@property (nonatomic, nullable) id<CBLMultipeerConflictResolver> conflictResolver;

/**
 Initializes the configuration with the specified collection.
 
 @param collection The collection.
 @return A CBLMultipeerCollectionConfiguration instance.
 */
- (instancetype) initWithCollection: (CBLCollection*)collection;

/** Not available. */
- (instancetype) init NS_UNAVAILABLE;

/**
 Creates an array of `CBLMultipeerCollectionConfiguration` objects from the given collections.
 
 Each collection is wrapped in a `CBLMultipeerCollectionConfiguration`using default settings
 (no filters and no custom conflict resolvers).

 This is a convenience method for configuring multiple collections with default configurations.
 If custom configurations are needed, construct `CBLMultipeerCollectionConfiguration` objects
 directly instead.
       
 @param collections The collections to replicate.
 @return An array of CBLMultipeerCollectionConfiguration objects for the given collections.
 */
+ (NSArray<CBLMultipeerCollectionConfiguration*>*) fromCollections: (NSArray<CBLCollection*>*)collections;

@end

NS_ASSUME_NONNULL_END
