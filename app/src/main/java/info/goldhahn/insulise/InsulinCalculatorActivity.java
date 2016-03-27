package info.goldhahn.insulise;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import info.goldhahn.insulise.history.HistoryContract;

public class InsulinCalculatorActivity extends AppCompatActivity {
    private NumberFormat numberFormat = new DecimalFormat("###0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insulin_calculator);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_history:
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
        }
        return false;
    }

    public void onCalculate(View view) {

        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            Double ik = getPrefValue(preferences, R.string.pref_key_ik);
            Double is = getPrefValue(preferences, R.string.pref_key_is);
            Double targetVal = getPrefValue(preferences, R.string.pref_key_target);

            Double bs = getInputValue(R.id.blodsugar);
            Double kh = getInputValue(R.id.karbo);

            Double insulin = kh / ik + (bs - targetVal) / is;

            EditText insulinValue = (EditText) findViewById(R.id.insulin);
            insulinValue.setText(formatNumber(insulin));
            insulinValue.setNextFocusDownId(R.id.insulin);

            saveToHistory(ik, is, targetVal, bs, kh, insulin);
        } catch(NumberFormatException e) {
            Toast.makeText(this, getString(R.string.error_msg_missing_prefs), Toast.LENGTH_LONG).show();
        } catch (RuntimeException e) {
            Toast.makeText(this, getString(R.string.error_msg_calc), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToHistory(Double ik, Double is, Double targetVal, Double bs, Double kh, Double insulin) {

        ContentValues values = new ContentValues();
        values.put(HistoryContract.HistoryEntry.Column.TIMESTAMP.toString(), System.currentTimeMillis());
        values.put(HistoryContract.HistoryEntry.Column.IK.toString(), ik);
        values.put(HistoryContract.HistoryEntry.Column.IS.toString(), is);
        values.put(HistoryContract.HistoryEntry.Column.TARGET.toString(), targetVal);
        values.put(HistoryContract.HistoryEntry.Column.BS.toString(), bs);
        values.put(HistoryContract.HistoryEntry.Column.KH.toString(), kh);
        values.put(HistoryContract.HistoryEntry.Column.INSULIN.toString(), insulin);

        getContentResolver().insert(HistoryContract.HistoryEntry.CONTENT_URI, values);
    }

    /**
     * If the value is not set or not convertable to a double, a NumberFormatException is thrown.
     * @param preferences
     * @param prefId
     * @return
     */
    @NonNull
    private Double getPrefValue(SharedPreferences preferences, int prefId) {
        return Double.valueOf(preferences.getString(getString(prefId), ""));
    }

    @NonNull
    private Double getInputValue(int resId) {
        EditText inView = (EditText) findViewById(resId);
        if (inView != null) {
            return Double.valueOf(inView.getText().toString());
        }

        throw new IllegalArgumentException();
    }

    private String formatNumber(Number num) {
        return numberFormat.format(num.doubleValue());
    }
}
