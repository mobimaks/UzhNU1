package ua.elitasoftware.UzhNU;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DrawerActivity extends BaseActivity {

    protected ListView drawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private SimpleAdapter adapter;

    private ArrayList<Map<String, Object>> data;
    private final String KEY_IMG = "iv";
    private final String KEY_TEXT = "tv";
    private final String[] from = {KEY_IMG, KEY_TEXT};
    private final int[] to = {R.id.ivDrawerItemIcon, R.id.tvDrawerItemName};

    protected void setDrawer(){
        drawerList = (ListView)findViewById(R.id.lvDrawerList);
        int paddingTop = 0;
        if ((Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) && portraitOrientation()) {
            paddingTop = getActionBarHeight() + getStatusBarHeight();
        }
        drawerList.setPadding(10, paddingTop, 10, 0);

//        data = fillData();
//        adapter = new SimpleAdapter(this, data, R.layout.drawer_item, from, to);
//        drawerList.setAdapter(adapter);
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close);

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectItem(String itemName, int position) {
        drawerList.setItemChecked(position, true);
        if (itemName.equals(getString(R.string.allFaculties))){
            openActivity(this, FacultiesActivity.class, true);
        } else if (itemName.equals(getString(R.string.myFaculty))){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String myFaculty = preferences.getString(SettingsFragment.LIST_KEY, "-");
            try {
                int id = Integer.parseInt(myFaculty.substring(0, myFaculty.indexOf("|")));
                String caption = myFaculty.substring(myFaculty.indexOf("|")+1);
                openFaculty(id, caption);
                this.finish();
            } catch (NumberFormatException e){
                Toast.makeText(this, getString(R.string.noFacultySelect), Toast.LENGTH_SHORT).show();
            }
        } else if (itemName.equals(getString(R.string.abDownloads))){
            //check if folder empty before open
            String folderName = getResources().getString(R.string.folderName);
            File downloadFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + folderName);
            if (downloadFolder.list() == null || downloadFolder.list().length == 0) {
                Toast.makeText(this, getString(R.string.emptyFolder), Toast.LENGTH_SHORT).show();
            } else {
                openActivity(this, DownloadActivity.class, true);
            }
        }
    }

    private ArrayList<Map<String, Object>> fillData() {
        ArrayList<Map<String, Object>> data = new ArrayList<>();
        addItem(data, R.drawable.book, getString(R.string.allFaculties));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String myFaculty = preferences.getString(SettingsFragment.LIST_KEY, "-");
        if (!myFaculty.startsWith("-") && !myFaculty.startsWith("+")){
            addItem(data, R.drawable.study, getString(R.string.myFaculty));
        }
        addItem(data, R.drawable.drawer_dowload, getString(R.string.abDownloads));
        return data;
    }

    @Override
    protected void onStart() {
        data = fillData();
        adapter = new SimpleAdapter(this, data, R.layout.drawer_item, from, to);
        drawerList.setAdapter(adapter);
        super.onStart();
    }

    private void addItem(ArrayList<Map<String, Object>> data, int icon, String caption) {
        Map<String, Object> item = new HashMap<>();
        item.put(KEY_IMG, icon);
        item.put(KEY_TEXT, caption);
        data.add(item);
    }

    private boolean portraitOrientation(){
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180){
            return true;
        } else {
            return false;
        }
    }

    private int getActionBarHeight() {
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return 0;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private class DrawerItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String itemName = ((TextView)view.findViewById(R.id.tvDrawerItemName)).getText().toString();
            selectItem(itemName, position);
        }
    }
}
