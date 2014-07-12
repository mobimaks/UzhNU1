package ua.elitasoftware.UzhNU;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.DownloadManager;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class BaseActivity extends Activity {

    public static final int TYPE_APP_UPD = 4;
    public static final String LAST_TIME_CHECK = "last_time_checked";
    public static String newVer = "";
    protected BroadcastReceiver receiver;
    protected Bundle anim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //якщо версія KitKat і більше то робим статус бар жовтим
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(getResources().getColor(R.color.yellowUZHNU));
        }
        //прикріпляєм ActionBar
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        anim = ActivityOptions.makeCustomAnimation(this, R.anim.animation, R.anim.animation2).toBundle();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                //check if downloaded file is 'apk'
                if (MimeTypeMap.getSingleton().getMimeTypeFromExtension("apk").equals(dm.getMimeTypeForDownloadedFile(downloadId))) {
                    finish();
                }
                //open downloaded file
                Uri myUri = dm.getUriForDownloadedFile(downloadId);
                String myMimeType = dm.getMimeTypeForDownloadedFile(downloadId);
                Intent openFile = new Intent(Intent.ACTION_VIEW).setDataAndType(myUri, myMimeType);
                try{
                    startActivity(openFile);
                } catch (ActivityNotFoundException e){}
            }
        };
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        checkNewVersion();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.abSettings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.abDownloads:
                String folderName = getResources().getString(R.string.folderName);
                File downloadFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + folderName);
                if (downloadFolder.list() == null || downloadFolder.list().length == 0) {
                    Toast.makeText(this, getString(R.string.emptyFolder), Toast.LENGTH_SHORT).show();
                } else {
                    intent = new Intent(this, DownloadActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.abAbout:
                new AboutDialog(getFragmentManager());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void checkNewVersion() {
        try {
            //TODO
            if (hasInternet() && needCheckForUpd()){//&& (Boolean)new CheckNewVersion().execute().get()) {
                String res = new CheckNewVersion().execute().get();
                if (res != null){
                    openNewVerDialog(res);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    protected void openFaculty(Integer id, String title){
        Intent intent =  new Intent(this, TimetableActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("title", title);
        startActivity(intent, anim);
    }

    protected void openActivity(Activity aClass, Class<? extends BaseActivity> activityClass, boolean finish){
        startActivity(new Intent(aClass, activityClass), anim);
        if (finish){
            aClass.finish();
        }
    }

    protected void openNewVerDialog(String changeLog) {
        String caption = getString(R.string.availableNewVer) + "\n" + getString(R.string.appUpdAsk) + "\n\n" + changeLog;
        DownloadDialog dialog = new DownloadDialog(getFragmentManager(), caption, TYPE_APP_UPD);
    }

    protected boolean needCheckForUpd() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long lastTimeChecked = preferences.getLong(LAST_TIME_CHECK, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -12);
        long time = calendar.getTimeInMillis();
        if (lastTimeChecked < time) {
            preferences.edit().putLong(LAST_TIME_CHECK, System.currentTimeMillis()).commit();
            return true;
        } else return false;
    }

    protected void clearTempFolder(){
        String folderName = getString(R.string.tempFolderName);
        File tempFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + folderName);
        removeDirectory(tempFolder);
    }

    protected boolean removeDirectory(File file) {
        if (file == null)
            return true;
        if (!file.exists())
            return true;
        if (file.isFile())
            return false;

        String[] list = file.list();

        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                File item = new File(file, list[i]);

                if (item.isDirectory()) {
                    if (!removeDirectory(item))
                        return false;
                } else {
                    if (!item.delete()) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

    private boolean hasInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnected());
    }

    protected class CheckNewVersion extends AsyncTask<Void, Void, String> {

        private boolean newVerAvailable = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lockRotation();
        }

        @Override
        protected String doInBackground(Void... params) {
            HandleHTTP handleHTTP = new HandleHTTP();
            String url = getString(R.string.verUrl);//"http://mobimaks.ucoz.ru/version.txt";
            String version = handleHTTP.makeRequest(url);
            if (version == null) {
                newVerAvailable = false;
            }
            Double versionSite, versionPhone;
            try {
                versionSite = Double.parseDouble(version);
                String packageVer;
                try {
                    packageVer = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                    versionPhone = Double.parseDouble(packageVer);
                    if (versionSite > versionPhone) {
                        newVer = String.valueOf(versionSite);
                        newVerAvailable = true;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    newVerAvailable = false;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                newVerAvailable = false;
            } catch (NullPointerException e){
                e.printStackTrace();
                newVerAvailable = false;
            }

            String changeLog = null;

            if (newVerAvailable){
                handleHTTP = new HandleHTTP();
                String changeLogUrl = getString(R.string.changeLogUrl);
                changeLog = handleHTTP.makeRequest(changeLogUrl);
            }
            return changeLog;
        }

        @Override
        protected void onPostExecute(String aString) {
            super.onPostExecute(aString);
            unlockRotation();
        }

        private void lockRotation(){
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
            else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }
        }

        private void unlockRotation(){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }
}
