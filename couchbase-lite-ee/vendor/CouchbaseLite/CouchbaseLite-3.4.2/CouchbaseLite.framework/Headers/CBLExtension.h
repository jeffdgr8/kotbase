//
//  CBLExtension.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/** 
 ENTERPRISE EDITION ONLY.
 
 Couchbase Lite Extension. 
 */
@interface CBLExtension : NSObject

/**
 Enables Vector Search extension. Requires CouchbaseLiteVectorSearch XCFramework.
 This function must be called before opening a database that intends to use the vector search extension. 
 @param error On return, error if any.
 @return True on success, false on failure.
 */
+ (BOOL) enableVectorSearch: (NSError**)error;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
