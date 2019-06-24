package info.goldhahn.insulinse;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import info.goldhahn.insulinse.history.HistoryContract.HistoryEntry;

public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int HISTORY_LOADER = 0;
    private HistoryAdapter historyAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getLoaderManager().initLoader(HISTORY_LOADER, null, this);

        historyAdapter = new HistoryAdapter(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_history_list, container, false);

        ListView listview = (ListView) rootView.findViewById(R.id.listview_history);

        listview.setAdapter(historyAdapter);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.history, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_history_clear:
                clearHistory();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearHistory() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                getActivity().getContentResolver().delete(HistoryEntry.CONTENT_URI, null, null);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case HISTORY_LOADER:
                String sortOrder = HistoryEntry.Column.TIMESTAMP + " DESC";
                return new CursorLoader(getActivity(), HistoryEntry.CONTENT_URI, HistoryEntry.ALL_COLUMNS, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        historyAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private static class HistoryAdapter extends CursorAdapter {
        public HistoryAdapter(Context context) {
            super(context, null, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            return LayoutInflater.from(context).inflate(R.layout.fragment_history_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView)view).setText(formatHistoryFromCursor(cursor));
        }

        private String formatHistoryFromCursor(Cursor cursor) {
            StringBuilder sb = new StringBuilder();
            long timestamp = cursor.getLong(HistoryEntry.Column.TIMESTAMP.ordinal());
            double bs = cursor.getDouble(HistoryEntry.Column.BS.ordinal());
            double kh = cursor.getDouble(HistoryEntry.Column.KH.ordinal());

            sb.append(formatTimestamp(timestamp));
            sb.append(": ");
            sb.append("BS:");
            sb.append(bs);
            sb.append(" KH:");
            sb.append(kh);
            return sb.toString();
        }
    }

    /**
     *
     * @param time milliseconds since 01.01.1970
     */
    private static String formatTimestamp(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd'.'MM'.'yyyy HH':'mm");
        return sdf.format(new Date(time));
    }
}
