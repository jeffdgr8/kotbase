package kotbase.ktx

import kotbase.BaseDbTest
import kotbase.DataSource
import kotbase.Expression
import kotbase.Function
import kotbase.Meta
import kotbase.QueryBuilder
import kotbase.SelectResult
import kotlin.test.Test
import kotlin.test.assertEquals

class QueryBuilderExtTest : BaseDbTest() {

    @Test
    fun select_expression_properties() {
        val expected = QueryBuilder.select(
            SelectResult.expression(Meta.id),
            SelectResult.property("foo"),
            SelectResult.property("bar")
        ).from(DataSource.database(baseTestDb))

        val actual = select(Meta.id, "foo", "bar") from baseTestDb

        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_expression_results() {
        val expected = QueryBuilder.select(
            SelectResult.expression(Meta.id),
            SelectResult.all()
        ).from(DataSource.database(baseTestDb))

        val actual = select(Meta.id, all()) from baseTestDb

        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_count() {
        val expected = QueryBuilder.select(
            SelectResult.expression(
                Function.count(Expression.string("*"))
            )
        ).from(DataSource.database(baseTestDb))

        val actual = selectCount() from baseTestDb

        assertEquals(expected.explain(), actual.explain())
    }
}
