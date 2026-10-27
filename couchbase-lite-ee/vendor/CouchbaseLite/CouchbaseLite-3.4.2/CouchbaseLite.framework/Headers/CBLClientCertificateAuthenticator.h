//
//  CBLClientCertificateAuthenticator.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc All rights reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

#import <CouchbaseLite/CBLAuthenticator.h>

@class CBLTLSIdentity;

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY.
 
 An authenticator that presents a client certificate to the server during the initial SSL/TLS
 handshake. This is currently used for authenticating with CBLURLEndpointListener only.
 */
@interface CBLClientCertificateAuthenticator : CBLAuthenticator

/** The identity object containing a key pair and certificate that represents the client's identity. */
@property (nonatomic, readonly) CBLTLSIdentity* identity;

/** Initializes with an identity object containing a key pair and certificate that represents the client's identity. */
- (instancetype) initWithIdentity: (CBLTLSIdentity*)identity;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END

