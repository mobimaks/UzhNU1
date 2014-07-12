package ua.elitasoftware.UzhNU;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class DBHelper extends SQLiteOpenHelper {

    //SQL operations const
    private static final String GET = "get";
    private static final String SET = "set";
    private static final String UPDATE = "upd";
    private static final String DELETE = "del";

    //table fields
    public static final String ID = "_id";
    public static final String CAPTION = "caption";
    public static final String DATA = "data";
    public static final String PARENT_ID = "parent_id";

    //tables name
    public static final String FACULTIES = "faculties";
    public static final String TIMETABLE = "timetables";

    private static final String DATABASE_NAME = "UzhNU.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_FACULTIES_DB = "create table faculties ( _id text primary key, caption text);";
    private static final String CREATE_TIMETABLES_DB = "create table timetables ( _id text primary key, caption text, data text, parent_id text);";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FACULTIES_DB);
        db.execSQL(CREATE_TIMETABLES_DB);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void setFaculties(String[][] faculties){

    }

    public String[][] getFaculties(){
        return null;
    }

    public void setTimetable(String id, String caption, String data, String parentd_ID){
        //знайти такий же, якщо є - замінити, нема - додати
        new DatabaseAsync().execute(SET, id, caption, data, parentd_ID);
    }

    public String[] getTimetable(String id) throws ExecutionException, InterruptedException {
        return new DatabaseAsync().execute(id).get();
    }

    private class DatabaseAsync extends AsyncTask<String, Integer, String[]>{

        private SQLiteDatabase db;

        @Override
        protected String[] doInBackground(String... params) {
            db = getWritableDatabase();
            switch (params[0]){
                case SET:
                    return setToDB(params);
                case GET:
                    return getFromDB(params);
            }
            return null;
        }

        public String[] setToDB(String[] params){
            ContentValues cv = new ContentValues();
            cv.put(ID, params[1]);
            cv.put(CAPTION, params[2]);
            cv.put(DATA, params[3]);
            cv.put(PARENT_ID, params[4]);
            final String id = String.valueOf(db.insert(TIMETABLE, null, cv));
            return new String[]{id};
        }

        public String[] getFromDB(String[] params){
            Cursor c = db.query(TIMETABLE, null, ID, new String[]{params[1]}, null, null, null);
            if (c.moveToFirst()){
                String[] result = new String[4];
                int idNum = c.getColumnIndex(ID);
                int captionNum = c.getColumnIndex(CAPTION);
                int dataNum = c.getColumnIndex(DATA);
                int parentIdNum = c.getColumnIndex(PARENT_ID);

                result[0] = c.getString(idNum);
                result[1] = c.getString(captionNum);
                result[2] = c.getString(dataNum);
                result[3] = c.getString(parentIdNum);
                return result;
            }
            return null;
        }
    }
}
