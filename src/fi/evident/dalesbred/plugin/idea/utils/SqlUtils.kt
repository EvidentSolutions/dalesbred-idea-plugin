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

import java.util.*

private val SELECT_ITEM_PATTERN = Regex("(.+\\.)?(.+?)(\\s+(as\\s+)?(\\S+))?", RegexOption.IGNORE_CASE)

internal enum class SelectListParseState {
    INITIAL, QUOTED_SINGLE, QUOTED_DOUBLE
}

fun selectVariables(sql: String): List<String> =
    try {
        selectList(sql.replace('\r', ' ').replace('\n', ' '))

    } catch (ignored: SqlSyntaxException) {
        emptyList()
    }

private fun selectList(sql: String): List<String> {
    val reader = SqlReader(sql)
    stripCommonTableExpression(reader)

    if (reader.skipIfLookingAtKeyword("select")) {
        if (reader.skipIfLookingAtKeyword("all")) {
            // nada
        } else if (reader.skipIfLookingAtKeyword("distinct")) {
            if (reader.skipIfLookingAtKeyword("on")) {
                reader.skipBalancedParens()
                reader.skipSpaces()
            }
        }

        return parseSelectList(reader)

    } else {
        while (reader.hasMore()) {
            reader.skipUntil("returning")
            if (reader.skipIfLookingAtKeyword("returning"))
                return parseSelectList(reader)
        }
    }

    throw SqlSyntaxException()
}

private fun parseSelectList(reader: SqlReader): List<String> {
    var state = SelectListParseState.INITIAL
    val result = ArrayList<String>()
    var parenNesting = 0
    var bracketNesting = 0

    val current = StringBuilder()

    while (reader.hasMore()) {
        if (reader.skipIfLookingAtKeyword("from") && parenNesting == 0 && bracketNesting == 0)
            break

        val ch = reader.readChar()
        current.append(ch)

        when (state) {
            SelectListParseState.INITIAL -> when (ch) {
                ',' -> if (parenNesting == 0 && bracketNesting == 0) {
                    current.setLength(current.length - 1) // remove trailing comma
                    result.add(parseSelectItem(current.toString().trim()))
                    current.setLength(0)
                }
                '(' -> parenNesting++
                ')' -> {
                    if (parenNesting == 0)
                        throw SqlSyntaxException()
                    parenNesting--
                }
                '[' -> bracketNesting++
                ']' -> {
                    if (bracketNesting == 0)
                        throw SqlSyntaxException()
                    bracketNesting--
                }
                '\'' -> state = SelectListParseState.QUOTED_SINGLE
                '"' -> state = SelectListParseState.QUOTED_DOUBLE
            }
            SelectListParseState.QUOTED_SINGLE -> if (ch == '\'')
                state = SelectListParseState.INITIAL
            SelectListParseState.QUOTED_DOUBLE -> if (ch == '"')
                state = SelectListParseState.INITIAL
        }
    }

    if (current.length != 0)
        result.add(parseSelectItem(current.toString().trim()))

    return result
}

private fun parseSelectItem(selectItem: String): String {
    val match = SELECT_ITEM_PATTERN.matchEntire(selectItem) ?: throw SqlSyntaxException()

    return if (match.groups[5] != null)
        normalizeAlias(match.groupValues[5])
    else
        normalizeAlias(match.groupValues[2])
}

internal fun stripCommonTableExpression(reader: SqlReader) {
    reader.skipSpaces()

    if (!reader.skipIfLookingAtKeyword("with"))
        return

    reader.skipIfLookingAtKeyword("recursive")

    skipWithItem(reader)

    while (reader.skipIfLookingAt(","))
        skipWithItem(reader)

    reader.skipSpaces()
}

private fun skipWithItem(reader: SqlReader) {
    reader.skipSpaces()
    reader.skipName()
    reader.skipSpaces()

    if (reader.lookingAt("(")) {
        reader.skipBalancedParens()
        reader.skipSpaces()
    }

    reader.expectKeyword("as")

    reader.skipBalancedParens()
}

fun normalizeAlias(alias: String): String {
    val al = if ((alias.startsWith("\"") && alias.endsWith("\"") && alias.length >= 2) || (alias.startsWith("[") && alias.endsWith("]")))
        alias.substring(1, alias.length - 1)
    else
        alias

    if (al.isEmpty() || al == "\"")
        throw SqlSyntaxException()

    return al
}

fun countQueryParametersPlaceholders(sql: String): Int {
    var count = 0

    val reader = SqlReader(sql)

    while (reader.hasMore()) {
        val ch = reader.readChar()

        when {
            ch == '?'               -> count++
            ch == '\''              -> reader.skipUntil('\'')
            reader.lookingAt("--")  -> reader.skipUntil('\n')
            reader.lookingAt("/*")  -> reader.skipUntil("*/")
        }
    }

    return count
}
