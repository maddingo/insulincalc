package goldhahn.info.insulincalculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Insulin_Calculator extends AppCompatActivity {
    private NumberFormat numberFormat = new DecimalFormat("###0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insulin__calculator);
    }

    public void onCalculate(View view) {

        EditText bsValue = (EditText) findViewById(R.id.blodsugar);
        EditText khValue = (EditText) findViewById(R.id.karbo);
        EditText insulinValue = (EditText) findViewById(R.id.insulin);

        Double bs = Double.valueOf(bsValue.getText().toString());
        Double kh = Double.valueOf(khValue.getText().toString());

        Double insulin = kh / 15 + (bs - 6) / 3;
        insulinValue.setText(formatNumber(insulin));
    }

    private String formatNumber(Number num) {
        String formatted = numberFormat.format(num.doubleValue());

        return formatted;
    }

}
