_How to use SQL++ query strings to build effective queries with Kotbase_

!!! note

    The examples used in this topic are based on the Travel Sample app and data introduced in the [Couchbase Mobile
    Workshop](https://docs.couchbase.com/tutorials/mobile-travel-tutorial/introduction.html) tutorial.

## Introduction

Developers using Kotbase can provide SQL++ query strings using the SQL++ Query API. This API uses query statements of
the form shown in [Example 2](#example-2).

The structure and semantics of the query format are based on that of Couchbase Server’s SQL++ query language — see
[SQL++ Reference Guide](https://docs.couchbase.com/server/current/n1ql/n1ql-language-reference/index.html) and [SQL++
Data Model](https://docs.couchbase.com/server/current/learn/data/n1ql-versus-sql.html).

## Running

The database can create a query object with the SQL++ string. See [Query Result Sets](result-sets.md) for how to work
with result sets.

!!! example "Example 1. Running a SQL++ Query"

    ```kotlin
    val query = database.createQuery(
        "SELECT META().id AS id FROM _ WHERE type = \"hotel\""
    )
    return query.execute().use { rs -> rs.allResults() }
    ```

We are accessing the current database using the shorthand notation `_` — see the [`FROM`](#from) clause for more on data
source selection and [Query Parameters](#query-parameters) for more on parameterized queries.

## Query Format

The API uses query statements of the form shown in [Example 2](#example-2).

!!! example "<span id='example-2'>Example 2. Query Format</span>"

    ```sql
    SELECT ____
    FROM 'data-source'
    WHERE ____,
    JOIN ____
    GROUP BY ____
    ORDER BY ____
    LIMIT ____
    OFFSET ____
    ```

**Query Components**

| Component                                 | Description                                                                                                            |
|:------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------|
| [SELECT statement](#select-statement)     | The document properties that will be returned in the result set                                                        |
| [FROM](#from)                             | The data source to be queried                                                                                          |
| [WHERE statement](#where-statement)       | The query criteria<br>The `SELECT`ed properties of documents matching this criteria will be returned in the result set |
| [JOIN statement](#join-statement)         | The criteria for joining multiple documents                                                                            |
| [GROUP BY statement](#group-by-statement) | The criteria used to group returned items in the result set                                                            |
| [ORDER BY statement](#order-by-statement) | The criteria used to order the items in the result set                                                                 |
| [LIMIT statement](#limit-statement)       | The maximum number of results to be returned                                                                           |
| [OFFSET statement](#offset-statement)     | The number of results to be skipped before starting to return results                                                  |

!!! tip

    We recommend working through the [SQL++ Tutorials](https://query-tutorial.couchbase.com/tutorial) to build your
    SQL++ skills.

## SELECT statement

### Purpose

Projects the result returned by the query, identifying the columns it will contain.

### Syntax

!!! example "Example 3. SQL++ Select Syntax"

    ```sql
    select = SELECT _ ( DISTINCT | ALL )? selectResult
    
    selectResults = selectResult ( _ ',' _ selectResult )*
    
    selectResult = expression ( _ (AS)? columnAlias )?
    
    columnAlias = IDENTIFIER
    ```

### Arguments

1. The select clause begins with the `SELECT` keyword.
    * The optional `ALL` argument is used to specify that the query should return ALL results (the default).
    * The optional `DISTINCT` argument specifies that the query should remove duplicated results.
2. `selectResults` is a list of columns projected in the query result. Each column is an expression which could be a
   property expression or any expressions or functions. You can use the wildcard `*` to select all columns — see [Select
   Wildcard](#select-wildcard).
3. Use the optional `AS` argument to provide an alias name for a property. Each property can be aliased by putting the
   `AS <alias name>` after the column name.

#### Select Wildcard

When using the `SELECT *` option the column name (key) of the SQL++ string is one of:

* The alias name if one was specified
* The data source name (or its alias if provided) as specified in the `FROM` clause.

This behavior is inline with that of Couchbase Server SQL++ — see example in [Table 1](#table-1).

**Table 1. Example Column Names for SELECT \***

| Query                       | Column Name |
|:----------------------------|:------------|
| `SELECT * AS data FROM _`   | `data`      |
| `SELECT * FROM _`           | `_`         |
| `SELECT * FROM _default`    | `_default`  |
| `SELECT * FROM db`          | `db`        |
| `SELECT * FROM db AS store` | `store`     |

### Example

!!! example "Example 4. SELECT properties"

    ```sql
    SELECT *
    
    SELECT db.* AS data
    
    SELECT name fullName
    
    SELECT db.name fullName
    
    SELECT DISTINCT address.city
    ```

    1. Use the `*` wildcard to select all properties.
    2. Select all properties from the `db` data source. Give the object an alias name of `data`.
    3. Select a pair of properties.
    4. Select a specific property from the db data source.
    5. Select the property item `city` from its parent property `address`.

See [Query Result Sets](result-sets.md) for more on processing query results.

## FROM

### Purpose

Specifies the data source, or sources, and optionally applies an alias (`AS`). It is mandatory.

### Syntax

```sql
FROM dataSource
      (optional JOIN joinClause )
```

### Arguments

1. Here `dataSource` is the database name against which the query is to run. Use `AS` to give the database an alias you
   can use within the query.  
   To use the current database, without specifying a name, use `_` as the datasource.
2. `JOIN joinclause` — use this optional argument to link data sources — see [`JOIN` statement](#join-statement).

### Example

!!! example "Example 5. FROM clause"

    ```sql
    SELECT name FROM db
    SELECT store.name FROM db AS store
    SELECT store.name FROM db store
    SELECT name FROM _
    SELECT store.name FROM _ AS store
    SELECT store.name FROM _ store
    ```

## JOIN statement

### Purpose

The `JOIN` clause enables you to select data from multiple data sources linked by criteria specified in the `JOIN`
statement.

Currently only self-joins are supported. For example to combine airline details with route details, linked by the
airline id — see [Example 6](#example-6).

### Syntax

```sql
joinClause = ( join )*

join = joinOperator _ dataSource _  (constraint)?

joinOperator = ( LEFT (OUTER)? | INNER | CROSS )? JOIN

dataSource = databaseName ( ( AS | _ )? databaseAlias )?

constraint ( ON expression )?
```

### Arguments

1. The join clause starts with a `JOIN` operator followed by the data source.
2. Five `JOIN` operators are supported:  
   `JOIN`, `LEFT JOIN`, `LEFT OUTER JOIN`, `INNER JOIN`, and `CROSS JOIN`.
   Note: `JOIN` and `INNER JOIN` are the same, `LEFT JOIN` and `LEFT OUTER JOIN` are the same.
3. The join constraint starts with the `ON` keyword followed by the expression that defines the joining constraints.

### Example

```sql
SELECT db.prop1, other.prop2 FROM db JOIN db AS other ON db.key = other.key

SELECT db.prop1, other.prop2 FROM db LEFT JOIN db other ON db.key = other.key

SELECT * FROM route r JOIN airline a ON r.airlineid = meta(a).id WHERE a.country = "France"
```

!!! example "<span id='example-6'>Example 6. Using JOIN to Combine Document Details</span>"

    This example JOINS the document of type `route` with documents of type `airline` using the document ID (`_id`) on
    the _airline_ document and `airlineid` on the _route_ document.

    ```sql
    SELECT * FROM travel-sample r JOIN travel-sample a ON r.airlineid = a.meta.id WHERE a.country = "France"
    ```

## WHERE statement

### Purpose

Specifies the selection criteria used to filter results.

As with SQL, use the `WHERE` statement to choose which documents are returned by your query.

### Syntax

```sql
where = WHERE expression
```

### Arguments

`WHERE` evaluates `expression` to a `BOOLEAN` value. You can chain any number of expressions in order to implement
sophisticated filtering capabilities.

See also — [Operators](#operators) for more on building expressions and [Query Parameters](#query-parameters) for more
on parameterized queries.

### Examples

```sql
SELECT name FROM db WHERE department = 'engineer' AND group = 'mobile'
```

## GROUP BY statement

### Purpose

Use `GROUP BY` to arrange values in groups of one or more properties.

### Syntax

```sql
groupBy = grouping _( having )?

grouping = GROUP BY expression( _ ',' _ expression )*

having = HAVING expression
```

### Arguments

1. The group by clause starts with the `GROUP BY` keyword followed by one or more expressions.
2. `grouping` — the group by clause is normally used together with the aggregate functions (e.g. `COUNT`, `MAX`, `MIN`,
   `SUM`, `AVG`).
3. `having` — allows you to filter the result based on aggregate functions — for example, `HAVING count(empnum)>100`.

### Examples

```sql
SELECT COUNT(empno), city FROM db GROUP BY city

SELECT COUNT(empno), city FROM db GROUP BY city HAVING COUNT(empno) > 100

SELECT COUNT(empno), city FROM db GROUP BY city HAVING COUNT(empno) > 100 WHERE state = 'CA'
```

## ORDER BY statement

### Purpose

Sort query results based on a given expression result.

### Syntax

```sql
orderBy = ORDER BY ordering ( _ ',' _ ordering )*

ordering = expression ( _ order )?

order = ( ASC / DESC )
```

### Arguments

1. `orderBy` — The order by clause starts with the `ORDER BY` keyword followed by the ordering clause.
2. `ordering` — The ordering clause specifies the properties or expressions to use for ordering the results.
3. `order` — In each ordering clause, the sorting direction is specified using the optional `ASC` (ascending) or `DESC`
   (descending) directives. Default is `ASC`.

### Examples

!!! example "Example 7. Simple usage"

    ```sql
    SELECT name FROM db  ORDER BY name
    
    SELECT name FROM db  ORDER BY name DESC
    
    SELECT name, score FROM db  ORDER BY name ASC, score DESC
    ```

## LIMIT statement

### Purpose

Specifies the maximum number of results to be returned by the query.

### Syntax

```sql
limit = LIMIT expression
```

### Arguments

The limit clause starts with the `LIMIT` keyword followed by an expression that will be evaluated as a number.

### Examples

!!! example "Example 8. Simple usage"

    ```sql
    SELECT name FROM db LIMIT 10
    ```

    Return only 10 results

## OFFSET statement

### Purpose

Specifies the number of results to be skipped by the query.

### Syntax

```sql
offset = OFFSET expression
```

### Arguments

The offset clause starts with the `OFFSET` keyword followed by an expression that will be evaluated as a number that
represents the number of results ignored before the query begins returning results.

### Examples

!!! example "Example 9. Simple usage"

    ```sql
    SELECT name FROM db OFFSET 10
    
    SELECT name FROM db  LIMIT 10 OFFSET 10
    ```

    1. Ignore first 10 results
    2. Ignore first 10 results then return the next 10 results

## Expressions

**In this section**  
[Literals](#literals) | [Identifiers](#identifiers) | [Property Expressions](#property-expressions) | [Any and Every
Expressions](#any-and-every-expressions) | [Parameter Expressions](#parameter-expressions) | [Parenthesis
Expressions](#parenthesis-expressions)

Expressions are references to identifiers that resolve to values. Categories of expression comprise the elements covered
in this section (see above), together with [Operators](#operators) and [Functions](#functions], which are covered in
their own sections.

### Literals

[Boolean](#boolean) | [Numeric](#numeric) | [String](#string) | [NULL](#null) | [MISSING](#missing) | [Array](#array) |
[Dictionary](#dictionary)

#### Boolean

##### Purpose

Represents a true or false value.

##### Syntax

`TRUE` | `FALSE`

##### Example

```sql
SELECT value FROM db  WHERE value = true
SELECT value FROM db  WHERE value = false
```

#### Numeric

##### Purpose

Represents a numeric value. Numbers may be signed or unsigned digits. They have optional fractional and exponent
components.

##### Syntax

```sql
'-'? (('.' DIGIT+) | (DIGIT+ ('.' DIGIT*)?)) ( [Ee] [-+]? DIGIT+ )? WB

DIGIT = [0-9]
```

##### Example

```sql
SELECT value FROM db  WHERE value = 10
SELECT value FROM db  WHERE value = 0
SELECT value FROM db WHERE value = -10
SELECT value FROM db WHERE value = 10.25
SELECT value FROM db WHERE value = 10.25e2
SELECT value FROM db WHERE value = 10.25E2
SELECT value FROM db WHERE value = 10.25E+2
SELECT value FROM db WHERE value = 10.25E-2
```

#### String

##### Purpose

The string literal represents a string or sequence of characters.

##### Syntax

```sql
"characters" | 'characters'
```

The string literal can be double-quoted as well as single-quoted.

##### Example

```sql
SELECT firstName, lastName FROM db WHERE middleName = "middle"
SELECT firstName, lastName FROM db WHERE middleName = 'middle'
```

#### NULL

##### Purpose

The literal `NULL` represents an empty value.

##### Syntax

```sql
NULL
```

##### Example

```sql
SELECT firstName, lastName FROM db WHERE middleName IS NULL
```

#### MISSING

##### Purpose

The `MISSING` literal represents a missing name-value pair in a document.

##### Syntax

```sql
MISSING
```

##### Example

```sql
SELECT firstName, lastName FROM db WHERE middleName IS MISSING
```

#### Array

##### Purpose

Represents an Array.

##### Syntax

```sql
arrayLiteral = '[' _ (expression ( _ ',' _ e2:expression )* )? ']'
```

##### Example

```sql
SELECT ["a", "b", "c"] FROM _
SELECT [ property1, property2, property3] FROM _
```

#### Dictionary

##### Purpose

Represents a dictionary literal.

##### Syntax

```sql
dictionaryLiteral = '{' _ ( STRING_LITERAL ':' e:expression
  ( _ ',' _ STRING_LITERAL ':' _ expression )* )?
   '}'
```

##### Example

```sql
SELECT { 'name': 'James', 'department': 10 } FROM db
SELECT { 'name': 'James', 'department': dept } FROM db
SELECT { 'name': 'James', 'phones': ['650-100-1000', '650-100-2000'] } FROM db
```

### Identifiers

#### Purpose

Identifiers provide symbolic references. Use them for example to identify: column alias names, database names, database
alias names, property names, parameter names, function names, and FTS index names.

#### Syntax

```sql
<[a-zA-Z_] [a-zA-Z0-9_$]*> _ | "`" ( [^`] | "``"   )* "`"  _
```

The identifier allows a-z, A-Z, 0-9, _ (underscore), and $ character.  
The identifier is case sensitive.

!!! tip

    To use other characters in the identifier, surround the identifier with the backtick ` character.

#### Example

!!! example "Example 10. Identifiers"

    ```sql
    SELECT * FROM _
    
    SELECT * FROM `db-1`
    
    SELECT key FROM db
    
    SELECT key$1 FROM db_1
    
    SELECT `key-1` FROM db
    ```

    Use of backticks allows a hyphen as part of the identifier name.

### Property Expressions

#### Purpose

The property expression is used to reference a property in a document.

#### Syntax

```sql
property = '*'| dataSourceName '.' _ '*'  | propertyPath

propertyPath = propertyName (
    ('.' _ propertyName ) |
    ('[' _ INT_LITERAL _ ']' _  )
    )*

propertyName = IDENTIFIER
```

1. Prefix the property expression with the data source name or alias to indicate its origin.
2. Use dot syntax to refer to nested properties in the propertyPath.
3. Use bracket (`[index]`) syntax to refer to an item in an array.
4. Use the asterisk (`*`) character to represents all properties. This can only be used in the result list of the
   `SELECT` clause.

#### Example

!!! example "Example 11. Property Expressions"

    ```sql
    SELECT *
      FROM db
      WHERE contact.name = "daniel"
    
    SELECT db.*
      FROM db
      WHERE collection.contact.name = "daniel"
    
    SELECT collection.contact.address.city
      FROM scope.collection
      WHERE collection.contact.name = "daniel"
    
    SELECT contact.address.city
      FROM scope.collection
      WHERE contact.name = "daniel"
    
    SELECT contact.address.city, contact.phones[0]
      FROM db
      WHERE contact.name = "daniel"
    ```

### Any and Every Expressions

#### Purpose

Evaluates expressions over items in an array object.

#### Syntax

```sql
arrayExpression = 
  anyEvery _ variableName 
     _ IN  _ expression 
       _ SATISFIES _ expression 
    END 

anyEvery = anyOrSome AND EVERY | anyOrSome | EVERY

anyOrSome = ANY | SOME
```

1. The array expression starts with `ANY/SOME`, `EVERY`, or `ANY/SOME AND EVERY`, each of which has a different function
   as described below, and is terminated by `END`
    * `ANY/SOME`: Returns `TRUE` if at least one item in the array satisfies the expression, otherwise returns `FALSE`.  
      NOTE: `ANY` and `SOME` are interchangeable.
    * `EVERY`: Returns `TRUE` if all items in the array satisfies the expression, otherwise return `FALSE`. If the array
      is empty, returns `TRUE`.
    * `ANY/SOME AND EVERY`: Same as `EVERY` but returns `FALSE` if the array is empty.
2. The variable name represents each item in the array.
3. The `IN` keyword is used for specifying the array to be evaluated.
4. The `SATISFIES` keyword is used for evaluating each item in the array.
5. `END` terminates the array expression.

#### Example

!!! example "Example 12. ALL and Every Expressions"

    ```sql
    SELECT name
      FROM db
      WHERE ANY v
              IN contacts
              SATISFIES v.city = 'San Mateo'
            END
    ```

### Parameter Expressions

#### Purpose

Parameter expressions specify a value to be assigned from the parameter map presented when executing the query.

!!! note

    If parameters are specified in the query string, but the parameter and value mapping is not specified in the query
    object, an error will be thrown when executing the query.

#### Syntax

```sql
$IDENTIFIER
```

#### Examples

!!! example "Example 13. Parameter Expression"

    ```sql
    SELECT name
      FROM db
      WHERE department = $department
    ```

!!! example "Example 14. Using a Parameter"

    ```kotlin
    val query = database.createQuery("SELECT name WHERE department = \$department")
    query.parameters = Parameters().setValue("department", "E001")
    val result = query.execute()
    ```

    The query resolves to `SELECT name WHERE department = "E001"`

### Parenthesis Expressions

#### Purpose

Use parentheses to group expressions together to make them more readable or to establish operator precedences.

#### Example

!!! example "Example 15. Parenthesis Expression"

    ```sql
    -- Establish the desired operator precedence; do the addition before the multiplication
    SELECT (value1 + value2) * value 3
      FROM db
    
    SELECT *
      FROM db
      WHERE ((value1 + value2) * value3) + value4 = 10
    
    SELECT *
      FROM db
      -- Clarify the conditional grouping
      WHERE (value1 = value2)
         OR (value3 = value4)
    ```

## Operators

**In this section**  
[Binary Operators](#binary-operators) | [Unary Operators](#unary-operators) | [COLLATE Operators](#collate-operators) |
[CONDITIONAL Operator](#conditional-operator)

### Binary Operators

[Maths](#maths) | [Comparison Operators](#comparison-operators) | [Logical Operators](#logical-operators) | [String
Operator](#string-operator)

#### Maths

**Table 2. Maths Operators**

| Op  | Desc                | Example              |
|:---:|:--------------------|:---------------------|
| `+` | Add                 | `WHERE v1 + v2 = 10` |
| `-` | Subtract            | `WHERE v1 - v2 = 10` |
| `*` | Multiply            | `WHERE v1 * v2 = 10` |
| `/` | Divide — see note ¹ | `WHERE v1 / v2 = 10` |
| `%` | Modulo              | `WHERE v1 % v2 = 0`  |

¹ If both operands are integers, integer division is used, but if one is a floating number, then float division is used.
This differs from Server SQL++, which performs float division regardless. Use DIV(x, y) to force float division in CBL
SQL++.

#### Comparison Operators

##### Purpose

The _comparison operators_ are used in the `WHERE` statement to specify the condition on which to match documents.

**Table 3. Comparison Operators**

|        Op        | Desc                                                                                                                                                                      | <div style="min-width:216px">Example</div>                                                                                                                                                                                |
|:----------------:|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|   `=` or `==`    | Equals                                                                                                                                                                    | `WHERE v1 = v2`<br>`WHERE v1 == v2`                                                                                                                                                                                       |
|   `!=` or `<>`   | Not Equal to                                                                                                                                                              | `WHERE v1 != v2`<br>`WHERE v1 <> v2`                                                                                                                                                                                      |
|       `>`        | Greater than                                                                                                                                                              | `WHERE v1 > v2`                                                                                                                                                                                                           |
|       `>=`       | Greater than or equal to                                                                                                                                                  | `WHERE v1 >= v2`                                                                                                                                                                                                          |
|       `>`        | Less than                                                                                                                                                                 | `WHERE v1 < v2`                                                                                                                                                                                                           |
|       `>=`       | Less than or equal to                                                                                                                                                     | `WHERE v1 ⇐ v2`                                                                                                                                                                                                           |
|       `IN`       | Returns `TRUE` if the value is in the list or array of values specified by the right hand side expression; Otherwise returns `FALSE`.                                     | `WHERE "James" IN contactsList`                                                                                                                                                                                           |
|      `LIKE`      | String wildcard pattern matching ² comparison. Two wildcards are supported:<ul><li>`%` Matches zero or more characters.</li><li>`_` Matches a single character.</li></ul> | `WHERE name LIKE 'a%'`<br>`WHERE name LIKE '%a'`<br>`WHERE name LIKE '%or%'`<br>`WHERE name LIKE 'a%o%'`<br>`WHERE name LIKE '%_r%'`<br>`WHERE name LIKE '%a_%'`<br>`WHERE name LIKE '%a__%'`<br>`WHERE name LIKE 'aldo'` |
|     `MATCH`      | String matching using FTS see Full Text Search Functions                                                                                                                  | `WHERE v1-index MATCH "value"`                                                                                                                                                                                            |
|    `BETWEEN`     | Logically equivalent to `v1>=X and v1<=Y`                                                                                                                                 | `WHERE v1 BETWEEN 10 and 100`                                                                                                                                                                                             |
|   `IS NULL` ³    | Equal to `NULL`                                                                                                                                                           | `WHERE v1 IS NULL`                                                                                                                                                                                                        |
|  `IS NOT NULL`   | Not equal to `NULL`                                                                                                                                                       | `WHERE v1 IS NOT NULL`                                                                                                                                                                                                    |
|   `IS MISSING`   | Equal to `MISSING`                                                                                                                                                        | `WHERE v1 IS MISSING`                                                                                                                                                                                                     |
| `IS NOT MISSING` | Not equal to `MISSING`                                                                                                                                                    | `WHERE v1 IS NOT MISSING`                                                                                                                                                                                                 |
|   `IS VALUED`    | `IS NOT NULL AND MISSING`                                                                                                                                                 | `WHERE v1 IS VALUED`                                                                                                                                                                                                      |
| `IS NOT VALUED`  | `IS NULL OR MISSING`                                                                                                                                                      | `WHERE v1 IS NOT VALUED`                                                                                                                                                                                                  |

² Matching is case-insensitive for ASCII characters, case-sensitive for non-ASCII.

³ Use of `IS` and `IS NOT` is limited to comparing `NULL` and `MISSING` values (this encompasses `VALUED`). This is 
different from `QueryBuilder`, in which they operate as equivalents of `==` and `!=`.

**Table 4. Comparing NULL and MISSING values using IS**

|       OP       | NON-NULL Value | NULL  | MISSING |
|:--------------:|:--------------:|:-----:|:-------:|
|    IS NULL     |     FALSE      | TRUE  | MISSING |
|  IS NOT NULL   |      TRUE      | FALSE | MISSING |
|   IS MISSING   |     FALSE      | FALSE |  TRUE   |
| IS NOT MISSING |      TRUE      | TRUE  |  FALSE  |
|   IS VALUED    |      TRUE      | FALSE |  FALSE  |
| IS NOT VALUED  |     FALSE      | TRUE  |  TRUE   |

#### Logical Operators

##### Purpose

Logical operators combine expressions using the following Boolean Logic Rules:

* TRUE is TRUE, and FALSE is FALSE
* Numbers 0 or 0.0 are FALSE
* Arrays and dictionaries are FALSE
* String and Blob are TRUE if the values are casted as a non-zero or FALSE if the values are casted as 0 or 0.0
* NULL is FALSE
* MISSING is MISSING

!!! note

    This is different from Server SQL++, where:

    * MISSING, NULL and FALSE are FALSE
    * Numbers 0 is FALSE
    * Empty strings, arrays, and objects are FALSE
    * All other values are TRUE

    !!! tip
    
        Use TOBOOLEAN(expr) function to convert a value based on Server SQL++ boolean value rules.

**Table 5. Logical Operators**

|  Op   | Description                                                                                                                                                                                                                                                                                                                                                                                   | Example                                                |
|:-----:|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------|
| `AND` | Returns `TRUE` if the operand expressions evaluate to `TRUE`; otherwise `FALSE`.<br>If an operand is `MISSING` and the other is `TRUE` returns `MISSING`, if the other operand is `FALSE` it returns `FALSE`.<br>If an operand is `NULL` and the other is `TRUE` returns `NULL`, if the other operand is `FALSE` it returns `FALSE`.                                                          | `WHERE city = "San Francisco" AND status = true`       |
| `OR`  | Returns `TRUE` if one of the operand expressions is evaluated to `TRUE`; otherwise returns `FALSE`.<br>If an operand is `MISSING`, the operation will result in `MISSING` if the other operand is `FALSE` or `TRUE` if the other operand is `TRUE`.<br>If an operand is `NULL`, the operation will result in `NULL` if the other operand is `FALSE` or `TRUE` if the other operand is `TRUE`. | `WHERE city = “San Francisco” OR city = "Santa Clara"` |

**Table 6. Logical Operation Table**

|           a           |     b     |   a AND b   |   a OR b    |
|:---------------------:|:---------:|:-----------:|:-----------:|
|  TRUE { rowspan=4 }   |   TRUE    |    TRUE     |    TRUE     |
                        |   FALSE   |    FALSE    |    TRUE     |
                        |   NULL    |  FALSE ⁵⁻¹  |    TRUE     |
                        |  MISSING  |   MISSING   |    TRUE     |
|  FALSE { rowspan=4 }  |   TRUE    |    FALSE    |    TRUE     |
                        |   FALSE   |    FALSE    |    FALSE    |
                        |   NULL    |    FALSE    |  FALSE ⁵⁻¹  |
                        |  MISSING  |    FALSE    |   MISSING   |
|  NULL { rowspan=4 }   |   TRUE    |  FALSE ⁵⁻¹  |    TRUE     |
                        |   FALSE   |    FALSE    |  FALSE ⁵⁻¹  |
                        |   NULL    |  FALSE ⁵⁻¹  |  FALSE ⁵⁻¹  |
                        |  MISSING  |  FALSE ⁵⁻²  | MISSING ⁵⁻³ |
| MISSING { rowspan=4 } |   TRUE    |   MISSING   |    TRUE     |
                        |   FALSE   |    FALSE    |   MISSING   |
                        |   NULL    |  FALSE ⁵⁻²  | MISSING ⁵⁻³ |
                        |  MISSING  |   MISSING   |   MISSING   |

!!! note

    This differs from Server SQL++ in the following instances:  
    ⁵⁻¹ Server will return: NULL instead of FALSE  
    ⁵⁻² Server will return: MISSING instead of FALSE  
    ⁵⁻³ Server will return: NULL instead of MISSING

#### String Operator

##### Purpose

A single string operator is provided. It enables string concatenation.

**Table 7. String Operators**

|        Op         | Description   | Example                                                     |
|:-----------------:|:--------------|:------------------------------------------------------------|
| <code>\|\|</code> | Concatenating | <code>SELECT firstnm \|\| lastnm AS fullname FROM db</code> |

### Unary Operators

#### Purpose

Three unary operators are provided. They operate by modifying an expression, making it numerically positive or negative,
or by logically negating its value (`TRUE` becomes `FALSE`).

#### Syntax

```sql
// UNARY_OP _ expr
```

**Table 8. Unary Operators**

|  Op   | Description                          | Example                             |
|:-----:|:-------------------------------------|:------------------------------------|
|  `+`  | Positive value                       | `WHERE v1 = +10`                    |
|  `-`  | Negative value                       | `WHERE v1 = -10`                    |
| `NOT` | Logical Negate operator <sup>*</sup> | `WHERE "James" NOT IN contactsList` |

<sup>*</sup> The `NOT` operator is often used in conjunction with operators such as `IN`, `LIKE`, `MATCH`, and `BETWEEN`
operators.  
`NOT` operation on `NULL` value returns `NULL`.  
`NOT` operation on `MISSING` value returns `MISSING`.

**Table 9. NOT Operation TABLE**

|    a    |  NOT a  |
|:-------:|:-------:|
|  TRUE   |  FALSE  |
|  FALSE  |  TRUE   |
|  NULL   |  FALSE  |
| MISSING | MISSING |

### COLLATE Operators

#### Purpose

Collate operators specify how the string comparison is conducted.

#### Usage

The collate operator is used in conjunction with string comparison expressions and `ORDER BY` clauses. It allows for one
or more collations.

If multiple collations are used, the collations need to be specified in a parenthesis. When only one collation is used,
the parenthesis is optional.

!!! note

    Collate is not supported by Server SQL++

#### Syntax

```sql
collate = COLLATE collation | '(' collation (_ collation )* ')'

collation = NO? (UNICODE | CASE | DIACRITICS) WB
```

#### Arguments

The available collation options are:

* `UNICODE`: Conduct a Unicode comparison; the default is to do ASCII comparison.
* `CASE`: Conduct case-sensitive comparison.
* `DIACRITIC`: Take account of accents and diacritics in the comparison; on by default.
* `NO`: This can be used as a prefix to the other collations, to disable them (for example: `NOCASE` to enable
  case-insensitive comparison)

#### Example

```sql
SELECT department FROM db WHERE (name = "fred") COLLATE UNICODE
```

```sql
SELECT department FROM db WHERE (name = "fred")
COLLATE (UNICODE)
```

```sql
SELECT department FROM db WHERE (name = "fred") COLLATE (UNICODE CASE)
```

```sql
SELECT name FROM db ORDER BY name COLLATE (UNICODE DIACRITIC)
```

### CONDITIONAL Operator

#### Purpose

The Conditional (or `CASE`) operator evaluates conditional logic in a similar way to the `IF`/`ELSE` operator.

#### Syntax

```sql
CASE (expression) (WHEN expression THEN expression)+ (ELSE expression)? END

CASE (expression)? (!WHEN expression)?
  (WHEN expression THEN expression)+ (ELSE expression)? END
```

Both _Simple Case_ and _Searched Case_ expressions are supported. The syntactic difference being that the _Simple Case_
expression has an expression after the `CASE` keyword.

1. Simple Case Expression
    * If the `CASE` expression is equal to the first `WHEN` expression, the result is the `THEN` expression.
    * Otherwise, any subsequent `WHEN` clauses are evaluated in the same way.
    * If no match is found, the result of the `CASE` expression is the `ELSE` expression, `NULL` if no `ELSE` expression
      was provided.
2. Searched Case Expression
    * If the first `WHEN` expression is `TRUE`, the result of this expression is its `THEN` expression.
    * Otherwise, subsequent `WHEN` clauses are evaluated in the same way. If no `WHEN` clause evaluate to `TRUE`, then
      the result of the expression is the `ELSE` expression, or `NULL` if no `ELSE` expression was provided.

#### Example

!!! example "Example 16. Simple Case"

    ```sql
    SELECT CASE state WHEN ‘CA’ THEN ‘Local’ ELSE ‘Non-Local’ END FROM DB
    ```

!!! example "Example 17. Searched Case"

    ```sql
    SELECT CASE WHEN shippedOn IS NOT NULL THEN ‘SHIPPED’ ELSE "NOT-SHIPPED" END FROM db
    ```

## Functions

**In this section**  
[Aggregation Functions](#aggregation-functions) | [Array Functions](#array-functions) | [Conditional
Functions](#conditional-functions) | [Date and Time Functions](#date-and-time-functions) | [Full Text Search
Functions](#full-text-search-functions) | [Maths Functions](#maths-functions) | [Metadata
Functions](#metadata-functions) | [Pattern Searching Functions](#pattern-searching-functinos) | [String
Functions](#string-functions) | [Type Checking Functions](#type-checking-functions) | [Type Conversion
Functions](#type-conversion-functions)

### Purpose

Functions are also expressions.

### Syntax

The function syntax is the same as Java’s method syntax. It starts with the function name, followed by optional
arguments inside parentheses.

```sql
function = functionName parenExprs

functionName  = IDENTIFIER

parenExprs = '(' ( expression (_ ',' _ expression )* )? ')'
```

### Aggregation Functions

**Table 10. Aggregation Functions**

| Function      | Description                                             |
|:--------------|:--------------------------------------------------------|
| `AVG(expr)`   | Returns average value of the number values in the group |
| `COUNT(expr)` | Returns a count of all values in the group              |
| `MIN(expr)`   | Returns the minimum value in the group                  |
| `MAX(expr)`   | Returns the maximum value in the group                  |
| `SUM(expr)`   | Returns the sum of all number values in the group       |

### Array Functions

**Table 11. Array Functions**

| Function               | Description                                                                                      |
|:-----------------------|:-------------------------------------------------------------------------------------------------|
| `ARRAY_AGG(expr)`      | Returns an array of the non-MISSING group values in the input expression, including NULL values. |
| `ARRAY_AVG(expr)`      | Returns the average of all non-NULL number values in the array; or NULL if there are none        |
| `ARRAY_CONTAINS(expr)` | Returns TRUE if the value exists in the array; otherwise FALSE                                   |
| `ARRAY_COUNT(expr)`    | Returns the number of non-null values in the array                                               |
| `ARRAY_IFNULL(expr)`   | Returns the first non-null value in the array                                                    |
| `ARRAY_MAX(expr)`      | Returns the largest non-NULL, non_MISSING value in the array                                     |
| `ARRAY_MIN(expr)`      | Returns the smallest non-NULL, non_MISSING value in the array                                    |
| `ARRAY_LENGTH(expr)`   | Returns the length of the array                                                                  |
| `ARRAY_SUM(expr)`      | Returns the sum of all non-NULL numeric value in the array                                       |

### Conditional Functions

**Table 12. Conditional Functions**

| <div style="min-width:237px">Function</div> | Description                                                                                                                                                                         |
|:--------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `IFMISSING(expr1, expr2, …)`                | Returns the first non-MISSING value, or NULL if all values are MISSING                                                                                                              |
| `IFMISSINGRONULL(expr1, expr2, …)`          | Returns the first non-NULL and non-MISSING value, or NULL if all values are NULL or MISSING                                                                                         |
| `IFNULL(expr1, expr2, …)`                   | Returns the first non-NULL, or NULL if all values are NULL                                                                                                                          |
| `MISSINGIF(expr1, expr2)`                   | Returns MISSING when expr1 = expr2; otherwise returns expr1.<br>Returns MISSING if either or both expressions are MISSING.<br>Returns NULL if either or both expressions are NULL.+ |
| `NULLF(expr1, expr2)`                       | Returns NULL when expr1 = expr2; otherwise returns expr1.<br>Returns MISSING if either or both expressions are MISSING.<br>Returns NULL if either or both expressions are NULL.+    |

### Date and Time Functions

**Table 13. Date and Time Functions**

| <div style="min-width:144px">Function</div> | Description                                                                                                                       |
|:--------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------|
| `STR_TO_MILLIS(expr)`                       | Returns the number of milliseconds since the unix epoch of the given ISO 8601 date input string.                                  |
| `STR_TO_UTC(expr)`                          | Returns the ISO 8601 UTC date time string of the given ISO 8601 date input string.                                                |
| `MILLIS_TO_STR(expr)`                       | Returns a ISO 8601 date time string in device local timezone of the given number of milliseconds since the unix epoch expression. |
| `MILLIS_TO_UTC(expr)`                       | Returns the UTC ISO 8601 date time string of the given number of milliseconds since the unix epoch expression.                    |

### Full Text Search Functions

**Table 14. FTS Functions**

| <div style="min-width:165px">Function</div> | Description                                                                                                                                                                   | Example                                                             |
|:--------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------|
| `MATCH(indexName, term)`                    | Returns `TRUE` if `term` expression matches the FTS indexed term. `indexName` identifies the FTS index, `term` expression to search for matching.                             | `WHERE MATCH (description, “couchbase”)`                            |
| `RANK(indexName)`                           | Returns a numeric value indicating how well the current query result matches the full-text query when performing the `MATCH`. `indexName` is an IDENTIFIER for the FTS index. | `WHERE MATCH (description, “couchbase”) ORDER BY RANK(description)` |

### Maths Functions

**Table 15. Maths Functions**

| Function                            | Description                                                                                                                                                                                                                                                                                                                                                             |
|:------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ABS(expr)`                         | Returns the absolute value of a number.                                                                                                                                                                                                                                                                                                                                 |
| `ACOS(expr)`                        | Returns the arc cosine in radians.                                                                                                                                                                                                                                                                                                                                      |
| `ASIN(expr)`                        | Returns the arcsine in radians.                                                                                                                                                                                                                                                                                                                                         |
| `ATAN(expr)`                        | Returns the arctangent in radians.                                                                                                                                                                                                                                                                                                                                      |
| `ATAN2(expr1,expr2)`                | Returns the arctangent of expr1/expr2.                                                                                                                                                                                                                                                                                                                                  |
| `CEIL(expr)`                        | Returns the smallest integer not less than the number.                                                                                                                                                                                                                                                                                                                  |
| `COS(expr)`                         | Returns the cosine value of the expression.                                                                                                                                                                                                                                                                                                                             |
| `DIV(expr1, expr2)`                 | Returns float division of expr1 and expr2.<br>Both expr1 and expr2 are cast to a double number before division.<br>The returned result is always a double.                                                                                                                                                                                                              |
| `DEGREES(expr)`                     | Converts radians to degrees.                                                                                                                                                                                                                                                                                                                                            |
| `E()`                               | Returns base of natural logarithms.                                                                                                                                                                                                                                                                                                                                     |
| `EXP(expr)`                         | Returns expr value                                                                                                                                                                                                                                                                                                                                                      |
| `FLOOR(expr)`                       | Returns largest integer not greater than the number.                                                                                                                                                                                                                                                                                                                    |
| `IDIV(expr1, expr2)`                | Returns integer division of expr1 and expr2.                                                                                                                                                                                                                                                                                                                            |
| `LN(expr)`                          | Returns log base e value.                                                                                                                                                                                                                                                                                                                                               |
| `LOG(expr)`                         | Returns log base 10 value.                                                                                                                                                                                                                                                                                                                                              |
| `PI()`                              | Return PI value.                                                                                                                                                                                                                                                                                                                                                        |
| `POWER(expr1, expr2)`               | Returns expr1expr2 value.                                                                                                                                                                                                                                                                                                                                               |
| `RADIANS(expr)`                     | Returns degrees to radians.                                                                                                                                                                                                                                                                                                                                             |
| `ROUND(expr (, digits_expr)?)`      | Returns the rounded value to the given number of integer digits to the right of the decimal point (left if digits is negative). Digits are 0 if not given.<br>The function uses _Rounding Away From Zero_ convention to round midpoint values to the next number away from zero (so, for example, `ROUND(1.75)` returns 1.8 but `ROUND(1.85)` returns 1.9. <sup>*</sup> |
| `ROUND_EVEN(expr (, digits_expr)?)` | Returns rounded value to the given number of integer digits to the right of the decimal point (left if digits is negative). Digits are 0 if not given.<br>The function uses _Rounding to Nearest Even_ (Banker’s Rounding) convention which rounds midpoint values to the nearest even number (for example, both `ROUND_EVEN(1.75)` and `ROUND_EVEN(1.85)` return 1.8). |
| `SIGN(expr)`                        | Returns -1 for negative, 0 for zero, and 1 for positive numbers.                                                                                                                                                                                                                                                                                                        |
| `SIN(expr)`                         | Returns sine value.                                                                                                                                                                                                                                                                                                                                                     |
| `SQRT(expr)`                        | Returns square root value.                                                                                                                                                                                                                                                                                                                                              |
| `TAN(expr)`                         | Returns tangent value.                                                                                                                                                                                                                                                                                                                                                  |
| `TRUNC (expr (, digits, expr)?)`    | Returns a truncated number to the given number of integer digits to the right of the decimal point (left if digits is negative). Digits are 0 if not given.                                                                                                                                                                                                             |

<sup>*</sup> The behavior of the `ROUND()` function is different from Server SQL++ `ROUND()`, which rounds the midpoint
values using _Rounding to Nearest Even_ convention.

### Metadata Functions

**Table 16. Metadata Functions**

| Function                | Description                                                                                                                                                                                                                                                                                                                                                                                                                                               | Example                                                                                                                                                                                                                                        |
|:------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `META(dataSourceName?)` | Returns a dictionary containing metadata properties including:<br><ul><li>id : document identifier</li<li>sequence : document mutating sequence number</li><li>deleted : flag indicating whether document is deleted or not</li><li>expiration : document expiration date in timestamp format<br>The optional dataSourceName identifies the database or the database alias name.</li></ul>To access a specific metadata property, use the dot expression. | `SELECT META() FROM db`<br>`SELECT META().id, META().sequence, META().deleted, META().expiration FROM db`<br>`SELECT p.name, r.rating FROM product as p INNER JOIN reviews AS r ON META(r).id IN p.reviewList WHERE META(p).id = "product320"` |

### Pattern Searching Functions

**Table 17. Pattern Searching Functions**

| Function                                    | Description                                                                                                                                                                            |
|:--------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `REGEXP_CONTAINS(expr, pattern)`            | Returns TRUE if the string value contains any sequence that matches the regular expression pattern.                                                                                    |
| `REGEXP_LIKE(expr, pattern)`                | Return TRUE if the string value exactly matches the regular expression pattern.                                                                                                        |
| `REGEXP_POSITION(expr, pattern)`            | Returns the first position of the occurrence of the regular expression pattern within the input string expression. Return -1 if no match is found. Position counting starts from zero. |
| `REGEXP_REPLACE(expr, pattern, repl [, n])` | Returns new string with occurrences of pattern replaced with repl. If n is given, at the most n replacements are performed. If n is not given, all matching occurrences are replaced.  |

### String Functions

**Table 18. String Functions**

| Function                         | Description                                                                                          |
|:---------------------------------|:-----------------------------------------------------------------------------------------------------|
| `CONTAINS(expr, substring_expr)` | Returns true if the substring exists within the input string, otherwise returns false.               |
| `LENGTH(expr)`                   | Returns the length of a string. The length is defined as the number of characters within the string. |
| `LOWER(expr)`                    | Returns the lowercase string of the input string.                                                    |
| `LTRIM(expr)`                    | Returns the string with all leading whitespace characters removed.                                   |
| `RTRIM(expr)`                    | Returns the string with all trailing whitespace characters removed.                                  |
| `TRIM(expr)`                     | Returns the string with all leading and trailing whitespace characters removed.                      |
| `UPPER(expr)`                    | Returns the uppercase string of the input string.                                                    |

### Type Checking Functions

**Table 19. Type Checking Functions**

| Function          | Description                                                                                                                                                                                                             |
|:------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ISARRAY(expr)`   | Returns TRUE if expression is an array, otherwise returns MISSING, NULL or FALSE.                                                                                                                                       |
| `ISATOM(expr)`    | Returns TRUE if expression is a Boolean, number, or string, otherwise returns MISSING, NULL or FALSE.                                                                                                                   |
| `ISBOOLEAN(expr)` | Returns TRUE if expression is a Boolean, otherwise returns MISSING, NULL or FALSE.                                                                                                                                      |
| `ISNUMBER(expr)`  | Returns TRUE if expression is a number, otherwise returns MISSING, NULL or FALSE.                                                                                                                                       |
| `ISOBJECT(expr)`  | Returns TRUE if expression is an object (dictionary), otherwise returns MISSING, NULL or FALSE.                                                                                                                         |
| `ISSTRING(expr)`  | Returns TRUE if expression is a string, otherwise returns MISSING, NULL or FALSE.                                                                                                                                       |
| `TYPE(expr)`      | Returns one of the following strings, based on the value of expression:<ul><li>“missing”</li><li>“null”</li><li>“boolean”</li><li>“number”</li><li>“string”</li><li>“array”</li><li>“object”</li><li>“binary”</li></ul> |

### Type Conversion Functions

**Table 20. Type Conversion Functions**

| Function          | Description                                                                                                                                                                                                                                                                                                      |
|:------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `TOARRAY(expr)`   | Returns MISSING if the value is MISSING.<br>Returns NULL if the value is NULL.<br>Returns the array itself.<br>Returns all other values wrapped in an array.                                                                                                                                                     |
| `TOATOM(expr)`    | Returns MISSING if the value is MISSING.<br>Returns NULL if the value is NULL.<br>Returns an array of a single item if the value is an array.<br>Returns an object of a single key/value pair if the value is an object.<br>Returns boolean, numbers, or strings<br>Returns NULL for all other values.           |
| `TOBOOLEAN(expr)` | Returns MISSING if the value is MISSING.<br>Returns NULL if the value is NULL.<br>Returns FALSE if the value is FALSE.<br>Returns FALSE if the value is 0 or NaN.<br>Returns FALSE if the value is an empty string, array, and object.<br>Return TRUE for all other values.                                      |
| `TONUMBER(expr)`  | Returns MISSING if the value is MISSING.<br>Returns NULL if the value is NULL.<br>Returns 0 if the value is FALSE.<br>Returns 1 if the value is TRUE.<br>Returns NUMBER if the value is NUMBER.<br>Returns NUMBER parsed from the string value.<br>Returns NULL for all other values.                            |
| `TOOBJECT(expr)`  | Returns MISSING if the value is MISSING.<br>Returns NULL if the value is NULL.<br>Returns the object if the value is an object.<br>Returns an empty object for all other values.                                                                                                                                 |
| `TOSTRING(expr)`  | Returns MISSING if the value is MISSING.<br>Returns NULL if the value is NULL.<br>Returns “false” if the value is FALSE.<br>Returns “true” if the value is TRUE.<br>Returns NUMBER in String if the value is NUMBER.<br>Returns the string value if the value is a string.<br>Returns NULL for all other values. |

## QueryBuilder Differences

Couchbase Lite SQL++ Query supports all `QueryBuilder` features, except _Predictive Query_ and _Index_. See [Table
21](#table-21) for the features supported by SQL++ but not by `QueryBuilder`.

<span id='table-21'>**Table 21. QueryBuilder Differences**</span>

| Category                   | Components                                                                                                 |
|:---------------------------|:-----------------------------------------------------------------------------------------------------------|
| Conditional Operator       | `CASE(WHEN … THEN … ELSE ..)`                                                                              |
| Array Functions            | `ARRAY_AGG`<br>`ARRAY_AVG`<br>`ARRAY_COUNT`<br>`ARRAY_IFNULL`<br>`ARRAY_MAX`<br>`ARRAY_MIN`<br>`ARRAY_SUM` |
| Conditional Functions      | `IFMISSING`<br>`IFMISSINGORNULL`<br>`IFNULL`<br>`MISSINGIF`<br>`NULLIF`                                    |
| Math Functions             | `DIV`<br>`IDIV`<br>`ROUND_EVEN`                                                                            |
| Pattern Matching Functions | `REGEXP_CONTAINS`<br>`REGEXP_LIKE`<br>`REGEXP_POSITION`<br>`REGEXP_REPLACE`                                |
| Type Checking Functions    | `ISARRAY`<br>`ISATOM`<br>`ISBOOLEAN`<br>`ISNUMBER`<br>`ISOBJECT`<br>`ISSTRING TYPE`                        |
| Type Conversion Functions  | `TOARRAY`<br>`TOATOM`<br>`TOBOOLEAN`<br>`TONUMBER`<br>`TOOBJECT`<br>`TOSTRING`                             |

## Query Parameters

You can provide runtime parameters to your SQL++ query to make it more flexible.

To specify substitutable parameters within your query string prefix the name with `$`, `$type` — see [Example
18](#example-18).

!!! example "<span id='example-18'>Example 18. Running a SQL++ Query</span>"

    ```kotlin
    val query = database.createQuery(
        "SELECT META().id AS id FROM _ WHERE type = \$type"
    ) 
    
    query.parameters = Parameters().setString("type", "hotel") 
    
    return query.execute().allResults()
    ```

    1. Define a parameter placeholder `$type`
    2. Set the value of the `$type` parameter
