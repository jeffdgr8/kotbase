_Couchbase Lite database data querying concepts — full text search_

## Overview

To run a full-text search (FTS) query, you must create a full-text index on the expression being matched. Unlike regular
queries, the index is not optional.

You can choose to use SQL++ or `QueryBuilder` syntaxes to create and use FTS indexes.

The following examples use the data model introduced in [Indexing](indexing.md). They create and use an FTS index built
from the hotel’s `overview` text.

## SQL++

### Create Index

SQL++ provides a configuration object to define Full Text Search indexes — `FullTextIndexConfiguration`.

!!! example "Example 1. Using SQL++'s FullTextIndexConfiguration"

    ```kotlin
    database.createIndex(
        "overviewFTSIndex",
        FullTextIndexConfiguration("overview")
    )
    ```

### Use Index

Full-text search is enabled using the SQL++ `match()` function.

With the index created, you can construct and run a full-text search (FTS) query using the indexed properties.

The index will omit a set of common words, to avoid words like "I", "the", and "an" from overly influencing your
queries. See [full list of these stop words](
https://github.com/couchbasedeps/sqlite3-unicodesn/blob/HEAD/stopwords_en.h).

The following example finds all hotels mentioning _Michigan_ in their `overview` text.

!!! example "Example 2. Using SQL++ Full Text Search"

    ```kotlin
    val ftsQuery = database.createQuery(
        "SELECT _id, overview FROM _ WHERE MATCH(overviewFTSIndex, 'michigan') ORDER BY RANK(overviewFTSIndex)"
    )
    ftsQuery.execute().use { rs ->
        rs.allResults().forEach {
            println("${it.getString("id")}: ${it.getString("overview")}")
        }
    }
    ```

## QueryBuilder

### Create Index

The following example creates an FTS index on the `overview` property.

!!! example "Example 3. Using the IndexBuilder method"

    ```kotlin
    database.createIndex(
        "overviewFTSIndex",
        IndexBuilder.fullTextIndex(FullTextIndexItem.property("overview"))
    )
    ```

### Use Index

With the index created, you can construct and run a full-text search (FTS) query using the indexed properties.

The following example finds all hotels mentioning _Michigan_ in their `overview` text.

!!! example "Example 4. Using QueryBuilder Full Text Search"

    ```kotlin
    val ftsQuery = QueryBuilder
        .select(
            SelectResult.expression(Meta.id),
            SelectResult.property("overview")
        )
        .from(DataSource.database(database))
        .where(FullTextFunction.match("overviewFTSIndex", "michigan"))
    
    ftsQuery.execute().use { rs ->
        rs.allResults().forEach {
            println("${it.getString("Meta.id")}: ${it.getString("overview")}")
        }
    }
    ```

## Operation

In the examples above, the pattern to match is a word, the full-text search query matches all documents that contain the
word "michigan" in the value of the `doc.overview` property.

Search is supported for all languages that use whitespace to separate words.

Stemming, which is the process of fuzzy matching parts of speech, like "fast" and "faster", is supported in the
following languages: Danish, Dutch, English, Finnish, French, German, Hungarian, Italian, Norwegian, Portuguese,
Romanian, Russian, Spanish, Swedish and Turkish.

## Pattern Matching Formats

As well as providing specific words or strings to match against, you can provide the pattern to match in these formats.

### Prefix Queries

The query expression used to search for a term prefix is the prefix itself with a "*" character appended to it.

!!! example "Example 5. Prefix query"

    Query for all documents containing a term with the prefix "lin".

    ```
    "lin*"
    ```

    This will match:

    * All documents that contain "linux"
    * And … those that contain terms "linear", "linker", "linguistic", and so on.

### Overriding the Property Name

Normally, a token or token prefix query is matched against the document property specified as the left-hand side of the
`match` operator. This may be overridden by specifying a property name followed by a ":" character before a basic term
query. There may be space between the ":" and the term to query for, but not between the property name and the ":"
character.

!!! example "Example 6. Override indexed property name"

    Query the database for documents for which the term "linux" appears in the document title, and the term "problems"
    appears in either the title or body of the document.

    ```
    'title:linux problems'
    ```

### Phrase Queries

A _phrase query_ is one that retrieves all documents containing a nominated set of terms or term prefixes in a specified
order with no intervening tokens.

Phrase queries are specified by enclosing a space separated sequence of terms or term prefixes in double quotes (").

!!! example "Example 7. Phrase query"

    Query for all documents that contain the phrase "linux applications".

    ```
    "linux applications"
    ```

### NEAR Queries

A NEAR query is a query that returns documents that contain two or more nominated terms or phrases within a specified
proximity of each other (by default with 10 or less intervening terms). A NEAR query is specified by putting the keyword
"NEAR" between two phrases, tokens or token prefix queries. To specify a proximity other than the default, an operator
of the form "NEAR/<number\>" may be used, where <number\> is the maximum number of intervening terms allowed.

!!! example "Example 8. Near query"

    Search for a document that contains the phrase "replication" and the term "database" with not more than 2 terms
    separating the two.

    ```
    "database NEAR/2 replication"
    ```

### AND, OR & NOT Query Operators

The enhanced query syntax supports the AND, OR and NOT binary set operators. Each of the two operands to an operator may
be a basic FTS query, or the result of another AND, OR or NOT set operation. Operators must be entered using capital
letters. Otherwise, they are interpreted as basic term queries instead of set operators.

!!! example "Example 9. Using And, Or and Not"

    Return the set of documents that contain the term "couchbase", and the term "database".

    ```
    "couchbase AND database"
    ```

### Operator Precedence

When using the enhanced query syntax, parenthesis may be used to specify the precedence of the various operators.

!!! example "Example 10. Operator precedence"

    Query for the set of documents that contains the term "linux", and at least one of the phrases "couchbase database"
    and "sqlite library".

    ```
    '("couchbase database" OR "sqlite library") AND "linux"'
    ```

## Ordering Results

It’s very common to sort full-text results in descending order of relevance. This can be a very difficult heuristic to
define, but Couchbase Lite comes with a ranking function you can use.

In the `OrderBy` array, use a string of the form `Rank(X)`, where `X` is the property or expression being searched, to
represent the ranking of the result.
