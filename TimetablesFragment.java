package ua.elitasoftware.UzhNU;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class TimetablesFragment extends Fragment implements OnChildClickListener, OnGroupClickListener {

    public static final String GET_FILE = "http://www.uzhnu.edu.ua/uk/infocentre/get/";
    public static final String GET = "http://www.uzhnu.edu.ua/uk/infocentre/openApi/";
    //Item types const
    public static final int TYPE_FOLDER = 0;
    public static final int TYPE_FILE = 1;
    public static final int TYPE_LINK = 2;
    public static final int TYPE_TEXT = 3;
    //Service const
    public static final String FILE = "file";
    public static final String FILE_NAME = "name";
    public static final String FILE_EXTENSION = "ext";
    //upd const
    public static final String ITEMS_FROM_DB = "db";
    public static final String ITEMS_FROM_INTERNET = "new";
    public static final String ADD_ITEM_TO_DB = "add";
    //JSON codes
    private final String TAG_ID = "id";
    private final String TAG_ITEM_TYPE = "item_type";
    private final String TAG_CAPTION = "caption";
    private final String TAG_DESCRIPTION = "description";
    private final String TAG_POST_DATE = "post_date";
    private final String TAG_HITS = "hits";
    private final String TAG_ITEMS = "items";
    private String mainId;
    private String mainTitle;
    private String parentId;
    private String parentTitle;
    private ProgressBar pbDownloadTimetable;
    private ExpandableListView elvTimetableExpListView;
    private Timetable timetable;
    private TimetablesAdapter adapter;
    private DownloadDialog dialog;
    private boolean rotated = false;
    private boolean noData = false;
    private TimetableRequest task;

    public interface OnGroupSelect {
        void onGroupSelect(ExpandableListView parent, int groupPosition, TimetableItem item);
    }

    public interface OnChildSelect {
        void onChildSelect(TimetableItem item);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            timetable = savedInstanceState.getParcelable("timetable");
            setMainId(savedInstanceState.getString("mainId"));
            setMainTitle(savedInstanceState.getString("mainTitle"));
            setParentId(savedInstanceState.getString("parentId"));
            setParentTitle(savedInstanceState.getString("parentTitle"));
            rotated = savedInstanceState.getBoolean("rotated");
            noData = savedInstanceState.getBoolean("noData");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (task != null){
            task.cancel(true);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_timetables, container, false);
        pbDownloadTimetable = (ProgressBar) v.findViewById(R.id.pbDownloadTimetable);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("timetable", timetable);
        outState.putString("mainId", getMainId());
        outState.putString("mainTitle", getMainTitle());
        outState.putString("parentId", getParentId());
        outState.putString("parentTitle", getParentTitle());
        outState.putBoolean("rotated", true);
        outState.putBoolean("noData", noData);
        super.onSaveInstanceState(outState);
    }

    public void openFolder(String id, String caption, String parentId, String parentTitle) {
        getActivity().setTitle(caption);
        if (!rotated) {
            setMainId(id);
            setMainTitle(caption);
            setParentId(parentId);
            setParentTitle(parentTitle);
        }

        //if internet available - get data from internet, create three with new data and save this to DB
        if (hasInternet() || (timetable != null)) {
            updateTimetable(id, caption);
        } else {
            clearTimetable();
            ImageView ivNoInternet = (ImageView) getActivity().findViewById(R.id.ivNoInternet);
            TextView tvNoInternet = (TextView) getActivity().findViewById(R.id.tvNoInternet);
            ivNoInternet.setVisibility(View.VISIBLE);
            tvNoInternet.setVisibility(View.VISIBLE);
        }
    }

    private void createTree(ArrayList<TimetableItem> timetableItems) {
        elvTimetableExpListView = (ExpandableListView) getActivity().findViewById(R.id.elvTimetableExpListView);
        adapter = new TimetablesAdapter(getActivity(), timetableItems);
        elvTimetableExpListView.setAdapter(adapter);
        elvTimetableExpListView.setOnChildClickListener(this);
        elvTimetableExpListView.setOnGroupClickListener(this);
        if (elvTimetableExpListView.getVisibility() == View.INVISIBLE) {
            elvTimetableExpListView.setVisibility(View.VISIBLE);
        }
    }

    private void updateTimetable(String id, String caption) {
            if (timetable == null) {
//                timetable = (Timetable) new TimetableRequest().execute(ITEMS_FROM_INTERNET, id).get();
                task = new TimetableRequest();
                task.execute(ITEMS_FROM_INTERNET, id, caption);
            } else if (noData) {//timetable == null || timetable.getItems().size() == 0) {
                //no information
                ImageView ivNoInternet = (ImageView) getActivity().findViewById(R.id.ivNoInternet);
                ivNoInternet.setImageResource(R.drawable.empty_faculty);
                ivNoInternet.setVisibility(View.VISIBLE);
                TextView tvNoInternet = (TextView) getActivity().findViewById(R.id.tvNoInternet);
                tvNoInternet.setText(getString(R.string.emptyFaculty));
                tvNoInternet.setVisibility(View.VISIBLE);
            } else {
                createTree(timetable.getItems());
                timetable.setId(id);
                timetable.setCaption(caption);
                timetable.setParent_id(getParentId());
            }
    }

    private void clearTimetable() {
        if (elvTimetableExpListView != null) {
            elvTimetableExpListView.setVisibility(View.INVISIBLE);
        }
    }

    private boolean hasInternet() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnected());
    }

    //calling the interface
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        OnChildSelect listener = (OnChildSelect) getActivity();
        listener.onChildSelect((TimetableItem) adapter.getChild(groupPosition, childPosition));
        return true;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        OnGroupSelect listener = (OnGroupSelect) getActivity();
        listener.onGroupSelect(parent, groupPosition, (TimetableItem) adapter.getGroup(groupPosition));
        return true;
    }

    //file clicked dialog
    public void showDialog(int id, String caption) {
        dialog = new DownloadDialog(getFragmentManager(), id, caption, TYPE_FILE);
    }

    //link clicked dialog
    public void showDialog(String caption, String description) {
        dialog = new DownloadDialog(getFragmentManager(), caption, description, TYPE_LINK);
    }

    //text clicked dialog
    public void showDialog(String caption) {
        dialog = new DownloadDialog(getFragmentManager(), caption, TYPE_TEXT);
    }

    //class for making request and parsing JSON
    //TODO: retain AsyncTask during rotation
    class TimetableRequest extends AsyncTask<String, Void, Void> {

        //Item string types const
        private final String TYPE_FOLDER = "folder";
        private final String TYPE_FILE = "file";
        private final String TYPE_LINK = "link";
        private final String TYPE_TEXT = "text";

        private String id;
        private String caption;

        private HandleHTTP handleHTTP;
        private DBHelper dbHelper;
        private SQLiteDatabase db;
        private int level = 1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lockRotation();
            pbDownloadTimetable.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            String type = params[0];
            id = params[1];
            caption = params[2];
            switch (type) {
                case ITEMS_FROM_DB:
                    timetable = getTimetableFromDB(id);
                case ITEMS_FROM_INTERNET:
                    timetable = getTimetableFromSite(id);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (timetable != null && timetable.getItems().size() > 0){
                createTree(timetable.getItems());
                timetable.setId(id);
                timetable.setCaption(caption);
                timetable.setParent_id(getParentId());
            } else {
                //no information
                ImageView ivNoInternet = (ImageView) getActivity().findViewById(R.id.ivNoInternet);
                ivNoInternet.setImageResource(R.drawable.empty_faculty);
                ivNoInternet.setVisibility(View.VISIBLE);
                TextView tvNoInternet = (TextView) getActivity().findViewById(R.id.tvNoInternet);
                tvNoInternet.setText(getString(R.string.emptyFaculty));
                tvNoInternet.setVisibility(View.VISIBLE);
                noData = true;
            }
            pbDownloadTimetable.setVisibility(View.INVISIBLE);
            unlockRotation();
        }

        private Timetable getTimetableFromDB(String id) {
            Timetable timetable;
            dbHelper = new DBHelper(getActivity().getApplicationContext());
            JSONArray jsonArray = null;
            db = dbHelper.getReadableDatabase();

            Cursor c = db.rawQuery("select * from Timetables where _id = ?", new String[]{id});

            //if result from DB isn`t empty
            if (c.moveToFirst()) {
                timetable = new Timetable();
                //index of columns
                int idColIndex = c.getColumnIndex(DBHelper.ID);
                int captionColIndex = c.getColumnIndex(DBHelper.CAPTION);
                int dataColIndex = c.getColumnIndex(DBHelper.DATA);
                int parentIdColIndex = c.getColumnIndex(DBHelper.PARENT_ID);

                timetable.setId(c.getString(idColIndex));
                timetable.setData(c.getString(dataColIndex));
                timetable.setCaption(c.getString(captionColIndex));
                timetable.setParent_id(c.getString(parentIdColIndex));

                try {
                    jsonArray = new JSONArray(c.getString(dataColIndex));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                timetable.setItems(fillData(jsonArray));

                c.close();
                db.close();
                return timetable;
            }
            c.close();
            db.close();
            return null;
        }

        private Timetable getTimetableFromSite(String id) {
            Timetable timetable;
            handleHTTP = new HandleHTTP();

            String jSONstr = handleHTTP.makeRequest(GET + id);
            if (jSONstr != null && !jSONstr.isEmpty()) {
                try {
                    timetable = new Timetable();
                    timetable.setItems(fillData(new JSONArray(jSONstr)));
                    timetable.setData(jSONstr);
                    return timetable;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        private ArrayList<TimetableItem> fillData(JSONArray jsonArray) {
            ArrayList<TimetableItem> timetableItems = new ArrayList<>();
            TimetableItem item;
            try {
                JSONArray timeJsonArray = jsonArray;
                for (int i = 0; i < timeJsonArray.length(); i++) {
                    //parsing JSON object
                    JSONObject oneItem = timeJsonArray.getJSONObject(i);
                    int id = Integer.parseInt(oneItem.getString(TAG_ID));
                    int itemType = getType(oneItem.getString(TAG_ITEM_TYPE));
                    String caption = oneItem.getString(TAG_CAPTION);
                    String description = oneItem.getString(TAG_DESCRIPTION);

                    //change date format
                    String postDate = oneItem.getString(TAG_POST_DATE);
//                    try {
//                        DateFormat originalDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm");
//                        DateFormat targetDateFormat = new SimpleDateFormat("E dd MMM kk:mm");
//                        Date date = originalDateFormat.parse(postDate);
//                        postDate = targetDateFormat.format(date);
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }

                    Integer hits = Integer.parseInt(oneItem.getString(TAG_HITS));
                    JSONArray items = oneItem.getJSONArray(TAG_ITEMS);

                    //get children with recursion
                    if ((level == 1) && (itemType == 0) && (items.length() == 0)) {
                        continue;
                    } else if (items.length() != 0) {
                        level++;
                        ArrayList<TimetableItem> groupItems = fillData(items);
                        item = new TimetableItem(id, itemType, caption, description, postDate, hits, groupItems);
                        level = 1;
                        timetableItems.add(item);
                    } else {
                        item = new TimetableItem(id, itemType, caption, description, postDate, hits);
                        timetableItems.add(item);
                    }
                }
                return timetableItems;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        private void lockRotation(){
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
            else {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }
        }

        private void unlockRotation(){
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        //"folder" to 0, "file" to 1, etc.
        private int getType(String type) {
            switch (type) {
                case TYPE_FOLDER:
                    return 0;
                case TYPE_FILE:
                    return 1;
                case TYPE_LINK:
                    return 2;
                case TYPE_TEXT:
                    return 3;
                default:
                    return 0;
            }
        }
    }

    // Getters and setters
    public String getMainId() {
        return mainId;
    }

    public void setMainId(String mainId) {
        this.mainId = mainId;
    }

    public String getMainTitle() {
        return mainTitle;
    }

    public void setMainTitle(String mainTitle) {
        this.mainTitle = mainTitle;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentTitle() {
        return parentTitle;
    }

    public void setParentTitle(String parentTitle) {
        this.parentTitle = parentTitle;
    }

    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
    }

    public void setRotated(boolean rotated) {
        this.rotated = rotated;
    }
}