//
//  CBLPeerID.h
//  CouchbaseLite
//
//  Copyright (c) 2025 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/** A peer identifier based on 32-byte SHA-256 binary data. */
@interface CBLPeerID : NSObject <NSCopying>

/// The raw 32-byte SHA-256 data
@property (nonatomic, readonly) NSData *bytes;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
