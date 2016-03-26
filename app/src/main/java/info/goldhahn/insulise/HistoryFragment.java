package info.goldhahn.insulise;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import info.goldhahn.insulise.history.HistoryContract;

public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private CursorAdapter historyAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        historyAdapter = new HistoryAdapter(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_history_list, container, false);

        ListView listview = (ListView) rootView.findViewById(R.id.listview_history);
        listview.setAdapter(historyAdapter);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = HistoryContract.HistoryEntry.Column.TIMESTAMP + " DESC";
        return new CursorLoader(getActivity(), HistoryContract.HistoryEntry.CONTENT_URI, HistoryContract.HistoryEntry.ALL_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

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
            return "text";
        }

    }
}
