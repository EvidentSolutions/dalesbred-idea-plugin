/*
 * Copyright (c) 2013-2015 Evident Solutions Oy
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

public final class SqlUtils {

    private static final Pattern SELECT_ITEM_PATTERN = Pattern.compile("(.+\\.)?(.+?)(\\s+(as\\s+)?(\\S+))?", CASE_INSENSITIVE);

    enum SelectListParseState {INITIAL, QUOTED_SINGLE, QUOTED_DOUBLE}

    private SqlUtils() {
    }

    @NotNull
    public static List<String> selectVariables(@NotNull String sql) {
        try {
            return selectList(sql.replace('\r', ' ').replace('\n', ' '));

        } catch (SqlSyntaxException ignored) {
            return Collections.emptyList();
        }
    }

    @NotNull
    private static List<String> selectList(@NotNull String sql) throws SqlSyntaxException {
        SqlReader reader = new SqlReader(sql);
        stripCommonTableExpression(reader);

        if (reader.skipIfLookingAtKeyword("select")) {
            if (reader.skipIfLookingAtKeyword("all")) {
                // nada
            } else if (reader.skipIfLookingAtKeyword("distinct")) {
                if (reader.skipIfLookingAtKeyword("on")) {
                    reader.skipBalancedParens();
                    reader.skipSpaces();
                }
            }

            return parseSelectList(reader);

        } else {
            while (reader.hasMore()) {
                reader.skipUntil("returning");
                if (reader.skipIfLookingAtKeyword("returning"))
                    return parseSelectList(reader);
            }
        }

        throw new SqlSyntaxException();
    }

    @NotNull
    private static List<String> parseSelectList(@NotNull SqlReader reader) throws SqlSyntaxException {
        SelectListParseState state = SelectListParseState.INITIAL;
        List<String> result = new ArrayList<String>();
        int parenNesting = 0;
        int bracketNesting = 0;

        StringBuilder current = new StringBuilder();

        while (reader.hasMore()) {
            if (reader.skipIfLookingAtKeyword("from") && parenNesting == 0 && bracketNesting == 0)
                break;

            char ch = reader.readChar();
            current.append(ch);

            switch (state) {
                case INITIAL:
                    switch (ch) {
                        case ',':
                            if (parenNesting == 0 && bracketNesting == 0) {
                                current.setLength(current.length() - 1); // remove trailing comma
                                result.add(parseSelectItem(current.toString().trim()));
                                current.setLength(0);
                            }
                            break;
                        case '(':
                            parenNesting++;
                            break;
                        case ')':
                            if (parenNesting == 0)
                                throw new SqlSyntaxException();
                            parenNesting--;
                            break;
                        case '[':
                            bracketNesting++;
                            break;
                        case ']':
                            if (bracketNesting == 0)
                                throw new SqlSyntaxException();
                            bracketNesting--;
                            break;
                        case '\'':
                            state = SelectListParseState.QUOTED_SINGLE;
                            break;
                        case '"':
                            state = SelectListParseState.QUOTED_DOUBLE;
                            break;
                    }
                    break;
                case QUOTED_SINGLE:
                    if (ch == '\'')
                        state = SelectListParseState.INITIAL;
                    break;
                case QUOTED_DOUBLE:
                    if (ch == '"')
                        state = SelectListParseState.INITIAL;
                    break;
            }
        }

        if (current.length() != 0)
            result.add(parseSelectItem(current.toString().trim()));

        return result;
    }

    @NotNull
    private static String parseSelectItem(@NotNull String selectItem) throws SqlSyntaxException {
        Matcher matcher = SELECT_ITEM_PATTERN.matcher(selectItem);
        if (!matcher.matches())
            throw new SqlSyntaxException();

        if (matcher.group(5) != null)
            return normalizeAlias(matcher.group(5));

        return normalizeAlias(matcher.group(2));
    }

    static void stripCommonTableExpression(@NotNull SqlReader reader) throws SqlSyntaxException {
        reader.skipSpaces();

        if (!reader.skipIfLookingAtKeyword("with"))
            return;

        reader.skipIfLookingAtKeyword("recursive");

        skipWithItem(reader);

        while (reader.skipIfLookingAt(","))
            skipWithItem(reader);

        reader.skipSpaces();
    }

    private static void skipWithItem(@NotNull SqlReader reader) throws SqlSyntaxException {
        reader.skipSpaces();
        reader.skipName();
        reader.skipSpaces();

        if (reader.lookingAt("(")) {
            reader.skipBalancedParens();
            reader.skipSpaces();
        }

        reader.expectKeyword("as");

        reader.skipBalancedParens();
    }

    @NotNull
    private static String normalizeAlias(@NotNull String alias) {
        if ((alias.startsWith("\"") && alias.endsWith("\"")) || (alias.startsWith("[") && alias.endsWith("]")))
            return alias.substring(1, alias.length() - 1);
        else
            return alias;
    }

    public static int countQueryParametersPlaceholders(@NotNull String sql) {
        int count = 0;

        SqlReader reader = new SqlReader(sql);

        while (reader.hasMore()) {
            char ch = reader.readChar();

            if (ch == '?') {
                count++;
            } else if (ch == '\'') {
                reader.skipUntil('\'');
            } else if (reader.lookingAt("--")) {
                reader.skipUntil('\n');
            } else if (reader.lookingAt("/*")) {
                reader.skipUntil("*/");
            }
        }

        return count;
    }
}
