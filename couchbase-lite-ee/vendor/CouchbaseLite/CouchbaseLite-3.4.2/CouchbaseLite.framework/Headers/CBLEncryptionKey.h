//
//  CBLEncryptionKey.h
//  CouchbaseLite
//
//  Copyright (c) 2018 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY.
 
 An encryption key for a database. This is a symmetric key that be kept secret.
 It should be stored either in the Keychain, or in the user's memory (hopefully not a sticky note.)
 */
@interface CBLEncryptionKey : NSObject

/**
 Initializes the encryption key with a raw AES-256 key 32 bytes in length.
 To create a key, generate random data using a secure cryptographic randomizer like
 SecRandomCopyBytes or CCRandomGenerateBytes.
 
 @param key The raw AES-256 key data.
 @return The CBLEncryptionKey object.
 */
- (instancetype) initWithKey: (NSData*)key;


/**
 Initializes the encryption key with the given password string. The password string will be
 internally converted to a raw AES-256 key using 64,000 rounds of PBKDF2 hashing.

 @param password The password string.
 @return The CBLEncryptionKey object.
 */
- (instancetype) initWithPassword: (NSString*)password;


/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
