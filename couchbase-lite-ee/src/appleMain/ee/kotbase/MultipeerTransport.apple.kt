/*
 * Copyright 2026 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import cocoapods.CouchbaseLite.CBLMultipeerTransport
import cocoapods.CouchbaseLite.CBLMultipeerTransportSet
import cocoapods.CouchbaseLite.kCBLMultipeerTransportBluetooth
import cocoapods.CouchbaseLite.kCBLMultipeerTransportNone
import cocoapods.CouchbaseLite.kCBLMultipeerTransportWifi

public actual enum class MultipeerTransport {
    WIFI,
    BLUETOOTH;

    internal val actual: CBLMultipeerTransport
        get() = when (this) {
            WIFI -> kCBLMultipeerTransportWifi
            BLUETOOTH -> kCBLMultipeerTransportBluetooth
        }

    override fun toString(): String = when (this) {
        WIFI -> "wifi"
        BLUETOOTH -> "bluetooth"
    }
}

internal fun CBLMultipeerTransport.toMultipeerTransport(): MultipeerTransport = when (this) {
    kCBLMultipeerTransportWifi -> MultipeerTransport.WIFI
    kCBLMultipeerTransportBluetooth -> MultipeerTransport.BLUETOOTH
    else -> error("Unknown transport: $this")
}

internal fun CBLMultipeerTransport.toMultipeerTransportOrNull(): MultipeerTransport? =
    if (this == kCBLMultipeerTransportNone) null else toMultipeerTransport()

internal val Set<MultipeerTransport>.actual: CBLMultipeerTransportSet
    get() = fold(kCBLMultipeerTransportNone) { acc, transport -> acc or transport.actual }

internal fun CBLMultipeerTransportSet.toMultipeerTransportSet(): Set<MultipeerTransport> = buildSet {
    if (this@toMultipeerTransportSet and kCBLMultipeerTransportWifi != kCBLMultipeerTransportNone) {
        add(MultipeerTransport.WIFI)
    }
    if (this@toMultipeerTransportSet and kCBLMultipeerTransportBluetooth != kCBLMultipeerTransportNone) {
        add(MultipeerTransport.BLUETOOTH)
    }
}
