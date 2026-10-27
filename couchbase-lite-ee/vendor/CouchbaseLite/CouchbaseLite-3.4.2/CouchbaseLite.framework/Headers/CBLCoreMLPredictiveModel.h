//
//  CBLCoreMLPredictiveModel.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>
#import <CoreML/CoreML.h>
#import <CouchbaseLite/CBLPrediction.h>

NS_ASSUME_NONNULL_BEGIN

/** Prediction input transformer block type. */
typedef CBLDictionary* __nonnull (^CBLCoreMLInputTransformerBlock) (CBLDictionary*);

/** Prediction output transformer block type. */
typedef CBLDictionary* __nullable (^CBLCoreMLOutputTransformerBlock) (CBLDictionary* _Nullable);

/**
 ENTERPRISE EDITION ONLY
 
 CBLCoreMLPredictiveModel is a Core ML based implementation of the CBLPredictiveModel
 protocol. Basically the CBLCoreMLPredictiveModel used a Core ML model to return
 prediction results.
 
 CBLCoreMLPredictiveModel automatically converts between Couchbase Lite data and Core ML
 data when calling into the MLModel object to return prediction results. All Core ML
 data types including Int64, Double, String, Dictionary, MultiArray, Image, and Sequence
 are supported.
 
 When the MLObject has a single input and the input type is Image, CBLCoreMLPredictiveModel
 will use Vision framework via the VNCoreMLModel to process the input image and call into
 the MLModel object. The CBLCoreMLPredictiveModel supports all VNObservation types including
 VNClassificationObservation, VNCoreMLFeatureValueObservation, and VNPixelBufferObservation
 as mentioned in https://developer.apple.com/documentation/vision/vncoremlrequest.
 
 However there is a compatibility limitation when the VNCoreMLModel returns
 VNCoreMLFeatureValueObservation or VNPixelBufferObservation results that the MLModel must
 return a single output, otherwise the observation outputs cannot be mapped to the MLModel
 outputs. When the VNCoreMLModel cannot be used to result the prediction result,
 CoreMLPredictiveModel will fall back to use the MLModel instead.
 
 When converting from blob data to VNPixelBuffer for an input image, only ARGB pixel format
 is currently supported. However this limitation is applied only when the VNCoreMLModel cannot
 be used.
 */
@interface CBLCoreMLPredictiveModel : NSObject <CBLPredictiveModel>

/**
 Initializes the CBLCoreMLPredictiveModel with the MLModel object.
 
 @param model The MLModel object.
 */
- (instancetype) initWithMLModel: (MLModel*)model;

/** Prediction input transformer block. */
@property (nonatomic, nullable) CBLCoreMLInputTransformerBlock inputTransformer;

/** Prediction output transformer block. */
@property (nonatomic, nullable) CBLCoreMLOutputTransformerBlock outputTransformer;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
