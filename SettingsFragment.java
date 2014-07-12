package ua.elitasoftware.UzhNU;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.ExecutionException;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener, OnPreferenceClickListener {

//    public interface OnPreferenceChange{
//        void onPreferenceChange(SharedPreferences sharedPreferences, String key);
//    }

    public final static String LIST_KEY = "list_of_faculties";
    public final static String CLEAR_ALL_KEY = "clear_folder";
    public final static String FEEDBACK_KEY = "feedback";
    public final static String DATE_KEY = "date";

    private ListPreference listPreference;
    private ListPreference datePreference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //add preferences from xml
        addPreferencesFromResource(R.xml.preferences);
        datePreference = (ListPreference) findPreference(DATE_KEY);
        if (datePreference.getValue() != null) {
            datePreference.setSummary(datePreference.getEntry());
        }
        //change entries of list preference
        listPreference = (ListPreference) findPreference(LIST_KEY);
        try {
            String[][] entries = (String[][]) new FacultiesGetter().execute(getActivity().getApplicationContext()).get();
            String[] entryValues = new String[entries[0].length];
            for (int i = 0; i < entryValues.length; i++) {
                entryValues[i] = entries[0][i] + "|" + entries[1][i];
            }
            listPreference.setEntryValues(entryValues);
            listPreference.setEntries(entries[1]);
            if (listPreference.getValue() != null) {
                listPreference.setSummary(listPreference.getEntry());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        Preference clearAllPref = findPreference(CLEAR_ALL_KEY);
        Preference feedbackPref = findPreference(FEEDBACK_KEY);
        clearAllPref.setOnPreferenceClickListener(this);
        feedbackPref.setOnPreferenceClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case LIST_KEY:
                listPreference.setSummary(listPreference.getEntry());

                break;
            case DATE_KEY:
                datePreference.setSummary(datePreference.getEntry());
                break;
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        OnPreferenceClick listener = (OnPreferenceClick) getActivity();
        listener.onPreferenceClick(preference);
        return true;
    }

    public interface OnPreferenceClick {
        void onPreferenceClick(Preference preference);
    }

    class FacultiesGetter extends AsyncTask<Context, Integer, Object> {
        @Override
        protected Object doInBackground(Context... params) {
            DBHelper dbHelper = new DBHelper(params[0]);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            Cursor c = db.query(dbHelper.FACULTIES, null, null, null, null, null, null);
            String[][] entries;
            if (c.moveToFirst()) {
                entries = new String[2][c.getCount() + 2];
                entries[0][0] = "-";
                entries[1][0] = getResources().getString(R.string.noFaculty);

                entries[0][1] = "+";
                entries[1][1] = getResources().getString(R.string.downloadActivity);
                int idIndex = c.getColumnIndex(dbHelper.ID);
                int captionIndex = c.getColumnIndex(dbHelper.CAPTION);

                for (int i = 2; i < c.getCount() + 2; i++) {
                    entries[0][i] = c.getString(idIndex);
                    entries[1][i] = c.getString(captionIndex);
                    c.moveToNext();
                }
            } else {
                entries = new String[2][2];
                entries[0][0] = "-";
                entries[1][0] = getResources().getString(R.string.noFaculty);

                entries[0][1] = "+";
                entries[1][1] = getResources().getString(R.string.downloadActivity);
            }
            return entries;
        }
    }
}
