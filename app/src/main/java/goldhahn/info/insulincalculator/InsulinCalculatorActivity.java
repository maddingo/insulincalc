package goldhahn.info.insulincalculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

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


    public void onCalculate(View view) {

        EditText bsValue = (EditText) findViewById(R.id.blodsugar);
        EditText khValue = (EditText) findViewById(R.id.karbo);
        EditText insulinValue = (EditText) findViewById(R.id.insulin);

        Double bs = Double.valueOf(bsValue.getText().toString());
        Double kh = Double.valueOf(khValue.getText().toString());

        Double insulin = kh / 15 + (bs - 6) / 3.6;
        insulinValue.setText(formatNumber(insulin));
        insulinValue.setNextFocusDownId(R.id.insulin);
    }

    private String formatNumber(Number num) {
        String formatted = numberFormat.format(num.doubleValue());

        return formatted;
    }
}
