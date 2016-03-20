package info.goldhahn.insulise;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class InsulinCalculatorActivityTest {

    @Rule
    public ActivityTestRule<InsulinCalculatorActivity> testRule = new ActivityTestRule<InsulinCalculatorActivity>(InsulinCalculatorActivity.class);

//    @Test
    public void withDefaultPrefs() {
        calculate(null, null, null, "12", "50", "5.00");
    }

    @Test
    public void withPrefs1() {
        calculate("12", "3.4", "5.5", "13", "52", "6.54");
    }

    private void calculate(String prefIK, String prefIS, String prefTarget, String bs, String carb, String expected) {
        InsulinCalculatorActivity activity = testRule.getActivity();

        // prepare preferences
        int nPrefChanges = 0;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        if (prefIK != null) {
            editor.putString(activity.getString(R.string.pref_key_ik), prefIK);
            nPrefChanges++;
        }
        if (prefIS != null) {
            editor.putString(activity.getString(R.string.pref_key_is), prefIS);
            nPrefChanges++;
        }
        if (prefTarget != null) {
            editor.putString(activity.getString(R.string.pref_key_target), prefTarget);
            nPrefChanges++;
        }
        if (nPrefChanges > 0) {
            editor.apply();
            editor.commit();
        }

        onView(withId(R.id.karbo))
                .perform(typeText(carb), closeSoftKeyboard());

        onView(withId(R.id.blodsugar))
                .perform(typeText(bs), closeSoftKeyboard());

        onView(withId(R.id.calcButton))
                .perform(click());

        onView(withId(R.id.insulin)).check(matches(withText(expected)));
    }
}
