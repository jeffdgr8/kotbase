//
//  CBLQueryFunction+Vector.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <CouchbaseLite/CBLQueryFunction.h>
#import <CouchbaseLite/CBLQueryExpression.h>

NS_ASSUME_NONNULL_BEGIN

#pragma mark - Vector Comparison

@interface CBLQueryFunction (Vector)

/**
 ENTERPRISE EDITION ONLY
 
 Creates a function that returns the euclidean distance between the two input vectors.
 The result is a non-negative floating-point number. The expression1 and expression2 must be
 arrays of numbers, and must be the same length.

 @param expression1 The expression evaluated to an arrays of numbers.
 @param expression2 The expression evaluated to an arrays of numbers.
 @return The euclient distance between two given input vectors.
 */
+ (CBLQueryExpression*) euclideanDistanceBetween: (CBLQueryExpression*)expression1
                                             and: (CBLQueryExpression*)expression2;

/**
 ENTERPRISE EDITION ONLY
 
 Creates a function that returns the squared euclidean distance between the two input vectors.
 The result is a non-negative floating-point number. The expression1 and expression2 must be
 arrays of numbers, and must be the same length.

 @param expression1 The expression evaluated to an arrays of numbers.
 @param expression2 The expression evaluated to an arrays of numbers.
 @return The squared euclient distance between two given input vectors.
 */
+ (CBLQueryExpression*) squaredEuclideanDistanceBetween: (CBLQueryExpression*)expression1
                                                    and: (CBLQueryExpression*)expression2;

/**
 ENTERPRISE EDITION ONLY
 
 Creates a function that returns the cosine distance which one minus the cosine similarity
 between the two input vectors. The result is a floating-point number ranges from −1.0 to 1.0.
 The expression1 and expression2 must be arrays of numbers, and must be the same length.

 @param expression1 The expression evaluated to an arrays of numbers.
 @param expression2 The expression evaluated to an arrays of numbers.
 @return The cosine distance between two given input vectors.
 */
+ (CBLQueryExpression*) cosineDistanceBetween: (CBLQueryExpression*)expression1
                                          and: (CBLQueryExpression*)expression2;

@end

NS_ASSUME_NONNULL_END
