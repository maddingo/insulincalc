package info.goldhahn.insulise.history;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class HistoryProvider extends ContentProvider {

    private static final UriMatcher uriMatcher = buildUriMatcher();

    private static final SQLiteQueryBuilder historyQueryBuilder = buildHistoryQueryBuilder();
    private HistoryDbHelper dbHelper;

    private static SQLiteQueryBuilder buildHistoryQueryBuilder() {
        final SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

        sqlBuilder.setTables(HistoryContract.HistoryEntry.TABLE_NAME);
        return sqlBuilder;
    }

    private static final int HISTORY = 100;
    private static final int HISTORY_WITH_ID = 101;

    public HistoryProvider() {
    }

    @Override
    public boolean onCreate() {
        dbHelper = new HistoryDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch(uriMatcher.match(uri)) {
            case HISTORY:
                return HistoryContract.HistoryEntry.CONTENT_TYPE;
            case HISTORY_WITH_ID:
                return HistoryContract.HistoryEntry.CONTENT_TYPE_ITEM;
            default:
                throw new IllegalArgumentException(String.valueOf(uri));
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri insertedUri;

        switch(uriMatcher.match(uri)) {
            case HISTORY:
                long historyId = dbHelper.getWritableDatabase().insert(
                        HistoryContract.HistoryEntry.TABLE_NAME,
                        null,
                        values);
                if (historyId > 0) {
                    insertedUri = HistoryContract.HistoryEntry.buildHistoryUri(historyId);
                } else {
                    throw new SQLException("Failed to insert " + uri);
                }
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(uri));
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return insertedUri;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch(uriMatcher.match(uri)) {
            case HISTORY:
                cursor = dbHelper.getReadableDatabase().query(
                        HistoryContract.HistoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(uri));
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void shutdown() {
        dbHelper.close();
        super.shutdown();
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(HistoryContract.CONTENT_AUTHORITY, HistoryContract.PATH_HISTORY, HISTORY);
        matcher.addURI(HistoryContract.CONTENT_AUTHORITY, HistoryContract.PATH_HISTORY + "/#", HISTORY_WITH_ID);
        return matcher;
    }
}
