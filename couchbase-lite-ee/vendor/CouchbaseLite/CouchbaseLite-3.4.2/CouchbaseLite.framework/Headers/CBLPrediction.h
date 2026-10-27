//
//  CBLPrediction.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>

@class CBLDictionary;

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY
 
 PredictiveModel protocol that allows to integrate machine learning model into
 CouchbaseLite Query via invoking the Function.prediction() function.
 */
@protocol CBLPredictiveModel <NSObject>

/**
 The prediction callback called when invoking the Function.prediction() function
 inside a query or an index. The input dictionary object's keys and values will be
 corresponding to the 'input' dictionary parameter of theFunction.prediction() function.

 If the prediction callback cannot return a result, the prediction callback
 should return null value, which will be evaluated as MISSING.
 
 @param input The input dictionary.
 @return The output dictionary.
 */
- (nullable CBLDictionary*) predict: (CBLDictionary*)input;

@end

/**
 The prediction model manager for registering and unregistering predictive models.
 */
@interface CBLPrediction : NSObject

/**
 Register a predictive model by the given name.

 @param model The predictive model.
 @param name The name of the predictive model.
 */
- (void) registerModel: (id<CBLPredictiveModel>)model withName: (NSString*)name;

/**
 Unregister the predictive model of the given name.

 @param name The name of the predictive model.
 */
- (void) unregisterModelWithName: (NSString*)name;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
