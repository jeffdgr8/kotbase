//
//  CBLDatabaseConfiguration+Encryption.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>
#import <CouchbaseLite/CBLDatabaseConfiguration.h>

@class CBLEncryptionKey;

NS_ASSUME_NONNULL_BEGIN

@interface CBLDatabaseConfiguration (Encryption)

/**
 ENTERPRISE EDITION ONLY.
 
 A key to encrypt the database with. If the database does not exist and is being created, it
 will use this key, and the same key must be given every time it's opened.
 
 * The primary form of key is an NSData object 32 bytes in length: this is interpreted as a raw
 AES-256 key. To create a key, generate random data using a secure cryptographic randomizer
 like SecRandomCopyBytes or CCRandomGenerateBytes.
 * Alternatively, the value may be an NSString containing a password. This will be run through
 64,000 rounds of the PBKDF algorithm to securely convert it into an AES-256 key.
 * A default nil value, of course, means the database is unencrypted.
 */
@property (nonatomic, nullable) CBLEncryptionKey* encryptionKey;

@end

NS_ASSUME_NONNULL_END
