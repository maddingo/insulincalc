package info.goldhahn.insulise;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.goldhahn.insulise.history.HistoryContract;
import info.goldhahn.insulise.history.HistoryDbHelper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class DbTest {

    private Context context;
    private Context testContext;

    @Before
    public void setupContext() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testContext = new RenamingDelegatingContext(context, "test_");
        deleteDatabase();
    }

    private void deleteDatabase() {
        context.deleteDatabase(HistoryDbHelper.DATABASE_NAME);
    }

    @Test
    public void createDb() {

        SQLiteDatabase db = new HistoryDbHelper(context).getWritableDatabase();

        assertThat(db, is(notNullValue()));
        assertThat(db.isOpen(), is(true));

        // Verify thst table exists in the database
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertThat("Cursor ", c.moveToFirst(), is(true));
        List<String> tables = new ArrayList<String>();
        do {
            tables.add(c.getString(0));
        } while(c.moveToNext());

        assertThat(tables, hasItem(equalTo(HistoryContract.HistoryEntry.TABLE_NAME)));

        // Verify that table contains all columns
        c = db.rawQuery("PRAGMA table_info(" + HistoryContract.HistoryEntry.TABLE_NAME + ")", null);
        assertThat(c.moveToFirst(), is(true));

        Set<String> columns = new HashSet<String>();
        int nameIdx = c.getColumnIndex("name");
        do {
            columns.add(c.getString(nameIdx));
        } while(c.moveToNext());

        assertThat("Number of database columns should match the contract", columns.size(), is(equalTo(HistoryContract.HistoryEntry.Column.values().length)));

        assertThat(columns.containsAll(Arrays.asList(HistoryContract.HistoryEntry.ALL_COLUMNS)), is(true));

        db.close();
    }

    @Test
    public void historyTable() {
        long historyId = insertHistoryEntry();

        assertThat(historyId, is(not(equalTo(-1L))));

        HistoryDbHelper helper = new HistoryDbHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c = db.query(HistoryContract.HistoryEntry.TABLE_NAME, null, null, null, null, null, null);
        assertThat(c, is(notNullValue()));

        assertThat(c.moveToFirst(), is(true));

        Map<String, Object> historyEntries = new HashMap<String, Object>();
        for (HistoryContract.HistoryEntry.Column col : HistoryContract.HistoryEntry.Column.values()) {
            historyEntries.put(col.toString(), null);
        }
        for (int i = 0; i < c.getColumnCount(); i++) {
            String colName = c.getColumnName(i);
            Object val;
            int type = c.getType(i);
            switch(type) {
                case Cursor.FIELD_TYPE_INTEGER:
                    val = c.getInt(i);
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    val = c.getString(i);
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    val = c.getDouble(i);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled type: " + type);
            }
            Object oldValue = historyEntries.put(colName, val);
            assertThat (oldValue, is(nullValue()));
        }
    }

    private long insertHistoryEntry() {
        HistoryDbHelper helper = new HistoryDbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(HistoryContract.HistoryEntry.Column.BS.toString(), 6.0);
        values.put(HistoryContract.HistoryEntry.Column.KH.toString(), 45.0);
        values.put(HistoryContract.HistoryEntry.Column.IK.toString(), 15.0);
        values.put(HistoryContract.HistoryEntry.Column.IS.toString(), 3.6);
        values.put(HistoryContract.HistoryEntry.Column.TARGET.toString(), 6.0);
        values.put(HistoryContract.HistoryEntry.Column.TIMESTAMP.toString(), System.currentTimeMillis());
        values.put(HistoryContract.HistoryEntry.Column.INSULIN.toString(), 3.0);

        long histIdx = db.insert(HistoryContract.HistoryEntry.TABLE_NAME, null, values);

        db.close();
        return histIdx;
    }
}
