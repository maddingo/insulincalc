package info.goldhahn.insulinse;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> testRule = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Test
    public void withDefaultPrefs() {
        calculate(null, null, null, "12", "50", null, R.string.error_msg_missing_prefs);
    }

    @Test
    public void withPrefs1() {
        calculate("12", "3.4", "5.5", "13", "52", "6.54", 0);
    }

    @Test
    public void missingValues() {
        calculate("15", "3.0", "6.0", "", "", null, R.string.error_msg_missing_prefs);
    }

    private void calculate(String prefIK, String prefIS, String prefTarget, String bs, String carb, String expected, @StringRes int expectedErrorCode) {
        MainActivity activity = testRule.getActivity();

        TestCalculationErrorHandler errorHandler = new TestCalculationErrorHandler();
        activity.setCalculationErrorHandler(errorHandler);
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

        onView(withId(R.id.karbo)).perform(typeText(carb), closeSoftKeyboard());

        onView(withId(R.id.blodsugar)).perform(typeText(bs), closeSoftKeyboard());

        onView(withId(R.id.calcButton)).perform(click());

        if (expected != null) {
            onView(withId(R.id.insulin)).check(matches(withText(expected)));
        } else {
            assertThat(errorHandler.errorCode, is(equalTo(expectedErrorCode)));
        }
    }

    private static class TestCalculationErrorHandler implements MainActivity.CalculationErrorHandler {

        private int errorCode;

        @Override
        public void handleError(Context ctx, @StringRes int resId) {
            this.errorCode = resId;
        }
    }
}
