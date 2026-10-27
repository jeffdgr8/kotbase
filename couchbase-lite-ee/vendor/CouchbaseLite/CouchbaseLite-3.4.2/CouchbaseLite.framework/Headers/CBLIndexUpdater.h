//
//  CBLIndexUpdater.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>
#import <CouchbaseLite/CBLArray.h>

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY
 
 CBLIndexUpdater is used for updating the index in lazy mode. Currently, the vector index is the only index type
 that can be updated lazily.
 */
@interface CBLIndexUpdater : NSObject <CBLArray>

/**
 The total number of vectors to compute and set for updating the index.
 */
@property (readonly) NSUInteger count;

/**
 Sets the vector for the value corresponding to the index.
 
 Setting nil value means that there is no vector for the value, and any existing vector will be removed
 when the -finishWithError: is called.
 
 @param vector Array of float numbers.
 @param index The index.
 @param error On return, the error if any.
 @return True on success, false on failure.
 */
- (BOOL) setVector: (nullable NSArray<NSNumber*>*)vector
           atIndex: (NSUInteger)index
             error: (NSError**)error;

/**
 Skip setting the vector for the value corresponding to the index. The vector will be required to compute and
 set again for the value when the CBLQueryIndex's -beginUpdateWithLimit:error: is later called for updating the index.
 
 @param index The index.
 */
- (void) skipVectorAtIndex: (NSUInteger)index;

/**
 Updates the index with the computed vectors and removes any index rows for which nil vector was given.
 If there are any indexes that do not have their vector value set or are skipped, a CBLErrorUnsupported error will be thrown.
 @note Before calling the finish() function, the set vectors are kept in the memory.
 
 @param error On return, the error if any.
 @return True on success, false on failure.
 */
- (BOOL) finishWithError: (NSError**)error;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
