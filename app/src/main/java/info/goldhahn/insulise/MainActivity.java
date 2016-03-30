package info.goldhahn.insulise;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import info.goldhahn.insulise.history.HistoryContract;

public class MainActivity extends AppCompatActivity {
    public static final DecimalFormatSymbols DECIMAL_FORMAT_SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private NumberFormat numberFormat = new DecimalFormat("###0.00", DECIMAL_FORMAT_SYMBOLS);
    private NumberFormat NUMBER_FORMAT_1DIGIT = new DecimalFormat("###0.0", DECIMAL_FORMAT_SYMBOLS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        View karbo = findViewById(R.id.karbo);
        karbo.setNextFocusDownId(R.id.blodsugar);
        View bs = findViewById(R.id.blodsugar);
        bs.setNextFocusDownId(R.id.calcButton);
        karbo.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void onCalculate(View view) {

        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            Double ik = getPrefValue(preferences, R.string.pref_key_ik);
            Double is = getPrefValue(preferences, R.string.pref_key_is);
            Double targetVal = getPrefValue(preferences, R.string.pref_key_target);

            Double bs = getInputValue(R.id.blodsugar);
            Double kh = getInputValue(R.id.karbo);

            Double insulin = kh / ik + (bs - targetVal) / is;

            TextView insulinValue = (TextView) findViewById(R.id.insulin);
            insulinValue.setText(formatNumber(insulin));
            insulinValue.setNextFocusDownId(R.id.insulin);

            // jump to insuline window
            insulinValue.requestFocus();
        } catch(NumberFormatException e) {
            Toast.makeText(this, getString(R.string.error_msg_missing_prefs), Toast.LENGTH_LONG).show();
        } catch (RuntimeException e) {
            Toast.makeText(this, getString(R.string.error_msg_calc), Toast.LENGTH_SHORT).show();
        }
    }

    public void onSave(View view) {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                Double ik = getPrefValue(preferences, R.string.pref_key_ik);
                Double is = getPrefValue(preferences, R.string.pref_key_is);
                Double targetVal = getPrefValue(preferences, R.string.pref_key_target);

                Double bs = getInputValue(R.id.blodsugar);
                Double kh = getInputValue(R.id.karbo);

                Double insulin = getInputValue(R.id.insulin);

                saveToHistory(ik, is, targetVal, bs, kh, insulin);
            }
        });
    }

    public void onAdjustAuto(View view) {
        adjustInsulin(0.0);
    }

    public void onAdjustPlus(View view) {
        adjustInsulin(0.5);
    }

    public void onAdjustMinus(View view) {
        adjustInsulin(-0.5);
    }

    public void onSendSMS(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String[] smsRecipients = getPrefList(preferences, R.string.pref_key_sms_recipients);

        StringBuilder text = new StringBuilder();
        text.append("BS:").append(getInputValue(R.id.blodsugar)).append(" ");
        text.append("KH:").append(getInputValue(R.id.karbo)).append(" ");
        text.append(" = ").append(getInputValue(R.id.insulin));
        for (String recipient : smsRecipients) {
            SmsManager.getDefault().sendTextMessage(recipient, null, text.toString(), null, null);
        }
    }

    private void adjustInsulin(double adjustment) {
        TextView insulinValue = (TextView) findViewById(R.id.insulin);
        Double insulin = Double.valueOf(insulinValue.getText().toString());
        insulinValue.setText(formatNumberPointFive(insulin + adjustment));
        insulinValue.setNextFocusDownId(R.id.insulin);

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
     */
    @NonNull
    private Double getPrefValue(SharedPreferences preferences, int prefId) {
        return Double.valueOf(preferences.getString(getString(prefId), ""));
    }

    private String[] getPrefList(SharedPreferences preferences, int prefId) {
        String prefValue = preferences.getString(getString(prefId), "");
        if (!TextUtils.isEmpty(prefValue)) {
            return SettingsActivity.splitSmsRecipients(prefValue);
        }
        return new String[0];
    }

    @NonNull
    private Double getInputValue(int resId) {
        TextView inView = (TextView) findViewById(resId);
        if (inView != null) {
            return Double.valueOf(inView.getText().toString());
        }

        throw new IllegalArgumentException();
    }

    private String formatNumber(Number num) {
        return numberFormat.format(num.doubleValue());
    }

    /**
     * Round to next .5 value and format
     */
    private String formatNumberPointFive(Number num) {

        return NUMBER_FORMAT_1DIGIT.format(Math.round(num.doubleValue() * 2.0) / 2.0);
    }
}
