//
//  CBLTLSIdentity.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>

@class CBLDatabase;

NS_ASSUME_NONNULL_BEGIN

// Certificate Attributes:
extern NSString* const kCBLCertAttrCommonName;              // e.g. "Jane Doe", (or "jane.example.com")
extern NSString* const kCBLCertAttrPseudonym;               // e.g. "plainjane837"
extern NSString* const kCBLCertAttrGivenName;               // e.g. "Jane"
extern NSString* const kCBLCertAttrSurname;                 // e.g. "Doe"
extern NSString* const kCBLCertAttrOrganization;            // e.g. "Example Corp."
extern NSString* const kCBLCertAttrOrganizationUnit;        // e.g. "Marketing"
extern NSString* const kCBLCertAttrPostalAddress;           // e.g. "123 Example Blvd #2A"
extern NSString* const kCBLCertAttrLocality;                // e.g. "Boston"
extern NSString* const kCBLCertAttrPostalCode;              // e.g. "02134"
extern NSString* const kCBLCertAttrStateOrProvince;         // e.g. "Massachusetts" (or "Quebec", ...)
extern NSString* const kCBLCertAttrCountry;                 // e.g. "us" (2-letter ISO country code)

// Certificate Subject Alternative Name attributes:
extern NSString* const kCBLCertAttrEmailAddress;            // e.g. "jane@example.com"
extern NSString* const kCBLCertAttrHostname;                // e.g. "www.example.com"
extern NSString* const kCBLCertAttrURL;                     // e.g. "https://example.com/jane"
extern NSString* const kCBLCertAttrIPAddress;               // An IP Address in binary format e.g. "\x0A\x00\x01\x01"
extern NSString* const kCBLCertAttrRegisteredID;            // A domain specific identifier.


/** Extended Key Usage for which the certified public key may be used. */
typedef NS_OPTIONS(NSUInteger, CBLKeyUsages) {
    kCBLKeyUsagesServerAuth       = 0x40,   ///< For Server Authentication
    kCBLKeyUsagesClientAuth       = 0x80    ///< For Client Authentication
};

/**
 ENTERPRISE EDITION ONLY.
 
 CBLTLSIdentity provides TLS Identity information including a key pair and X.509 certificate chain used
 for configuring TLS communication to the listener.
*/
@interface CBLTLSIdentity: NSObject

/** The certificate chain as an array of SecCertificateRef object.  */
@property (nonatomic, readonly) NSArray* certs;

/** The identity expiration date which is the expiration date of the first certificate in the chain. */
@property (nonatomic, readonly) NSDate* expiration;

/** Not available. */
- (instancetype) init NS_UNAVAILABLE;

/** Get an identity from the Keychain with the given label. */
+ (nullable CBLTLSIdentity*) identityWithLabel: (NSString*)label
                                         error: (NSError**)error NS_SWIFT_NOTHROW;

/** Get an identity with a SecIdentity object. Any intermediate or root certificates required to identify the certificate
    but not present in the system wide set of trusted anchor certificates need to be specified in the optional certs
    parameter. In additon, the specified SecIdenetity object is required to be present in the KeyChain, otherwise
    an exception will be thrown.
 */
+ (nullable CBLTLSIdentity*) identityWithIdentity: (SecIdentityRef)identity
                                            certs: (nullable NSArray*)certs
                                            error: (NSError**)error NS_SWIFT_NOTHROW;

/**
 Generate a TLS identity, either self-signed or signed by an issuer identity,  and stores it in the Keychain with the given label.
 
 The attributes must include a common name (CN); otherwise an error will be returned.
 
 If no the expiration date is specified, the default validity of one year will be applied.
 
 The certificate will be self-signed.
 */
+ (nullable CBLTLSIdentity*) createIdentityForKeyUsages: (CBLKeyUsages)keyUsages
                                             attributes: (NSDictionary<NSString*, NSString*>*)attributes
                                             expiration: (nullable NSDate*)expiration
                                                  label: (NSString*)label
                                                  error: (NSError**)error;

/**
 Generate a TLS identity, signed by the provided key,  and stores it in the Keychain with the given label.
 
 The attributes must include a common name (CN); otherwise an error will be returned.
 
 If no the expiration date is specified, the default validity of one year will be applied.
 
 This should only be used in secured environments where the CA key and certificate are securely stored and managed.
 */
+ (nullable CBLTLSIdentity*) createSignedIdentityInsecureForKeyUsages: (CBLKeyUsages)keyUsages
                                                           attributes: (NSDictionary<NSString*, NSString*>*)attributes
                                                           expiration: (nullable NSDate*)expiration
                                                                caKey: (NSData*)key
                                                        caCertificate: (NSData*)certificate
                                                                label: (NSString*)label
                                                                error: (NSError**)error;

/**
 Creates a self-signed identity and persist the identity in the Keychain with the given label. Note that the Common Name
 (kCBLCertAttrCommonName) attribute is required. If the Common Name attribute is not included, an error will be returned.
 */
+ (nullable CBLTLSIdentity*) createIdentityForServer: (BOOL)server
                                          attributes: (NSDictionary<NSString*, NSString*>*)attributes
                                          expiration: (nullable NSDate*)expiration
                                               label: (NSString*)label
                                               error: (NSError**)error
__deprecated_msg("Use [CBLTLSIdentity createIdentityForKeyUsages:attributes:expiration:label:error:] instead.");

/**
 Imports and creates a identity from the given PKCS12 Data. The imported identity will be stored in the Keychain with the given label.
*/
+ (nullable CBLTLSIdentity*) importIdentityWithData: (NSData*)data
                                           password: (nullable NSString*)password
                                              label: (NSString*)label
                                              error: (NSError**)error;

/**
 Delete the identity in the Keychain with the given label.
 */
+ (BOOL) deleteIdentityWithLabel: (NSString*)label
                           error: (NSError**)error;

@end

NS_ASSUME_NONNULL_END
