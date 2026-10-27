//
//  CBLMultipeerCertificateAuthenticator.h
//  CouchbaseLite
//
//  Copyright (c) 2025 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>

@class CBLPeerID;

NS_ASSUME_NONNULL_BEGIN

/** An authenticator used by the multipeer replicator to authenticate peers. */
@protocol CBLMultipeerAuthenticator <NSObject> @end

/**
 A block used to authenticate a peer’s certificate.
 
 @param peerID The identifier of the peer.
 @param certs An array of the certificate chain received from the peer. The array contains `SecCertificateRef` objects.
 @return YES to accept the peer, NO to reject.
 */
typedef BOOL (^CBLMultipeerCertificateAuthBlock) (CBLPeerID* peerID, NSArray* certs);

/** A certificate authenticator used to verify a peer’s identity during multipeer replication. */
@interface CBLMultipeerCertificateAuthenticator : NSObject <CBLMultipeerAuthenticator>

/**
 Initializes the authenticator with a list of trusted root certificates.
 
 The peer’s certificate will be accepted only if it is signed by one of the specified root certificates.
 
 @param rootCerts An array of trusted root certificates (`SecCertificateRef`).
 @return A CBLMultipeerCertificateAuthenticator instance.
 */
- (instancetype) initWithRootCerts: (NSArray*)rootCerts;

/**
 Initializes the authenticator with a custom certificate validation block.
 
 @param auth A block that performs custom validation on the peer’s certificate chain.
 @return A CBLMultipeerCertificateAuthenticator instance.
 */
- (instancetype) initWithBlock: (CBLMultipeerCertificateAuthBlock)auth ;

/** Not available. */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
