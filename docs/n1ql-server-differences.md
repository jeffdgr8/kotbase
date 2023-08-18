_Differences between Couchbase Server SQL++ and Couchbase Lite SQL++_

!!! important

    N1QL is Couchbase’s implementation of the developing **SQL++** standard. As such the terms _N1QL_ and _SQL++_ are
    used interchangeably in Couchbase documentation unless explicitly stated otherwise.

There are several minor but notable behavior differences between _SQL++ for Mobile_ queries and _SQL++ for Server_, as
shown in [Table 1](#table-1).

In some instances, if required, you can force SQL++ for Mobile to work in the same way as SQL++ for Server. This table compares Couchbase Server and Mobile instances:

<span id='table-1'>**Table 1. SQL++ Query Comparison**</span>

|           Feature            | SQL++ for Couchbase Server                                                                                                                                                     | SQL++ for Mobile                                                                                                                                                                                          |
|:----------------------------:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|           USE KEYS           | SELECT fname, email FROM tutorial USE KEYS ["dave", "ian"];                                                                                                                    | SELECT fname, email FROM tutorial WHERE meta().id IN ("dave", "ian");                                                                                                                                     |
|           ON KEYS            | SELECT * FROM \`user\` u<br>JOIN orders o ON KEYS ARRAY s.order_id<br>FOR s IN u.order_history END;                                                                            | SELECT * FROM user u, u.order_history s<br>JOIN orders o ON s.order_id = meta(o).id;                                                                                                                      |
|            ON KEY            | SELECT * FROM \`user\` u<br>JOIN orders o ON KEY o.user_id FOR u;                                                                                                              | SELECT * FROM user u<br>JOIN orders o ON meta(u).id = o.user_id;                                                                                                                                          |
|             NEST             | SELECT *<br>FROM \`user\` u NEST orders orders ON KEYS ARRAY s.order_id FOR s IN u.order_history END;                                                                          | NEST/UNNEST not supported                                                                                                                                                                                 |
|       LEFT OUTER NEST        | SELECT * FROM user u<br>LEFT OUTER NEST orders orders<br>ON KEYS ARRAY s.order_id FOR s IN u.order_history END;                                                                | NEST/UNNEST not supported                                                                                                                                                                                 |
|            ARRAY             | ARRAY i FOR i IN [1, 2] END                                                                                                                                                    | (SELECT VALUE i FROM [1, 2] AS i)                                                                                                                                                                         |
|         ARRAY FIRST          | ARRAY FIRST arr                                                                                                                                                                | arr[0]                                                                                                                                                                                                    |
|       LIMIT l OFFSET o       | _Does not_ allow OFFSET without LIMIT                                                                                                                                          | Allows OFFSET without LIMIT                                                                                                                                                                               |
| UNION, INTERSECT, and EXCEPT | All three are supported (with ALL and DISTINCT variants)                                                                                                                       | Not supported                                                                                                                                                                                             |
|          OUTER JOIN          | Both LEFT and RIGHT OUTER JOIN supported                                                                                                                                       | Only LEFT OUTER JOIN supported (and necessary for query expressability)                                                                                                                                   |
|   <, <=, =, etc. operators   | Can compare either complex values or scalar values                                                                                                                             | Only scalar values may be compared                                                                                                                                                                        |
|           ORDER BY           | Result sequencing is based on specific rules described in [SQL++ (server) OrderBy clause](https://docs.couchbase.com/server/current/n1ql/n1ql-language-reference/orderby.html) | Result sequencing is based on the SQLite ordering described in [SQLite select overview](https://sqlite.org/lang_select.html)<br>The ordering of Dictionary and Array objects is based on binary ordering. |
|       SELECT DISTINCT        | Supported                                                                                                                                                                      | SELECT DISTINCT VALUE is supported when the returned values are scalars                                                                                                                                   |
|         CREATE INDEX         | Supported                                                                                                                                                                      | Not Supported                                                                                                                                                                                             |
|     INSERT/UPSERT/DELETE     | Supported                                                                                                                                                                      | Not Supported                                                                                                                                                                                             |

## Boolean Logic Rules

| SQL++ for Couchbase Server { style="text-align: center;" }                                                                                                                                                                                                                                                                                                                       | SQL++ for Mobile { style="text-align: center;" }                                                                                                                                                                                                                                                                                                                                                                                                                           |
|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Couchbase Server operates in the same way as Couchbase Lite, except:<ul><li>MISSING, NULL and FALSE are FALSE</li><li>Numbers 0 is FALSE</li><li>Empty strings, arrays, and objects are FALSE</li><li>All other values are TRUE</li></ul>You can choose to use _Couchbase Server’s SQL++ rules_ by using the `TOBOOLEAN(expr)` function to convert a value to its boolean value. | SQL++ for Mobile’s boolean logic rules are based on SQLite’s, so:<ul><li>TRUE is TRUE, and FALSE is FALSE</li><li>Numbers 0 or 0.0 are FALSE</li><li>Arrays and dictionaries are FALSE</li><li>String and Blob are TRUE if the values are casted as a non-zero or FALSE if the values are casted as 0 or 0.0 — see [SQLITE’s CAST and Boolean expressions](https://sqlite.org/lang_expr.html) for more details)</li><li>NULL is FALSE</li><li>MISSING is MISSING</li></ul> |

### Logical Operations

In SQL++ for Mobile logical operations will return one of three possible values: `TRUE`, `FALSE`, or `MISSING`.

Logical operations with the MISSING value could result in `TRUE` or `FALSE` if the result can be determined regardless
of the missing value, otherwise the result will be `MISSING`.

In SQL++ for Mobile — unlike SQL++ for Server — `NULL` is implicitly converted to `FALSE` before evaluating logical
operations. [Table 2](#table-2) summarizes the result of logical operations with different operand values and also shows
where the Couchbase Server behavior differs.

<span id='table-2'>**Table 2. Logical Operations Comparison**</span>

<!-- can't have multiple headers in markdown table, so using raw html -->
<table>
<thead>
<tr>
<th style="text-align: center; vertical-align: bottom;" rowspan="2">Operand<br>a</th>
<th style="text-align: center;" colspan="3">SQL++ for Mobile</th>
<th style="text-align: center;" colspan="3">SQL++ for Server</th>
</tr>
<tr>
<th style="text-align: center;">b</th>
<th style="text-align: center;">a AND b</th>
<th style="text-align: center;">a OR b</th>
<th style="text-align: center;">b</th>
<th style="text-align: center;">a AND b</th>
<th style="text-align: center;">a OR b</th>
</tr>
</thead>
<tbody>
<tr>
<td style="text-align: center;" rowspan="4"><code>TRUE</code></td>
<td style="text-align: center;"><code>TRUE</code></td>
<td style="text-align: center;"><code>TRUE</code></td>
<td style="text-align: center;"><code>TRUE</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
</tr>
<tr>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>TRUE</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
</tr>
<tr>
<td style="text-align: center;"><code>NULL</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>TRUE</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;"><code><strong>NULL</strong></code></td>
<td style="text-align: center;">-</td>
</tr>
<tr>
<td style="text-align: center;"><code>MISSING</code></td>
<td style="text-align: center;"><code>MISSING</code></td>
<td style="text-align: center;"><code>TRUE</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
</tr>
<tr>
<td style="text-align: center;" rowspan="4"><code>FALSE</code></td>
<td style="text-align: center;"><code>TRUE</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>TRUE</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
</tr>
<tr>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
</tr>
<tr>
<td style="text-align: center;"><code>NULL</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
<td style="text-align: center;"><code><strong>NULL</strong></code></td>
</tr>
<tr>
<td style="text-align: center;"><code>MISSING</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>MISSING</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
</tr>
<tr>
<td style="text-align: center;" rowspan="4"><code>NULL</code></td>
<td style="text-align: center;"><code>TRUE</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>TRUE</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;"><code><strong>NULL</strong></code></td>
<td style="text-align: center;">-</td>
</tr>
<tr>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
<td style="text-align: center;"><code><strong>NULL</strong></code></td>
</tr>
<tr>
<td style="text-align: center;"><code>NULL</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;"><code><strong>NULL</strong></code></td>
<td style="text-align: center;"><code><strong>NULL</strong></code></td>
</tr>
<tr>
<td style="text-align: center;"><code>MISSING</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>MISSING</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;"><code><strong>MISSING</strong></code></td>
<td style="text-align: center;"><code><strong>NULL</strong></code></td>
</tr>
<tr>
<td style="text-align: center;" rowspan="4"><code>MISSING</code></td>
<td style="text-align: center;"><code>TRUE</code></td>
<td style="text-align: center;"><code>MISSING</code></td>
<td style="text-align: center;"><code>TRUE</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
</tr>
<tr>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>MISSING</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
</tr>
<tr>
<td style="text-align: center;"><code>NULL</code></td>
<td style="text-align: center;"><code>FALSE</code></td>
<td style="text-align: center;"><code>MISSING</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;"><code><strong>MISSING</strong></code></td>
<td style="text-align: center;"><code><strong>NULL</strong></code></td>
</tr>
<tr>
<td style="text-align: center;"><code>MISSING</code></td>
<td style="text-align: center;"><code>MISSING</code></td>
<td style="text-align: center;"><code>MISSING</code></td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
<td style="text-align: center;">-</td>
</tr>
</tbody>
</table>

## CRUD Operations

SQL++ for Mobile only supports Read or Query operations.

SQL++ for Server fully supports CRUD operation.

## Functions

### Division Operator

| SQL++ for Server { style="text-align: center;" }                                                                                                                                   | SQL++ for Mobile { style="text-align: center;" }                                                                                                                              |
|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SQL++ for Server always performs float division regardless of the types of the operands.<br><br>You can force this behavior in SQL++ for Mobile by using the `DIV(x, y)` function. | The operand types determine the division operation performed.<br>If both are integers, integer division is used.<br>If one is a floating number, then float division is used. |

### Round Function

| SQL++ for Server { style="text-align: center;" }                                                                                                                                                               | SQL++ for Mobile { style="text-align: center;" }                                                                                                                                                                                                                                                                                                                           |
|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SQL++ for Server `ROUND()` uses the _Rounding to Nearest Even_ convention (for example, `ROUND(1.85)` returns 1.8).<br><br>You can force this behavior in Couchbase Lite by using the `ROUND_EVEN()` function. | The `ROUND()` function returns a value to the given number of integer digits to the right of the decimal point (left if digits is negative).<ul><li>Digits are 0 if not given.</li><li>Midpoint values are handled using the _Rounding Away From Zero_ convention, which rounds them to the next number away from zero (for example, `ROUND(1.85)` returns 1.9).</li></ul> |
