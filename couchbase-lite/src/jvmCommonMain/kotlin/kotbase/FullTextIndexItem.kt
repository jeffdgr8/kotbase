package kotbase

import kotbase.base.DelegatedClass

public actual class FullTextIndexItem
private constructor(actual: com.couchbase.lite.FullTextIndexItem) :
    DelegatedClass<com.couchbase.lite.FullTextIndexItem>(actual) {

    public actual companion object {

        public actual fun property(property: String): FullTextIndexItem =
            FullTextIndexItem(com.couchbase.lite.FullTextIndexItem.property(property))
    }
}
