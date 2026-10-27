//
//  CBLMessage.h
//  CouchbaseLite
//
//  Copyright (c) 2018 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY.
 
 A message sent between message endpoint connections.
 */
@interface CBLMessage : NSObject

/**
 Creates a message object from data.

 @param data The data.
 @return The message.
 */
+ (CBLMessage*) fromData: (NSData*)data;

/**
 Gets the message as data.

 @return The data.
 */
- (NSData*) toData;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
