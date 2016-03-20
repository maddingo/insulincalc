package info.goldhahn.insulise;

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
        } catch (RuntimeException e) {
            Toast.makeText(this, getString(R.string.error_msg_calc), Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    private Double getPrefValue(SharedPreferences preferences, int prefId) {
        return Double.valueOf(preferences.getString(getString(prefId), "1.0"));
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
