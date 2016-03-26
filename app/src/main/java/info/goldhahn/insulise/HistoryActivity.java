package info.goldhahn.insulise;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class HistoryActivity extends AppCompatActivity {

    private static final String HISTFRAG_TAG = "HIST_FRAG_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.history_container, new HistoryFragment(), HISTFRAG_TAG)
//                    .commit();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
