/*
 * Copyright (c) 2016 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.evident.dalesbred.plugin.idea.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class SqlUtilsTest {

    @Test
    fun countQueryPlaceHolders() {
        assertEquals(0, countQueryParametersPlaceholders("select * from foo where x=4"))
        assertEquals(1, countQueryParametersPlaceholders("select * from foo where x=?"))
        assertEquals(2, countQueryParametersPlaceholders("select * from foo where x=? and y=?"))
        assertEquals(3, countQueryParametersPlaceholders("select ? from foo where x=? and y=?"))
    }

    @Test
    fun questionMarksInsideLiteralsAreNotPlaceholders() {
        assertEquals(0, countQueryParametersPlaceholders("select * from foo where x='foo?'"))
        assertEquals(0, countQueryParametersPlaceholders("select * from foo where x='foo '' ?'"))
    }

    @Test
    fun countPlaceholdersWithComments() {
        assertEquals(1, countQueryParametersPlaceholders("select * -- comment with quote ' \n from foo t where x=?"))
        assertEquals(1, countQueryParametersPlaceholders("select * -- comment with placeholder ? \n from foo t where x=?"))
        assertEquals(1, countQueryParametersPlaceholders("select * /* comment with quote ' */ from foo t where x=?"))
        assertEquals(1, countQueryParametersPlaceholders("select * /* comment with placeholder ? */ from foo t where x=?"))
    }

    @Test
    fun parseSimpleSelectVariables() {
        assertEquals(variables("foo"), selectVariables("select foo from bar"))
        assertEquals(variables("foo", "bar", "baz"), selectVariables("SELECT foo, bar, baz FROM foobar"))
    }

    @Test
    fun parseQualifiedSelectVariables() {
        assertEquals(variables("foo"), selectVariables("select b.foo from bar b"))
        assertEquals(variables("foo", "bar"), selectVariables("select x.foo, y.bar from bar x, baz y"))
    }

    @Test
    fun parseSelectVariablesWithDuplicateNames() {
        assertEquals(variables("foo", "foo"), selectVariables("select x.foo, y.foo from bar x, baz y"))
    }

    @Test
    fun parseSelectVariablesAliased() {
        assertEquals(variables("f", "b"), selectVariables("select foo as f, bar as b from bar"))
        assertEquals(variables("f", "b"), selectVariables("select foo f, bar b from bar"))
        assertEquals(variables("xFoo", "yFoo"), selectVariables("select x.foo as xFoo, y.foo as yFoo from bar x, baz y"))
    }

    @Test
    fun parseSelectConstants() {
        assertEquals(variables("42", "'foo'"), selectVariables("select 42, 'foo' from bar"))
        assertEquals(variables("x", "y"), selectVariables("select 42 x, 'foo' y from bar"))
    }

    @Test
    fun parseSelectStar() {
        assertEquals(variables("*"), selectVariables("select * from bar"))
    }

    @Test
    fun selectVariablesIgnoresDistinct() {
        assertEquals(variables("x", "y", "z"), selectVariables("select distinct x, y, z from bar"))
        assertEquals(variables("x", "y", "z"), selectVariables("select distinct on (foo, bar) x, y, z from bar"))
    }

    @Test
    fun selectVariablesIgnoresAll() {
        assertEquals(variables("x", "y", "z"), selectVariables("select all x, y, z from foo"))
    }

    @Test
    fun insertReturning() {
        assertEquals(variables("x", "y", "z"), selectVariables("insert into foo (bar) values (1) returning x, y, z"))
    }

    @Test
    fun updateReturning() {
        assertEquals(variables("x", "y", "z"), selectVariables("update foo set bar=1 returning x, y, z"))
    }

    @Test
    fun deleteReturning() {
        assertEquals(variables("x", "y", "z"), selectVariables("delete from foo returning x, y, z"))
    }

    @Test
    fun invalidQueriesReturnEmptyList() {
        assertEquals(variables(), selectVariables("select a,,b from foo"))
    }

    @Test
    fun parseSelectVariablesFromSelectWithoutFrom() {
        assertEquals(variables("foo", "bar"), selectVariables("select foo, bar"))
        assertEquals(variables("foo"), selectVariables("select nextval('my_schema.foo_id_seq') as foo"))
    }

    @Test
    fun numberOfColumnsInFunctionCalls() {
        assertEquals(1, selectVariables("select coalesce(x, y) from foo").size)
        assertEquals(1, selectVariables("select coalesce(max(x, z), min(y, z)) from foo").size)
    }

    @Test
    fun selectListWithCommasInsideLiterals() {
        assertEquals(3, selectVariables("select 'foo,bar', \"quux,xyzzy\", [foo,bar] from foo").size)
    }

    @Test
    fun malformedNesting() {
        assertEquals(variables(), selectVariables("select coalesce)x, y( from foo"))
    }

    @Test
    fun aliases() {
        assertEquals(variables("employee"), selectVariables("select employee_id is not null as employee"))
        assertEquals(variables("employee"), selectVariables("select employee_id is not null::bool as employee"))
    }

    @Test
    fun ansiQuotedColumnNames() {
        assertEquals(variables("foo", "bar"), selectVariables("select \"foo\", \"bar\" from quux"))
    }

    @Test
    fun sqlServerQuotedColumnNames() {
        assertEquals(variables("foo", "bar"), selectVariables("select [foo], [bar] from quux"))
    }

    @Test
    fun parseSelectVariablesWhenStringHasNewlines() {
        assertEquals(variables("foo", "bar", "baz"), selectVariables("\nSELECT\nfoo, bar,\nbaz \nFROM \nfoobar\n"))
    }

    @Test
    fun parseSelectVariablesWhenCTE() {
        assertEquals(variables("foo", "bar", "baz"), selectVariables("with temp as (select 42) select foo, bar, baz from foobar"))
    }

    @Test
    fun parseSelectVariablesWhenMultipleCTEs() {
        assertEquals(variables("foo", "bar", "baz"), selectVariables("with temp1 as (select 42), temp2 as (select 43) select foo, bar, baz from foobar"))
    }

    @Test
    fun parseSelectVariablesWhenCTEWithSubqueries() {
        assertEquals(variables("foo", "bar", "baz"), selectVariables("with temp1 as (select 1 from (select 2 from (select 3))) select foo, bar, baz from foobar"))
    }

    @Test
    fun parseSelectVariablesWhenCTEWithEscapedParentheses() {
        assertEquals(variables("foo", "bar", "baz"), selectVariables("with temp1 as (select '(') select foo, bar, baz from foobar"))
    }

    @Test
    fun parseSelectVariablesWhenCTEIsFromItem() {
        assertEquals(variables("foo", "bar", "baz"), selectVariables("with temp as (select 1 as foo, 2 as bar, 3 as baz) select foo, bar, baz from temp"))
    }

    @Test
    fun parseSelectVariablesWhenMultilineCTE() {
        assertEquals(variables("foo", "bar", "baz"), selectVariables("with \ntemp as (select \n42)\n select foo, bar, baz from foobar"))
    }

    @Test
    fun parseSelectVariablesWhenCTEWithColumnNames() {
        assertEquals(variables("foo", "bar", "baz"), selectVariables("with temp(foo, bar, quux) as (select 1, 2, 3) select foo, bar, baz from foobar"))
    }

    @Test
    fun subSelects() {
        assertEquals(variables("foo", "bar", "baz"), selectVariables("select foo, (select count(*) from a) as bar, array(select quux from b) as baz from foobar"))
    }

    @Test
    fun cteStripping() {
        assertEquals("", stripCTE(""))
        assertEquals("select * from foo", stripCTE("select * from foo"))
        assertEquals("select * from foo", stripCTE("with temp as (select * from bar) select * from foo"))
        assertEquals("select * from foo", stripCTE("with temp(baz) as (select * from bar) select * from foo"))
        assertEquals("select * from foo", stripCTE("with temp(baz,quux) as (select * from bar) select * from foo"))
        assertEquals("select * from foo", stripCTE("with temp(baz, quux) as (select * from bar) select * from foo"))
        assertEquals("select * from foo", stripCTE("with temp(baz, quux, xyzzy) as (select * from bar) select * from foo"))
    }

    private fun stripCTE(s: String): String {
        val reader = SqlReader(s)
        stripCommonTableExpression(reader)
        return reader.rest()
    }

    private fun variables(vararg variables: String) = variables.asList()
}
