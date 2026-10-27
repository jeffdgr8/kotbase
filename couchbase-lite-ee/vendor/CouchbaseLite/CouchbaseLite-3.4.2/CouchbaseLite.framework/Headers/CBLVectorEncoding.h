//
//  CBLVectorEncoding.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <CouchbaseLite/CBLVectorIndexTypes.h>

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY
 
 Vector encoding type to use in vector indexes.
 */
@interface CBLVectorEncoding: NSObject

/** No encoding; 4 bytes per dimension, no data loss. */
+ (instancetype) none;

/**
 Scalar Quantizer encoding.
 
 @param type The type of Scalar Quantizer
*/
+ (instancetype) scalarQuantizerWithType: (CBLScalarQuantizerType)type;

/**
 Product Quantizer encoding.
 
 @param subquantizers  Number of subquantizers. Must be > 1 and a factor of vector dimensions.
 @param bits Number of bits. Must be >= 4 and <= 12.
*/
+ (instancetype) productQuantizerWithSubquantizers: (unsigned int)subquantizers bits: (unsigned int)bits;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
