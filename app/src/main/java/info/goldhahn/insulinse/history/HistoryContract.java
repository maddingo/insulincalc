package info.goldhahn.insulinse.history;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class HistoryContract {
    public static final String CONTENT_AUTHORITY = "info.goldhahn.insulinse";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_HISTORY = "history";

    public static final class HistoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_HISTORY).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HISTORY;
        public static final String CONTENT_TYPE_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HISTORY;

        public static final String TABLE_NAME = "history";

        public enum Column {
            _ID(BaseColumns._ID),
            TIMESTAMP("logtime"),
            IK("ikarb"),
            IS("isens"),
            TARGET("bs_target"),
            BS("blood_sugar"),
            KH("carbs"),
            INSULIN("insulin");

            private final String colName;

            Column(String name) {
                this.colName = name;
            }

            public String toString() {
                return colName;
            }
        }

        public static final String[] ALL_COLUMNS = buildAllColumns();

        private static String[] buildAllColumns() {
            Column[] colValues = Column.values();
            String[] allColumns = new String[colValues.length];
            int i = 0;
            for (Column col : colValues) {
                allColumns[i++] = col.toString();
            }
            return allColumns;
        }

        public static Uri buildHistoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
