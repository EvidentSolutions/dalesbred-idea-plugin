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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

public final class SqlUtils {

    private static final Pattern SELECT_LIST_PATTERN = Pattern.compile("\\s*select\\s+((all|distinct(\\s+on\\s*(\\(.*?\\)))?)\\s+)?(.+?)(\\s+from.+|\\s*)?", CASE_INSENSITIVE);
    private static final Pattern RETURNING_PATTERN = Pattern.compile(".+returning\\s+(.+)", CASE_INSENSITIVE);
    private static final Pattern SELECT_ITEM_PATTERN = Pattern.compile("(.+\\.)?(.+?)(\\s+(as\\s+)?(\\S+))?", CASE_INSENSITIVE);
    enum SelectListParseState { INITIAL, QUOTED_SINGLE, QUOTED_DOUBLE }

    private SqlUtils() {
    }

    @NotNull
    public static List<String> selectVariables(@NotNull String sql) {
        try {
            String selectList = selectList(sql.replace('\r', ' ').replace('\n', ' '));
            return parseSelectList(selectList);

        } catch (SqlSyntaxException ignored) {
            return Collections.emptyList();
        }
    }

    @NotNull
    private static List<String> parseSelectList(@NotNull String selectList) throws SqlSyntaxException {
        SelectListParseState state = SelectListParseState.INITIAL;
        List<String> result = new ArrayList<String>();
        int currentStart = 0;
        int parenNesting = 0;
        int bracketNesting = 0;

        for (int i = 0, len = selectList.length(); i < len; i++) {
            char ch = selectList.charAt(i);

            switch (state) {
                case INITIAL:
                    switch (ch) {
                        case ',':
                            if (parenNesting == 0 && bracketNesting == 0) {
                                result.add(parseSelectItem(selectList.substring(currentStart, i).trim()));
                                currentStart = i + 1;
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

        if (currentStart < selectList.length())
            result.add(parseSelectItem(selectList.substring(currentStart).trim()));

        return result;
    }

    @NotNull
    private static String selectList(@NotNull String sql) throws SqlSyntaxException {
        Matcher m1 = SELECT_LIST_PATTERN.matcher(sql);
        if (m1.matches())
            return m1.group(5);

        Matcher m2 = RETURNING_PATTERN.matcher(sql);
        if (m2.matches())
            return m2.group(1);

        throw new SqlSyntaxException();
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

    @NotNull
    private static String normalizeAlias(@NotNull String alias) {
        if ((alias.startsWith("\"") && alias.endsWith("\"")) || (alias.startsWith("[") && alias.endsWith("]")))
            return alias.substring(1, alias.length()-1);
        else
            return alias;
    }

    public static int countQueryParametersPlaceholders(@NotNull String sql) {
        boolean inLiteral = false;
        int count = 0;

        for (int i = 0, len = sql.length(); i < len; i++) {
            char ch = sql.charAt(i);
            if (ch == '\'')
                inLiteral = !inLiteral;
            else if (ch == '?' && !inLiteral)
                count++;
        }

        return count;
    }
}
