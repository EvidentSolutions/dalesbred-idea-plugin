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

public final class SqlUtils {

    private static final Pattern SELECT_LIST_PATTERN = Pattern.compile("\\s*select\\s+((all|distinct(\\s+on\\s*(\\(.*?\\)))?)\\s+)?(.+?)\\s+from.+", Pattern.CASE_INSENSITIVE);
    private static final Pattern SELECT_ITEM_PATTERN = Pattern.compile("(.+\\.)?(.+?)(\\s+(as\\s+)?(.+))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern COMMA_SEP_PATTERN = Pattern.compile("\\s*,\\s*");

    private SqlUtils() {
    }

    @NotNull
    public static List<String> selectVariables(@NotNull String sql) {
        Matcher matcher = SELECT_LIST_PATTERN.matcher(sql);
        if (matcher.matches()) {
            String[] selectItems = COMMA_SEP_PATTERN.split(matcher.group(5));

            List<String> result = new ArrayList<String>(selectItems.length);
            for (String selectItem : selectItems)
                result.add(parseSelectItem(selectItem));

            return result;
        } else {
            return Collections.emptyList();
        }
    }

    @NotNull
    private static String parseSelectItem(@NotNull String selectItem) {
        Matcher matcher = SELECT_ITEM_PATTERN.matcher(selectItem);
        assert matcher.matches();

        if (matcher.group(5) != null)
            return matcher.group(5);

        return matcher.group(2);
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
