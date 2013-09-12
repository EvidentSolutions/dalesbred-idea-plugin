package fi.evident.dalesbred.plugin.idea;

import org.jetbrains.annotations.NotNull;

final class SqlUtils {

    private SqlUtils() {
    }

    static int countQueryParametersPlaceholders(@NotNull String sql) {
        // TODO don't count placeholders inside string literals
        int count = 0;
        for (int i = 0, len = sql.length(); i < len; i++)
            if (sql.charAt(i) == '?')
                count++;
        return count;
    }
}
