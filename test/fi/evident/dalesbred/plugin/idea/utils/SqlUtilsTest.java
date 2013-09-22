/*
 * Copyright (c) 2013 Evident Solutions Oy
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

package fi.evident.dalesbred.plugin.idea.utils;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.List;

import static fi.evident.dalesbred.plugin.idea.utils.SqlUtils.countQueryParametersPlaceholders;
import static fi.evident.dalesbred.plugin.idea.utils.SqlUtils.selectVariables;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SqlUtilsTest {

    @Test
    public void countQueryPlaceHolders() {
       assertThat(countQueryParametersPlaceholders("select * from foo where x=4"), is(0));
       assertThat(countQueryParametersPlaceholders("select * from foo where x=?"), is(1));
       assertThat(countQueryParametersPlaceholders("select * from foo where x=? and y=?"), is(2));
       assertThat(countQueryParametersPlaceholders("select ? from foo where x=? and y=?"), is(3));
    }

    @Test
    public void questionMarksInsideLiteralsAreNotPlaceholders() {
        assertThat(countQueryParametersPlaceholders("select * from foo where x='foo?'"), is(0));
        assertThat(countQueryParametersPlaceholders("select * from foo where x='foo '' ?'"), is(0));
    }

    @Test
    public void parseSimpleSelectVariables() {
        assertThat(selectVariables("select foo from bar"), is(variables("foo")));
        assertThat(selectVariables("SELECT foo, bar, baz FROM foobar"), is(variables("foo", "bar", "baz")));
    }

    @Test
    public void parseQualifiedSelectVariables() {
        assertThat(selectVariables("select b.foo from bar b"), is(variables("foo")));
        assertThat(selectVariables("select x.foo, y.bar from bar x, baz y"), is(variables("foo", "bar")));
    }

    @Test
    public void parseSelectVariablesWithDuplicateNames() {
        assertThat(selectVariables("select x.foo, y.foo from bar x, baz y"), is(variables("foo", "foo")));
    }

    @Test
    public void parseSelectVariablesAliased() {
        assertThat(selectVariables("select foo as f, bar as b from bar"), is(variables("f", "b")));
        assertThat(selectVariables("select foo f, bar b from bar"), is(variables("f", "b")));
        assertThat(selectVariables("select x.foo as xFoo, y.foo as yFoo from bar x, baz y"), is(variables("xFoo", "yFoo")));
    }

    @Test
    public void parseSelectConstants() {
        assertThat(selectVariables("select 42, 'foo' from bar"), is(variables("42", "'foo'")));
        assertThat(selectVariables("select 42 x, 'foo' y from bar"), is(variables("x", "y")));
    }

    @Test
    public void parseSelectStar() {
        assertThat(selectVariables("select * from bar"), is(variables("*")));
    }

    @Test
    public void selectVariablesIgnoresDistinct() {
        assertThat(selectVariables("select distinct x, y, z from bar"), is(variables("x", "y", "z")));
        assertThat(selectVariables("select distinct on (foo, bar) x, y, z from bar"), is(variables("x", "y", "z")));
    }

    @Test
    public void selectVariablesIgnoresAll() {
        assertThat(selectVariables("select all x, y, z from foo"), is(variables("x", "y", "z")));
    }

    @Test
    public void insertReturning() {
        assertThat(selectVariables("insert into foo (bar) values (1) returning x, y, z"), is(variables("x", "y", "z")));
    }

    @Test
    public void updateReturning() {
        assertThat(selectVariables("update foo set bar=1 returning x, y, z"), is(variables("x", "y", "z")));
    }

    @Test
    public void deleteReturning() {
        assertThat(selectVariables("delete from foo returning x, y, z"), is(variables("x", "y", "z")));
    }

    @NotNull
    private static Matcher<List<String>> variables(@NotNull String... variables) {
        return is(asList(variables));
    }
}
