//
//  CBLProtocolType.h
//  CouchbaseLite
//
//  Copyright (c) 2018 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

/**
 ENTERPRISE EDITION ONLY.
 
 The protocol type of the data transportation.
 */
typedef enum {
    kCBLProtocolTypeMessageStream,  ///< The message oriented transportation.
    kCBLProtocolTypeByteStream      ///< The stream oriented transportation; the
                                    ///  replicator will apply framing to each message.
} CBLProtocolType;
