package ua.elitasoftware.UzhNU;

import android.content.*;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkSettings();
    }

    private void checkSettings() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultPref = "-|" +getResources().getString(R.string.noFaculty);
        String listPreference = preference.getString(SettingsFragment.LIST_KEY, defaultPref);
        String id = listPreference.substring(0, listPreference.indexOf("|"));
        String caption = listPreference.substring(listPreference.lastIndexOf("|")+1);
        switch (id){
            case "-":
                openActivity(this, FacultiesActivity.class, true);
//                startActivity(new Intent(this, FacultiesActivity.class));
//                finish();
                break;
            case "+":
                /**
                 * check if downloads folder isn't empty/exists
                 * if empty - show Toast msg and open all faculties
                 */
                File downloadFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                        getString(R.string.folderName));
                if (downloadFolder != null){
                    int num;
                    try {
                        num = downloadFolder.list().length;
                    } catch (NullPointerException e){
                        num = 0;
                    }

                    if (num > 0){
                        openActivity(this, DownloadActivity.class, true);
                    } else {
                        Toast.makeText(this, getString(R.string.emptyFolder), Toast.LENGTH_SHORT).show();
                        openActivity(this, FacultiesActivity.class, true);
                    }
                }
                break;
            default:
                openFaculty(Integer.parseInt(id), caption);
                finish();
                break;
        }
    }
}
