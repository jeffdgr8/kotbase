package kotbase

import kotbase.base.actuals
import com.couchbase.lite.IndexBuilder as CBLIndexBuilder

public actual object IndexBuilder {

    public actual fun valueIndex(vararg items: ValueIndexItem): ValueIndex =
        ValueIndex(CBLIndexBuilder.valueIndex(*items.actuals()))

    public actual fun fullTextIndex(vararg items: FullTextIndexItem): FullTextIndex =
        FullTextIndex(CBLIndexBuilder.fullTextIndex(*items.actuals()))
}
