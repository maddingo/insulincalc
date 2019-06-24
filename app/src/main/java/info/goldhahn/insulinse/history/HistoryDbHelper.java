package info.goldhahn.insulinse.history;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;

    public static final String DATABASE_NAME = "history.db";

    public HistoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String createTable = "CREATE TABLE " + HistoryContract.HistoryEntry.TABLE_NAME + " (" +
                HistoryContract.HistoryEntry.Column._ID+ " INTEGER PRIMARY KEY AUTOINCREMENT," +
                HistoryContract.HistoryEntry.Column.TIMESTAMP + " INTEGER NOT NULL, " +
                HistoryContract.HistoryEntry.Column.IK + " REAL NOT NULL, " +
                HistoryContract.HistoryEntry.Column.IS + " REAL NOT NULL, " +
                HistoryContract.HistoryEntry.Column.TARGET + " REAL NOT NULL, " +
                HistoryContract.HistoryEntry.Column.BS + " REAL NOT NULL, " +
                HistoryContract.HistoryEntry.Column.KH + " REAL NOT NULL, " +
                HistoryContract.HistoryEntry.Column.INSULIN + " REAL NOT NULL" +
                " );";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + HistoryContract.HistoryEntry.TABLE_NAME);
        onCreate(db);
    }
}
