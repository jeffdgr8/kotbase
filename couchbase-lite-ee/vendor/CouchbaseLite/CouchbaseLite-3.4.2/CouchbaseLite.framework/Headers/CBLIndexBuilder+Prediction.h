//
//  CBLIndexBuilder+Prediction.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>
#import <CouchbaseLite/CBLIndexBuilder.h>

@class CBLPredictiveIndex;
@class CBLQueryExpression;

NS_ASSUME_NONNULL_BEGIN

@interface CBLIndexBuilder (Prediction)

/**
 ENTERPRISE EDITION ONLY
 
 Create a predictive index with the given predictive model name, input specification to
 the predictive model, and the properties of the prediction result.
 
 The input given specification should be matched to the input specification given to the
 query prediction() function so that the predictive index can be matched and used in query.
 
 The predictive index is different from the normal index in that the predictive index will
 also cache the prediction result along with creating the value index of the specified
 properties. If the properties are not specified, the predictive index will only cache
 the prediction result so that the prediction model will not be called again after indexing.
 If multiple properties are specified, a compound value index will be created from the
 the given properties.

 @param model The predictive model name.
 @param input The input specification that should be matched with the input
              specification given to the query prediction function.
 @param properties The prediction result's properties to be indexed.
 @return The predictive index.
 */
+ (CBLPredictiveIndex*) predictiveIndexWithModel: (NSString*)model
                                           input: (CBLQueryExpression*)input
                                      properties: (nullable NSArray<NSString*>*)properties;

@end

NS_ASSUME_NONNULL_END
