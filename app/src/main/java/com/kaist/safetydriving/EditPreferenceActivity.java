package com.kaist.safetydriving;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class EditPreferenceActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preference);
    }
}
