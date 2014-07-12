package ua.elitasoftware.UzhNU;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class FacultiesFragment extends Fragment implements OnItemClickListener {

    //    private String URL_FACULTIES_REQUEST = "http://mobimaks.ucoz.ru/faculty.txt";
    private final String KEY_FACULTIES_ARRAY = "faculties";
    //JSON codes
    private final String TAG_ID = "id";
    private final String TAG_CAPTION = "caption";
    private ListView lvFacultiesList;
    private ProgressBar progressBar;
    private FacultiesAdapter adapter;
    private ArrayList<Faculty> faculties;
    private boolean noInternet = false;
    private boolean noData = false;
    private FacultyRequest task;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_faculties, container, false);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (task != null){
            task.cancel(true);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.pbDownloadFaculties);

        lvFacultiesList = (ListView) getActivity().findViewById(R.id.lvFacultiesList);
        lvFacultiesList.setOnItemClickListener(this);

        if (savedInstanceState == null) {
            task = new FacultyRequest();
            task.execute(getString(R.string.facultiesUrl));
        } else {
            faculties = savedInstanceState.getParcelableArrayList("list");
            //get data on rotation
            if (faculties != null) {
                adapter = new FacultiesAdapter(getActivity(), faculties);
                lvFacultiesList.setAdapter(adapter);
            } else {
                //no data
                ImageView ivNoInternet = (ImageView) getActivity().findViewById(R.id.ivNoInternet);
                ivNoInternet.setImageResource(R.drawable.empty_faculty);
                ivNoInternet.setVisibility(View.VISIBLE);
                TextView tvNoInternet = (TextView) getActivity().findViewById(R.id.tvNoInternet);
                tvNoInternet.setText(getString(R.string.emptyFaculty));
                tvNoInternet.setVisibility(View.VISIBLE);
            }
//
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("list", faculties);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OnSelectedFaculty listener = (OnSelectedFaculty) getActivity();
        listener.onSelectFaculty(parent, view, position, faculties.get(position));
    }

    public interface OnSelectedFaculty {
        void onSelectFaculty(AdapterView<?> parent, View view, int position, Faculty faculty);
    }

    /**
     * AsyncTask
     * TODO: retain AsyncTask during rotation
     */
    class FacultyRequest extends AsyncTask<String, Void, Void> {

        private HandleHTTP handleHTTP;
        private boolean noData = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            lockRotation();
        }

        @Override
        protected Void doInBackground(String... params) {
            handleHTTP = new HandleHTTP();
            String jSONstr;
            if (hasInternet()) {
                jSONstr = handleHTTP.makeRequest(params[0]);
                if (jSONstr != null) {
                    faculties = fillData(jSONstr);
                    if (faculties == null || faculties.size() == 0){
                        noData = true;
                    }
                } else{
                    noData = true;
                }
            } else {
                noData = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!noData){
                adapter = new FacultiesAdapter(getActivity(), faculties);
                lvFacultiesList.setAdapter(adapter);
            } else {
                ImageView ivNoInternet = (ImageView) getActivity().findViewById(R.id.ivNoInternet);
                TextView tvNoInternet = (TextView) getActivity().findViewById(R.id.tvNoInternet);
                ivNoInternet.setVisibility(View.VISIBLE);
                tvNoInternet.setVisibility(View.VISIBLE);
            }
            progressBar.setVisibility(View.INVISIBLE);
            unlockRotation();
        }

        private ArrayList<Faculty> fillData(String jSONstr) {
            ArrayList<Faculty> faculties = new ArrayList<>();
            Faculty faculty;
            try {
                JSONArray dekanatsArray = new JSONArray(jSONstr);
                for (int i = 0; i < dekanatsArray.length(); i++) {
                    JSONObject oneDekanat = dekanatsArray.getJSONObject(i);
                    int id = oneDekanat.getInt(TAG_ID);
                    String name = oneDekanat.getString(TAG_CAPTION);

                    faculty = new Faculty(id, name);

                    faculties.add(faculty);
                }

                //adding faculties to DB
                if (!faculties.isEmpty())
                    addToDB(faculties);
                return faculties;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        private void addToDB(ArrayList<Faculty> faculties) {
            DBHelper dbHelper = new DBHelper(getActivity().getApplicationContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            db.delete(dbHelper.FACULTIES, null, null);
            String sqlInsert = "insert into " + dbHelper.FACULTIES + " (" + dbHelper.ID + ", " + dbHelper.CAPTION + ") values";
            int i;
            for (i = 0; i < faculties.size(); i++) {
                Integer id = faculties.get(i).getId();
                String caption = faculties.get(i).getName().replaceAll("'", "''");
                sqlInsert += " ('" + id + "', '" + caption + "'),";
            }
            sqlInsert = sqlInsert.substring(0, sqlInsert.length() - 1) + ";";
            db.execSQL(sqlInsert);
        }


        private boolean hasInternet() {
            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//            if
//            {
//                return InetAddress.getByName("www.google.com").isReachable(1500);
//            }
            return (activeNetwork != null && activeNetwork.isConnected());
//        return (activeNetwork != null && activeNetwork.isConnected()) ? true : false;
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
    }
}
