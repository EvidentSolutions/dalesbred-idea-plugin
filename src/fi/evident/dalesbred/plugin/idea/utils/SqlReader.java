/*
 * Copyright (c) 2015 Evident Solutions Oy
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

import org.jetbrains.annotations.NotNull;

final class SqlReader {

    @NotNull
    private final String sql;

    private int index;

    SqlReader(@NotNull String sql) {
        this.sql = sql;
    }

    void skipBalancedParens() throws SqlSyntaxException {
        boolean insideQuote = false;
        int parenNesting = 0;

        while (hasMore()) {
            switch (readChar()) {
                case '(':
                    if (!insideQuote)
                        parenNesting++;
                    break;
                case ')':
                    if (!insideQuote) {
                        parenNesting--;

                        if (parenNesting < 0)
                            throw new SqlSyntaxException();

                        if (parenNesting == 0)
                            return;
                    }
                    break;
                case '\'':
                    insideQuote = !insideQuote;
                    break;
            }
        }
    }

    public boolean hasMore() {
        return index < sql.length();
    }

    @NotNull
    public String rest() {
        return sql.substring(index);
    }

    public char readChar() {
        return sql.charAt(index++);
    }

    public void skipUntil(char c) {
        while (hasMore())
            if (readChar() == c)
                break;
    }
}
