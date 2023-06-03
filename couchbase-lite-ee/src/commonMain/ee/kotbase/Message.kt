package kotbase

/**
 * **ENTERPRISE EDITION API**
 *
 * A message sent between message endpoint connections.
 */
public expect class Message {

    /**
     * Gets the message as data.
     *
     * @return the data
     */
    public fun toData(): ByteArray

    public companion object {

        /**
         * Creates a message object from data.
         *
         * @param data the data
         * @return the Message object
         */
        public fun fromData(data: ByteArray): Message
    }
}
