-dontwarn android.**
-dontwarn javax.annotation.**
-dontwarn edu.umd.cs.findbugs.annotations.*
-dontwarn kotlinx.serialization.**
-dontwarn org.conscrypt.*

-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings

-keep class com.couchbase.lite.ConnectionStatus { <init>(...); }
-keep class com.couchbase.lite.LiteCoreException { static <methods>; }
-keep class com.couchbase.lite.internal.replicator.CBLTrustManager {
    public java.util.List checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String, java.lang.String);
}
-keep interface com.couchbase.lite.internal.ReplicationCollection$C4Filter
-keep class com.couchbase.lite.internal.ReplicationCollection {
    static <methods>;
    <fields>;
}
-keep class com.couchbase.lite.internal.fleece.FLSliceResult { static <methods>; }
-keep class com.couchbase.lite.internal.core.C4* {
    static <methods>;
    <fields>;
    <init>(...);
}
