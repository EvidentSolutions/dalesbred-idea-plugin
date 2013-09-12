package fi.evident.dalesbred.plugin.idea;

import org.jetbrains.annotations.NotNull;

final class SqlUtils {

    private SqlUtils() {
    }

    static int countQueryParametersPlaceholders(@NotNull String sql) {
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
