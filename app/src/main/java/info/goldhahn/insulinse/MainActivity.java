package info.goldhahn.insulinse;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import info.goldhahn.insulinse.history.HistoryContract;

public class MainActivity extends AppCompatActivity {
    public static final DecimalFormatSymbols DECIMAL_FORMAT_SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private NumberFormat numberFormat = new DecimalFormat("###0.00", DECIMAL_FORMAT_SYMBOLS);
    private NumberFormat NUMBER_FORMAT_1DIGIT = new DecimalFormat("###0.0", DECIMAL_FORMAT_SYMBOLS);

    /**
     * This field is used to make the MainActivity more testable.
     * The test hangs if a Toast is shown in case of an error.
     */
    private CalculationErrorHandler calculationErrorHandler;

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
        View bs = findViewById(R.id.blodsugar);
        bs.setNextFocusDownId(R.id.calcButton);
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
//            insulinValue.setNextFocusDownId(R.id.insulin);

            // jump to insulin window
//            insulinValue.requestFocus();
        } catch(NumberFormatException e) {
            getCalculationErrorHandler().handleError(this, R.string.error_msg_missing_prefs);
        } catch (RuntimeException e) {
            getCalculationErrorHandler().handleError(this, R.string.error_msg_calc);
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
     * If the value is not set or not convertible to a double, a NumberFormatException is thrown.
     */
    @NonNull
    private Double getPrefValue(SharedPreferences preferences, int prefId) {
        String prefValue = preferences.getString(getString(prefId), "");
        return Double.valueOf(prefValue);
    }

    @NonNull
    private Double getInputValue(int resId) {
        TextView inView = findViewById(resId);
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

    public CalculationErrorHandler getCalculationErrorHandler() {
        if (calculationErrorHandler == null) {
            synchronized(this) {
                if (calculationErrorHandler == null) {
                    calculationErrorHandler = new StandardCalculationErrorHandler();
                }
            }
        }
        return calculationErrorHandler;
    }

    public synchronized void setCalculationErrorHandler(CalculationErrorHandler calculationErrorHandler) {
        this.calculationErrorHandler = calculationErrorHandler;
    }

    private static class StandardCalculationErrorHandler implements CalculationErrorHandler {

        @Override
        public void handleError(Context ctx, @StringRes int resId) {
            Toast.makeText(ctx, ctx.getString(resId), Toast.LENGTH_LONG).show();
        }
    }

    public interface CalculationErrorHandler {
        void handleError(Context ctx, @StringRes int resId);
    }
}
