_How to set up a listener to accept a replicator connection and sync using peer-to-peer_

!!! warning "Android enablers"

    **Allow Unencrypted Network Traffic**

    To use cleartext, un-encrypted, network traffic (`http://` and-or `ws://`), include
    `android:usesCleartextTraffic="true"` in the `application` element of the manifest as shown on
    [developer.android.com](https://developer.android.com/training/articles/security-config#CleartextTrafficPermitted).  
    **This not recommended in production.**

!!! warning "iOS Restrictions"

    **iOS 14 Applications**

    When your application attempts to access the user’s local network, iOS will prompt them to allow (or deny) access.
    You can customize the message presented to the user by editing the description for the
    `NSLocalNetworkUsageDescription` key in the `Info.plist`.

!!! warning "Use Background Threads"

    As with any network or file I/O activity, Couchbase Lite activities should not be performed on the UI thread.
    **Always** use a **background** thread.

!!! note "Code Snippets"

    All code examples are indicative only. They demonstrate the basic concepts and approaches to using a feature. Use
    them as inspiration and adapt these examples to best practice when developing applications for your platform.

## Introduction

This content provides code and configuration examples covering the implementation of [Peer-to-Peer Sync](
https://docs.couchbase.com/couchbase-lite/current/android/refer-glossary.html#peer-to-peer-sync) over WebSockets.
Specifically, it covers the implementation of a [Passive Peer](
https://docs.couchbase.com/couchbase-lite/current/android/refer-glossary.html#passive-peer).

Couchbase’s Passive Peer (also referred to as the server, or listener) will accept a connection from an [Active Peer](
https://docs.couchbase.com/couchbase-lite/current/android/refer-glossary.html#active-peer) (also referred to as the
client or replicator) and replicate database changes to synchronize both databases.

Subsequent sections provide additional details and examples for the main configuration options.

!!! note "Secure Storage"

    The use of TLS, its associated keys and certificates requires using secure storage to minimize the chances of a
    security breach. The implementation of this storage differs from platform to platform — see [Using Secure Storage](
    peer-to-peer-sync.md#using-secure-storage).

## Configuration Summary

You should configure and initialize a listener for each Couchbase Lite database instance you want to sync. There is no
limit on the number of listeners you may configure — [Example 1](#example-1) shows a simple initialization and
configuration process.

!!! example "<span id='example-1'>Example 1. Listener configuration and initialization</span>"

    ```kotlin
    val listener = URLEndpointListener(
        URLEndpointListenerConfigurationFactory.create(
            database = database,
            port = 55990,
            networkInterface = "wlan0",
    
            enableDeltaSync = false,
    
            // Configure server security
            disableTls = false,
    
            // Use an Anonymous Self-Signed Cert
            identity = null,
    
            // Configure Client Security using an Authenticator
            // For example, Basic Authentication
            authenticator = ListenerPasswordAuthenticator { usr, pwd ->
                (usr === validUser) && (pwd.concatToString() == validPass)
            }
        )
    )
    
    // Start the listener
    listener.start()
    ```

1. Identify the local database to be used — see [Initialize the Listener Configuration
   ](#initialize-the-listener-configuration)
2. Optionally, choose a port to use. By default the system will automatically assign a port — to override this, see [Set
   Port and Network Interface](#set-port-and-network-interface)
3. Optionally, choose a network interface to use. By default the system will listen on all network interfaces — to
   override this see [Set Port and Network Interface](#set-port-and-network-interface)
4. Optionally, choose to sync only changes. The default is not to enable delta-sync — see [Delta Sync](#delta-sync)
5. Set server security. TLS is always enabled instantly, so you can usually omit this line. But you _can_, optionally,
   disable TLS (**not** advisable in production) — see [TLS Security](#tls-security)
6. Set the credentials this server will present to the client for authentication. Here we show the default TLS
   authentication, which is an anonymous self-signed certificate. The server must always authenticate itself to the
   client.
7. Set client security — define the credentials the server expects the client to present for authentication. Here we
   show how basic authentication is configured to authenticate the client-supplied credentials from the http
   authentication header against valid credentials — see [Authenticating the Client](#authenticating-the-client) for
   more options.  
   Note that client authentication is optional.
8. Initialize the listener using the configuration settings.
9. [Start Listener](#start-listener)

## Device Discovery

**This phase is optional:** If the listener is initialized on a well-known URL endpoint (for example, a static IP
address or well-known DNS address) then you can configure Active Peers to connect to those.

Before initiating the listener, you may execute a peer discovery phase. For the Passive Peer, this involves advertising
the service using, for example, [Network Service Discovery on Android](
https://developer.android.com/training/connect-devices-wirelessly/nsd) or [Bonjour on iOS](
https://developer.apple.com/bonjour/) and waiting for an invite from the Active Peer. The connection is established once
the Passive Peer has authenticated and accepted an Active Peer’s invitation.

## Initialize the Listener Configuration

Initialize the listener configuration with the local database — see [Example 2](#example-2). All other configuration
values take their default setting.

Each listener instance serves one Couchbase Lite database. Couchbase sets no hard limit on the number of listeners you
can initialize.

!!! example "<span id='example-2'>Example 2. Specify Local Database</span>"

    ```kotlin
    database = database,
    ```

Set the local database using the [`URLEndpointListenerConfiguration`](
/api/couchbase-lite-ee/kotbase/-u-r-l-endpoint-listener-configuration/)'s constructor
[`URLEndpointListenerConfiguration(Database)`](
/api/couchbase-lite-ee/kotbase/-u-r-l-endpoint-listener-configuration/-u-r-l-endpoint-listener-configuration.html).  
The database must be opened before the listener is started.  

## Set Port and Network Interface

### Port number

The Listener will automatically select an available port if you do not specify one — see [Example 3](#example-3) for how
to specify a port.

!!! example "<span id='example-3'>Example 3. Specify a port</span>"

    ```kotlin
    port = 55990,
    ```

To use a canonical port — one known to other applications — specify it explicitly using the [`port`](
/api/couchbase-lite-ee/kotbase/-u-r-l-endpoint-listener-configuration/port.html) property shown here.  
Ensure that firewall rules do not block any port you do specify.

### Network Interface

The listener will listen on all network interfaces by default.

!!! example "Example 4. Specify a Network Interface to Use"

    ```kotlin
    networkInterface = "wlan0",
    ```

To specify an interface — one known to other applications — identify it explicitly, using the [`networkInterface`](
/api/couchbase-lite-ee/kotbase/-u-r-l-endpoint-listener-configuration/network-interface.html) property shown here. This
must be either an IP address or network interface name such as `en0`.

## Delta Sync

Delta Sync allows clients to sync only those parts of a document that have changed. This can result in significant
bandwidth consumption savings and throughput improvements. Both are valuable benefits, especially when network bandwidth
is constrained.

!!! example "Example 5. Enable delta sync"

```kotlin
enableDeltaSync = false,
```

Delta sync replication is not enabled by default. Use [`URLEndpointListenerConfiguration`](
/api/couchbase-lite-ee/kotbase/-u-r-l-endpoint-listener-configuration/)'s [`isDeltaSyncEnabled`](
/api/couchbase-lite-ee/kotbase/-u-r-l-endpoint-listener-configuration/is-delta-sync-enabled.html) property to activate
or deactivate it.

## TLS Security

### Enable or Disable TLS

Define whether the connection is to use TLS or clear text.

TLS-based encryption is enabled by default, and this setting ought to be used in any production environment. However, it
_can_ be disabled. For example, for development or test environments.

When TLS is enabled, Couchbase Lite provides several options on how the listener may be configured with an appropriate
TLS Identity — see [Configure TLS Identity for Listener](#configure-tls-identity-for-listener).

!!! note

    On the Android platform, to use cleartext, un-encrypted, network traffic (`http://` and-or `ws://`), include
    `android:usesCleartextTraffic="true"` in the `application` element of the manifest as shown on
    [developer.android.com](https://developer.android.com/training/articles/security-config#CleartextTrafficPermitted).  
    **This not recommended in production.**

You can use [`URLEndpointListenerConfiguration`](
/api/couchbase-lite-ee/kotbase/-u-r-l-endpoint-listener-configuration/)'s [`isTlsDisabled`](
/api/couchbase-lite-ee/kotbase/-u-r-l-endpoint-listener-configuration/is-tls-disabled.html) method to disable TLS
communication if necessary.

The `isTlsDisabled` setting must be `false` when _Client Cert Authentication_ is required.

Basic Authentication can be used with, or without, TLS.

`isTlsDisabled` works in conjunction with `TLSIdentity`, to enable developers to define the key and certificate to be
used.

* If `isTlsDisabled` is `true` — TLS communication is disabled and TLS identity is ignored.  
  Active peers will use the `ws://` URL scheme used to connect to the listener.
* If `isTlsDisabled` is `false` or not specified — TLS communication is enabled.  
  Active peers will use the wss:// URL scheme to connect to the listener.

### Configure TLS Identity for Listener

Define the credentials the server will present to the client for authentication. Note that the server must always
authenticate itself with the client — see [Authenticating the Listener on Active Peer](
active-peer.md#authenticating-the-listener) for how the client deals with this.

Use [`URLEndpointListenerConfiguration`](/api/couchbase-lite-ee/kotbase/-u-r-l-endpoint-listener-configuration/)'s
[`tlsIdentity`](/api/couchbase-lite-ee/kotbase/-u-r-l-endpoint-listener-configuration/tls-identity.html) property to
configure the TLS Identity used in TLS communication.

If [`TLSIdentity`](/api/couchbase-lite-ee/kotbase/-t-l-s-identity/) is not set, then the listener uses an auto-generated
anonymous self-signed identity (unless `isTlsDisabled = true`). Whilst the client cannot use this to authenticate the
server, it will use it to encrypt communication, giving a more secure option than non-TLS communication.

The auto-generated anonymous self-signed identity is saved in secure storage for future use to obviate the need to
re-generate it.

!!! note

    Typically, you will configure the listener’s TLS Identity once during the initial launch and re-use it (from secure
    storage on any subsequent starts.

Here are some example code snippets showing:

* Importing a TLS identity — see [Example 6](#example-6)
* Setting TLS identity to expect self-signed certificate — see [Example 7](#example-7)
* Setting TLS identity to expect anonymous certificate — see [Example 8](#example-8)

!!! example "<span id='example-6'>Example 6. Import Listener’s TLS identity</span>"

    TLS identity certificate import APIs are platform-specific.

    === "Android"

        ```kotlin title="in androidMain"
        config.isTlsDisabled = false
        
        KeyStoreUtils.importEntry(
            "PKCS12",
            context.assets.open("cert.p12"),
            "store-password".toCharArray(),
            "store-alias",
            "key-password".toCharArray(),
            "new-alias"
        )
        
        config.tlsIdentity = TLSIdentity.getIdentity("new-alias")
        ```

    === "iOS/macOS"

        ```kotlin title="in appleMain"
        config.isTlsDisabled = false
        
        val path = NSBundle.mainBundle.pathForResource("cert", ofType = "p12") ?: return
        
        val certData = NSData.dataWithContentsOfFile(path) ?: return
        
        val tlsIdentity = TLSIdentity.importIdentity(
            data = certData.toByteArray(),
            password = "123".toCharArray(),
            alias = "alias"
        )
        
        config.tlsIdentity = tlsIdentity
        ```

    === "JVM"

        ```kotlin title="in jvmMain"
        config.isTlsDisabled = false
        
        val keyStore = KeyStore.getInstance("PKCS12")
        Files.newInputStream(Path("cert.p12")).use { keyStream ->
            keyStore.load(
                keyStream,
                "keystore-password".toCharArray()
            )
        }
        
        config.tlsIdentity = TLSIdentity.getIdentity(keyStore, "alias", "keyPass".toCharArray())
        ```

1. Ensure TLS is used
2. Get key and certificate data
3. Use the retrieved data to create and store the TLS identity
4. Set this identity as the one presented in response to the client’s prompt

!!! example "<span id='example-7'>Example 7. Create Self-Signed Cert</span>"

    === "Common"

        ```kotlin title="in commonMain"
        config.isTlsDisabled = false
        
        val attrs = mapOf(
            TLSIdentity.CERT_ATTRIBUTE_COMMON_NAME to "Couchbase Demo",
            TLSIdentity.CERT_ATTRIBUTE_ORGANIZATION to "Couchbase",
            TLSIdentity.CERT_ATTRIBUTE_ORGANIZATION_UNIT to "Mobile",
            TLSIdentity.CERT_ATTRIBUTE_EMAIL_ADDRESS to "noreply@couchbase.com"
        )
        
        val tlsIdentity = TLSIdentity.createIdentity(
            true,
            attrs,
            Clock.System.now() + 1.days,
            "cert-alias"
        )
        
        config.tlsIdentity = tlsIdentity
        ```

    === "JVM"

        ```kotlin title="in jvmMain"
        // On the JVM platform, before calling
        // common TLSIdentity.createIdentity() or getIdentity()
        // load a KeyStore to use
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(null, null)
        TLSIdentity.useKeyStore(keyStore)
        ```

1. Ensure TLS is used.
2. Map the required certificate attributes.
3. Create the required TLS identity using the attributes. Add to secure storage as 'cert-alias'.
4. Configure the server to present the defined identity credentials when prompted.

!!! example "<span id='example-8'>Example 8. Use Anonymous Self-Signed Certificate</span>"

    This example uses an _anonymous_ self-signed certificate. Generated certificates are held in secure storage.

    ```kotlin
    config.isTlsDisabled = false
    
    // Use an Anonymous Self-Signed Cert
    config.tlsIdentity = null
    ```

1. Ensure TLS is used.  
   This is the default setting.
2. Authenticate using an anonymous self-signed certificate.  
   This is the default setting.

## Authenticating the Client

**In this section**  
[Use Basic Authentication](#use-basic-authentication) | [Using Client Certificate Authentication
](#using-client-certificate-authentication) | [Delete Entry](#delete-entry) | [The Impact of TLS Settings
](#the-impact-of-tls-settings)

Define how the server (listener) will authenticate the client as one it is prepared to interact with.

Whilst client authentication is optional, Couchbase Lite provides the necessary tools to implement it. Use the
[`URLEndpointListenerConfiguration`](/api/couchbase-lite-ee/kotbase/-u-r-l-endpoint-listener-configuration/) class’s
[`authenticator`](/api/couchbase-lite-ee/kotbase/-u-r-l-endpoint-listener-configuration/authenticator.html) property to
specify how the client-supplied credentials are to be authenticated.

Valid options are:

* No authentication — If you do not define a `ListenerAuthenticator` then all clients are accepted.
* Basic Authentication — uses the [`ListenerPasswordAuthenticator`](
  /api/couchbase-lite-ee/kotbase/-listener-password-authenticator/) to authenticate the client using the client-supplied
  username and password (from the http authentication header).
* [`ListenerCertificateAuthenticator`](/api/couchbase-lite-ee/kotbase/-listener-certificate-authenticator/) — which
  authenticates the client using a client supplied chain of one or more certificates. You should initialize the
  authenticator using one of the following constructors:
    * A list of one or more root certificates — the client supplied certificate must end at a certificate in this list
      if it is to be authenticated
    * A block of code that assumes total responsibility for authentication — it must return a boolean response (`true`
      for an authenticated client, or `false` for a failed authentication).

### Use Basic Authentication

Define how to authenticate client-supplied username and password credentials. To use client-supplied certificates
instead — see [Using Client Certificate Authentication](#using-client-certificate-authentication)

!!! example "Example 9. Password authentication"

    ```kotlin
    config.authenticator = ListenerPasswordAuthenticator { username, password ->
        username == validUser && password.concatToString() == validPassword
    }
    ```

Where `username`/`password` are the client-supplied values (from the http-authentication header) and
`validUser`/`validPassword` are the values acceptable to the server.

### Using Client Certificate Authentication

Define how the server will authenticate client-supplied certificates.

There are two ways to authenticate a client:

* A chain of one or more certificates that ends at a certificate in the list of certificates supplied to the constructor
  for [`ListenerCertificateAuthenticator`](/api/couchbase-lite-ee/kotbase/-listener-certificate-authenticator/) — see
  [Example 10](#example-10)
* Application logic: This method assumes complete responsibility for verifying and authenticating the client — see
  [Example 11](#example-11)<br><br>
  If the parameter supplied to the constructor for `ListenerCertificateAuthenticator` is of type
  `ListenerCertificateAuthenticatorDelegate`, all other forms of authentication are bypassed.<br><br>
  The client response to the certificate request is passed to the method supplied as the constructor parameter. The
  logic should take the form of a function or lambda.

!!! example "Example 10. Set Certificate Authorization"

    Configure the server (listener) to authenticate the client against a list of one or more certificates provided by
    the server to the [`ListenerCertificateAuthenticator`](
    /api/couchbase-lite-ee/kotbase/-listener-certificate-authenticator/).

    ```kotlin
    // Configure the client authenticator
    // to validate using ROOT CA
    // validId.certs is a list containing a client cert to accept
    // and any other certs needed to complete a chain between
    // the client cert and a CA
    val validId = TLSIdentity.getIdentity("Our Corporate Id")
        ?: throw IllegalStateException("Cannot find corporate id")
    
    // accept only clients signed by the corp cert
    val listener = URLEndpointListener(
        URLEndpointListenerConfigurationFactory.create(
            // get the identity 
            database = database,
            identity = validId,
            authenticator = ListenerCertificateAuthenticator(validId.certs)
        )
    )
    ```

1. Get the identity data to authenticate against. This can be, for example, from a resource file provided with the app,
   or an identity previously saved in secure storage.
2. Configure the authenticator to authenticate the client supplied certificate(s) using these root certs. A valid client
   will provide one or more certificates that match a certificate in this list.
3. Add the authenticator to the listener configuration.

!!! example "Example 11. Application Logic"

    Configure the server (listener) to authenticate the client using user-supplied logic.

    ```kotlin
    // Configure authentication using application logic
    val corpId = TLSIdentity.getIdentity("OurCorp")
        ?: throw IllegalStateException("Cannot find corporate id")
    
    config.tlsIdentity = corpId
    
    config.authenticator = ListenerCertificateAuthenticator { certs ->
        // supply logic that returns boolean
        // true for authenticate, false if not
        // For instance:
        certs[0].contentEquals(corpId.certs[0])
    }
    ```

1. Get the identity data to authenticate against. This can be, for example, from a resource file provided with the app,
   or an identity previously saved in secure storage.
2. Configure the authenticator to pass the root certificates to a user supplied code block. This code assumes complete
   responsibility for authenticating the client supplied certificate(s). It must return a boolean value; with `true`
   denoting the client supplied certificate authentic.
3. Add the authenticator to the listener configuration.

### Delete Entry

You can remove unwanted TLS identities from secure storage using the convenience API.

!!! example "Example 12. Deleting TLS Identities"

    ```kotlin
    TLSIdentity.deleteIdentity("cert-alias")
    ```

### The Impact of TLS Settings

The table in this section shows the expected system behavior (in regards to security) depending on the TLS configuration settings deployed.

**Table 1. Expected system behavior**

| isTlsDisabled | tlsIdentity (corresponding to server)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | Expected system behavior                                                                                                                                                                                                                             |
|:--------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `true`        | Ignored                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   | TLS is disabled; all communication is plain text.                                                                                                                                                                                                    |
| `false`       | Set to `null`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | <ul><li>The system will auto generate an _anonymous_ self-signed cert.</li><li>Active Peers (clients) should be configured to accept self-signed certificates.</li><li>Communication is encrypted.</li></ul>                                         |
| `false`       | Set to server identity generated from a self- or CA-signed certificate<ul><li>On first use — Bring your own certificate and private key; for example, using the [`TLSIdentity`](/api/couchbase-lite-ee/kotbase/-t-l-s-identity/) class’s [`createIdentity()`](/api/couchbase-lite-ee/kotbase/-t-l-s-identity/-companion/create-identity.html) method to add it to the secure storage.</li><li>Each time — Use the server identity from the certificate stored in the secure storage; for example, using the [`TLSIdentity`](/api/couchbase-lite-ee/kotbase/-t-l-s-identity/) class’s [`getIdentity()`](/api/couchbase-lite-ee/kotbase/-t-l-s-identity/-companion/get-identity.html) method with the alias you want to retrieve.</li></ul> | <ul><li>System will use the configured identity.</li><li>Active Peers will validate the server certificate corresponding to the `TLSIdentity` (as long as they are configured to not skip validation — see [TLS Security](#tls-security)).</li></ul> |

## Start Listener

Once you have completed the listener’s configuration settings you can initialize the listener instance and start it
running — see [Example 13](#example-13).

!!! example "<span id='example-13'>Example 13. Initialize and start listener</span>"

    ```kotlin
    // Initialize the listener
    val listener = URLEndpointListener(
        URLEndpointListenerConfigurationFactory.create(
            database = database,
            port = 55990,
            networkInterface = "wlan0",
    
            enableDeltaSync = false,
    
            // Configure server security
            disableTls = false,
    
            // Use an Anonymous Self-Signed Cert
            identity = null,
    
            // Configure Client Security using an Authenticator
            // For example, Basic Authentication
            authenticator = ListenerPasswordAuthenticator { usr, pwd ->
                (usr === validUser) && (pwd.concatToString() == validPass)
            }
        )
    )
    
    // Start the listener
    listener.start()
    ```

## Monitor Listener

Use the listener’s [`status`](/api/couchbase-lite-ee/kotbase/-u-r-l-endpoint-listener/status.html) property to get
counts of total and active connections — see [Example 14](#example-14).

You should note that these counts can be extremely volatile. So, the actual number of active connections may have
changed, by the time the [`ConnectionStatus`](/api/couchbase-lite-ee/kotbase/-connection-status/) class returns a
result.

!!! example "<span id='example-14'>Example 14. Get connection counts</span>"

    ```kotlin
    val connectionCount = listener.status?.connectionCount
    val activeConnectionCount = listener.status?.activeConnectionCount
    ```

## Stop Listener

It is best practice to check the status of the listener’s connections and stop only when you have confirmed that there
are no active connections — see [Example 15](#example-15).

!!! example "<span id='example-15'>Example 15. Stop listener using stop method</span>"

    ```kotlin
    listener.stop()
    ```

!!! note

    Closing the database will also close the listener.
