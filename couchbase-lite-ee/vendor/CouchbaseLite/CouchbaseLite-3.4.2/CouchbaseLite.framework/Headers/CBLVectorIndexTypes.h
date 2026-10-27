//
//  CBLVectorIndexTypes.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY
 
 Scalar Quantizer encoding type.
*/
typedef NS_ENUM(uint32_t, CBLScalarQuantizerType) {
    kCBLSQ4 = 4,    /* 4 bits per dimension */
    kCBLSQ6 = 6,    /* 6 bits per dimension */
    kCBLSQ8 = 8     /* 8 bits per dimension */
};

/**
 ENTERPRISE EDITION ONLY
 
 Distance metric type
*/
typedef NS_ENUM(uint32_t, CBLDistanceMetric) {
    kCBLDistanceMetricEuclideanSquared = 1,     /* Squared Euclidean distance (AKA Squared L2) */
    kCBLDistanceMetricCosine,                   /* Cosine distance (1.0 - Cosine Similarity) */
    kCBLDistanceMetricEuclidean,                /* Euclidean distance (AKA L2) */
    kCBLDistanceMetricDot                       /* Dot-product distance (Negative of dot-product) */
};

NS_ASSUME_NONNULL_END
