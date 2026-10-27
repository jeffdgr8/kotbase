//
//  CBLDatabaseEndpoint.h
//  CouchbaseLite
//
//  Copyright (c) 2018 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>
#import <CouchbaseLite/CBLEndpoint.h>

@class CBLDatabase;

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY.
 
 Database based replication target endpoint. Available in the Enterprise Edition only.
 */
@interface CBLDatabaseEndpoint : NSObject <CBLEndpoint>

/** The database object. */
@property (readonly, nonatomic) CBLDatabase* database;

/**
 Initializes with the database object.

 @param database The database object.
 @return The CBLDatabaseEndpoint object.
 */
- (instancetype) initWithDatabase: (CBLDatabase*)database;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
