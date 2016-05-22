package info.goldhahn.insulise;


import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_ik)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_is)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_target)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_sms_recipients)));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String stringValue = newValue != null ? newValue.toString() : "";
        if (preference.getKey().equals(getString(R.string.pref_key_sms_recipients))) {
            if (!TextUtils.isEmpty(stringValue)) {
                String[] numbers = splitSmsRecipients(stringValue);
                for (String number : numbers) {
                    if (!PhoneNumberUtils.isWellFormedSmsAddress(number)) {
                        return false;
                    }
                }
            }
        }
        preference.setSummary(stringValue);
        return true;
    }

    @NonNull
    public static String[] splitSmsRecipients(String stringValue) {
        return stringValue.split("\\s*[\\,\\;]\\s*");
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

}
