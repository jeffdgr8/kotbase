package kotbase

public expect class FullTextIndexConfiguration(vararg expressions: String) : IndexConfiguration {

    /**
     * The language code which is an ISO-639 language such as "en", "fr", etc.
     * Setting the language code affects how word breaks and word stems are parsed.
     * If not explicitly set, the current locale's language will be used. Setting
     * a null, empty, or unrecognized value will disable the language features.
     */
    public fun setLanguage(language: String?): FullTextIndexConfiguration

    public var language: String?

    /**
     * Set the true value to ignore accents/diacritical marks. The default value is false.
     */
    public fun ignoreAccents(ignoreAccents: Boolean): FullTextIndexConfiguration

    public var isIgnoringAccents: Boolean
}
