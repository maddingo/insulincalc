package info.goldhahn.insulise;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import info.goldhahn.insulise.history.HistoryContract;
import info.goldhahn.insulise.history.HistoryProvider;
import info.goldhahn.insulise.util.TestContentObserver;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ProviderTest {
    Context context;

    @Before
    public void createContext() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void providerRegistry() {
        PackageManager pm = context.getPackageManager();

        ComponentName cn = new ComponentName(context.getPackageName(), HistoryProvider.class.getName());

        try {
            ProviderInfo pi = pm.getProviderInfo(cn, 0);

            assertThat(pi.authority, is(equalTo(HistoryContract.CONTENT_AUTHORITY)));

        } catch (PackageManager.NameNotFoundException e) {
            assertThat(e, is(nullValue()));
        }
    }

    @Test
    public void getType() {
        // content://info.goldhahn.insulise/history
        String type = context.getContentResolver().getType(HistoryContract.HistoryEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertThat(HistoryContract.HistoryEntry.CONTENT_TYPE, is(equalTo(type)));

        // content://info.goldhhn.insulise/history/1
        type = context.getContentResolver().getType(
                HistoryContract.HistoryEntry.buildHistoryUri(1L));
        // vnd.android.cursor.item/info.goldhahn.insulise/history
        assertThat(HistoryContract.HistoryEntry.CONTENT_TYPE_ITEM, is(equalTo(type)));
    }

    @Test
    public void allHistoryQuery() {
        DbTest.insertHistoryEntry(context, DbTest.getContentValues(6.0, 45.0));

        Cursor c = context.getContentResolver().query(HistoryContract.HistoryEntry.CONTENT_URI, null, null, null, null);
        assertThat(c, is(notNullValue()));

        assertThat(c.moveToFirst(), is(true));

        Map<String, Object> entry = DbTest.cursorToMap(c);
        assertThat(entry.size(), is(equalTo(HistoryContract.HistoryEntry.Column.values().length)));
    }

    @Test
    public void insertHistory() {
        DbTest.deleteDatabase(context);

        TestContentObserver tco = TestContentObserver.getTestContentObserver();
        ContentResolver resolver = context.getContentResolver();
        resolver.registerContentObserver(HistoryContract.HistoryEntry.CONTENT_URI, true, tco);

        ContentValues values = DbTest.getContentValues(7.0, 50.0);
        resolver.insert(HistoryContract.HistoryEntry.CONTENT_URI, values);

        tco.waitForNotificationOrFail();
    }

    @Test
    public void deleteHistory() {
        DbTest.deleteDatabase(context);
        DbTest.insertHistoryEntry(context, DbTest.getContentValues(6.0, 45.0));
        DbTest.insertHistoryEntry(context, DbTest.getContentValues(7.0, 15.0));

        TestContentObserver tco = TestContentObserver.getTestContentObserver();
        ContentResolver resolver = context.getContentResolver();
        resolver.registerContentObserver(HistoryContract.HistoryEntry.CONTENT_URI, true, tco);
        int nDeleted = resolver.delete(HistoryContract.HistoryEntry.CONTENT_URI, null, null);
        tco.waitForNotificationOrFail();
        assertThat(nDeleted, is(2));
    }
}
