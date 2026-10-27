//
//  CBLDatabase+Prediction.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>
#import <CouchbaseLite/CBLDatabase.h>

@class CBLPrediction;

NS_ASSUME_NONNULL_BEGIN

@interface CBLDatabase (Prediction)

/**
 ENTERPRISE EDITION ONLY
 
 The predictive model manager for registering and unregistering predictive models.
 */
+ (CBLPrediction*) prediction;

@end

NS_ASSUME_NONNULL_END
