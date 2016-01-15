package com.example.owner.pins;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Owner on 12/14/2015.
 */
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager manage = getFragmentManager();
        FragmentTransaction transact = manage.beginTransaction();
        myPrefsFragment prefs = new myPrefsFragment();
        transact.replace(android.R.id.content, prefs);
        transact.commit();
    }

    public static class myPrefsFragment extends PreferenceFragment {
        SharedPreferences.OnSharedPreferenceChangeListener listener;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate((savedInstanceState));
            addPreferencesFromResource(R.xml.preferences);
        }
    }

}
