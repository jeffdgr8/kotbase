//
//  CBLMessagingError.h
//  CouchbaseLite
//
//  Copyright (c) 2018 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>

/**
 ENTERPRISE EDITION ONLY.
 
 The messaging error.
 */
@interface CBLMessagingError : NSObject

/**
 The error object.
 */
@property (nonatomic, readonly) NSError* error;

/**
 The flag identifying whether the error is recoverable or not.
 */
@property (nonatomic, readonly) BOOL isRecoverable;

/**
 Initializes a CBLMessagingError object with the given error and the flag
 identifying if the error is recoverable or not. The replicator uses isRecoverable
 flag to determine whether the replication should be retried as the error is
 recoverable or stopped as the error is non-recoverable.

 @param error The error object.
 @param isRecoverable The flag identifying if the error is recoverable or not.
 @return The CBLMessagingError object.
 */
- (instancetype) initWithError: (NSError*)error isRecoverable: (BOOL)isRecoverable;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end
