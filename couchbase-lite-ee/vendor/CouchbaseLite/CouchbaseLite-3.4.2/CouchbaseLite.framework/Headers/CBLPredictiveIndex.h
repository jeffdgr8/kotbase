//
//  CBLPredictiveIndex.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>
#import <CouchbaseLite/CBLIndex.h>

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY
 
 The predictive index used for querying with prediction function. The predictive index
 is different from the normal index in that the predictive index will cache the prediction
 result along with creating the value index of the prediction output properties.
 
 The PredictiveIndex can be created by using IndexBuilder's predictiveIndex() function.
 If the prediction output properties are not specified, the predictive index will only cache
 the predictive result so that the predictive model will not be called again after indexing.
 */
@interface CBLPredictiveIndex : CBLIndex

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
