_Differences between Couchbase Lite’s QueryBuilder and SQL++ for Mobile_

Couchbase Lite’s SQL++ for Mobile supports all QueryBuilder features, except _Predictive Query_ and _Index_.
See [Table 1](#table-1) for the features supported by SQL++ but not by QueryBuilder.

<span id='table-1'>**Table 1. QueryBuilder Differences**</span>

| Category                   | Components                                                                                                |
|:---------------------------|:----------------------------------------------------------------------------------------------------------|
| Conditional Operator       | `CASE(WHEN … THEN … ELSE …)`                                                                              |
| Array Functions            | `ARRAY_AGG` `ARRAY_AVG` `ARRAY_COUNT` `ARRAY_IFNULL` `ARRAY_MAX` `ARRAY_MIN` `ARRAY_SUM`                  |
| Conditional Functions      | `IFMISSING` `IFMISSINGORNULL` `IFNULL` `MISSINGIF` `NULLIF` `Match` `Functions` `DIV` `IDIV` `ROUND_EVEN` |
| Pattern Matching Functions | `REGEXP_CONTAINS` `REGEXP_LIKE` `REGEXP_POSITION` `REGEXP_REPLACE`                                        |
| Type Checking Functions    | `ISARRAY` `ISATOM` `ISBOOLEAN` `ISNUMBER` `ISOBJECT` `ISSTRING` `TYPE`                                    |
| Type Conversion Functions  | `TOARRAY` `TOATOM` `TOBOOLEAN` `TONUMBER` `TOOBJECT` `TOSTRING`                                           |
