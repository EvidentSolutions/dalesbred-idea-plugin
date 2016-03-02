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

internal class SqlReader(private val sql: String) {

    private var index = 0

    fun skipSpaces() {
        while (hasMore() && sql[index].isWhitespace())
            index++
    }

    fun skipBalancedParens() {
        var insideQuote = false
        var parenNesting = 0

        while (hasMore()) {
            when (readChar()) {
                '(' ->
                    if (!insideQuote)
                        parenNesting++
                ')' ->
                    if (!insideQuote) {
                        parenNesting--

                        if (parenNesting < 0)
                            throw SqlSyntaxException()

                        if (parenNesting == 0)
                            return
                    }
                '\'' ->
                    insideQuote = !insideQuote
            }
        }
    }

    fun hasMore() = index < sql.length

    fun rest() = sql.substring(index)

    fun readChar() = sql[index++]

    fun lookingAt(s: String) = sql.regionMatches(index, s, 0, s.length, ignoreCase = true)

    fun skipIfLookingAt(s: String) =
        if (lookingAt(s)) {
            index += s.length
            true
        } else {
            false
        }

    fun skipIfLookingAtKeyword(s: String) =
        if (lookingAt(s)) {
            val tokenLength = s.length

            // First verify that our token does not continue
            if (index + tokenLength < sql.length && sql[index + tokenLength].isLetterOrDigit())
                false
            else {
                index += tokenLength
                skipSpaces()
                true
            }
        } else {
            false
        }

    fun skipName() {
        while (hasMore() && sql[index].isLetterOrDigit())
            index++
    }

    fun skipUntil(c: Char) {
        while (hasMore() && sql[index++] != c) {
            // ignore
        }
    }

    fun skipUntil(s: String) {
        while (hasMore() && !lookingAt(s))
            index++
    }

    fun expectKeyword(s: String) {
        if (!skipIfLookingAtKeyword(s))
            throw SqlSyntaxException()
    }
}
