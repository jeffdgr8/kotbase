//
//  CBLVectorIndexConfiguration.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <CouchbaseLite/CBLIndexConfiguration.h>
#import <CouchbaseLite/CBLVectorEncoding.h>
#import <CouchbaseLite/CBLVectorIndexTypes.h>

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY
 
 Configuration for creating vector indexes.
 */
@interface CBLVectorIndexConfiguration : CBLIndexConfiguration

/**
 The SQL++ expression returning either a vector, which is an array of 32-bit floating-point numbers,
 or a Base64 string representing an array of 32-bit floating-point numbers in little-endian order.
 
 When lazy index is enabled, an expression will return a value for computing a vector lazily by using
 CBLIndexUpdater instead. 
 */
@property (nonatomic, readonly) NSString* expression;

/** 
 The number of vector dimensions. 
 @note The maximum number of vector dimensions supported is 4096.
 */
@property (nonatomic, readonly) unsigned int dimensions;

/** 
 The number of centroids which is the number buckets to partition the vectors in the index. 
 @note The recommended number of centroids is the square root of the number of vectors to be indexed,
       and the maximum number of centroids supported is 64,000.
 */
@property (nonatomic, readonly) unsigned int centroids;

/** 
 Vector encoding type. The default value is 8-bits Scalar Quantizer. 
 */
@property (nonatomic) CBLVectorEncoding* encoding;

/** 
 Distance Metric type. The default value is euclidean distance.
*/
@property (nonatomic) CBLDistanceMetric metric;

/** 
 The minimum number of vectors for training the index. The default value
 is zero, meaning that minTrainingSize will be automatically calculated by
 the index based on the number of centroids specified, encoding types, and
 the encoding parameters. 
 
 The training will occur at or before the APPROX_VECTOR_DISANCE query is
 executed, provided there is enough data at that time, and consequently, if
 training is triggered during a query, the query may take longer to return
 results.
 
 If a query is executed against the index before it is trained, a full
 scan of the vectors will be performed. If there are insufficient vectors
 in the database for training, a warning message will be logged,
 indicating the required number of vectors.
 */
@property (nonatomic) unsigned int minTrainingSize;

/** 
 The maximum number of vectors used for training the index. The default
 value is zero, meaning that the maxTrainingSize will be automatically
 calulated by the index based on the number of centroids specified,
 encoding types, and the encoding parameters.
 */
@property (nonatomic) unsigned int maxTrainingSize;

/**
 The number of centroids that will be scanned during a query.
 The default value is zero, meaning that the numProbes will be
 automatically calulated by the index based on the number of centroids specified.
 */
@property (nonatomic) unsigned int numProbes;

/**
 The boolean flag indicating that index is lazy or not. The default value is false.
 
 If the index is lazy, it will not be automatically updated when the documents in the collection are changed,
 except when the documents are deleted or purged.
 
 When configuring the index to be lazy, the expression set to the config is the expression that returns
 a value used for computing the vector.
  
 To update the lazy index, use a CBLIndexUpdater object obtained from a CBLQueryIndex object,
 which can be retrieved from a CBLCollection object.
 */
@property (nonatomic) bool isLazy;

/**
 Initializes the CBLVectorIndexConfiguration object.
 
 @param expression The SQL++ expression returning a vector which is an array of numbers.
 @param dimensions  The number of dimensions of the vectors to be indexed. The vectors that do not have the same dimensions
                   specified in the config will not be indexed. The dimensions must be >= 2 and <= 4096.
 @param centroids The number of centroids which is the number buckets to partition the vectors in the index. The number of
                  centroids will be based on the expected number of vectors to be indexed; one suggested rule is to use
                  the square root of the number of vectors. The centroids must be >= 1 and <= 64000.
 @return The CBLVectorIndexConfiguration object.
 */
- (instancetype)initWithExpression: (NSString*)expression
                        dimensions: (unsigned int)dimensions
                         centroids: (unsigned int)centroids;

@end

NS_ASSUME_NONNULL_END
