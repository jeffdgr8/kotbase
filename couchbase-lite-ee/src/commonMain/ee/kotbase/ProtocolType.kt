/*
 * Copyright 2022-2023 Jeff Lockhart
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

/**
 * **ENTERPRISE EDITION API**
 *
 * The protocol type of the data transportation.
 */
public expect enum class ProtocolType {

    /**
     * MESSAGE protocol means that Core callbacks contain exactly
     * the data that needs to be transferred.  Core does not format
     * the data in any way.
     */
    MESSAGE_STREAM,

    /**
     * BYTE protocol means that Core knows that this is a web socket
     * connection.  The data with which Core calls us contains the
     * properly framed message, heartbeats and so on.
     * ... we don't use this because that would be too easy, right?
     * OkHTTP also wants to frame the data.
     */
    BYTE_STREAM
}
