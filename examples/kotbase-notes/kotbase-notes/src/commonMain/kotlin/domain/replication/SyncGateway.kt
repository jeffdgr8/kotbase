package domain.replication

data class SyncGateway(
    val useTls: Boolean,
    val url: String,
    val port: Int,
    val databaseName: String
) {
    val httpEndpoint = "${if (useTls) "https" else "http"}://$url:$port/$databaseName"

    val wsEndpoint = "${if (useTls) "wss" else "ws"}://$url:$port/$databaseName"
}
