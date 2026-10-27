//
//  CBLReplicatorConfiguration+ServerCert.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>
#import <CouchbaseLite/CBLReplicatorConfiguration.h>

NS_ASSUME_NONNULL_BEGIN

@interface CBLReplicatorConfiguration (ServerCert)

/**
 ENTERPRISE EDITION ONLY.
 
 Specify the replicator to accept any and only self-signed certs. Any non-self-signed certs will be rejected
 to avoid accidentally using this mode with the non-self-signed certs in production.
 
 Default value is ``kCBLDefaultReplicatorSelfSignedCertificateOnly``
 */
@property (nonatomic) BOOL acceptOnlySelfSignedServerCertificate;

@end

NS_ASSUME_NONNULL_END
