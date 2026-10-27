//
//  CBLQueryFunction+Prediction.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <CouchbaseLite/CBLQueryFunction.h>
#import <CouchbaseLite/CBLQueryExpression.h>

@class CBLQueryPredictionFunction;

NS_ASSUME_NONNULL_BEGIN

#pragma mark - Prediction

@interface CBLQueryFunction (Prediction)

/**
 ENTERPRISE EDITION ONLY
 
 Creates prediction function with the given model name and input. When running a query with
 the prediction function, the corresponding predictive model registered to CBLDatabase class
 will be called with the given input to predict the result.
 
 The prediction result returned by the predictive model will be in a form dictionary object.
 To create an expression that refers to a property in the prediction result, the -property:
 method of the created CBLQueryPredictionFunction object can be used.

 @param model The predictive model name registered to the CouchbaseLite Database.
 @param input The expression evaluated to a dictionary.
 @return A CBLQueryPredictionFunction object.
 */
+ (CBLQueryPredictionFunction*) predictionUsingModel: (NSString*)model
                                               input: (CBLQueryExpression*)input;

@end

/**
 ENTERPRISE EDITION ONLY
 
 CBLQueryPredictionFunction that allows to create an expression that
 refers to one of the properties of the prediction result dictionary.
 */
@interface CBLQueryPredictionFunction : CBLQueryExpression


/**
 Creates a property expression that refers to a property of the prediction result dictionary.

 @param keyPath The key path to the property.
 @return The property expression referring to a property of the prediction dictionary result.
 */
- (CBLQueryExpression*) property: (NSString*)keyPath;

@end

NS_ASSUME_NONNULL_END
