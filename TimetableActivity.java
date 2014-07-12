package ua.elitasoftware.UzhNU;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.Toast;
import ua.elitasoftware.UzhNU.TimetablesFragment.OnChildSelect;
import ua.elitasoftware.UzhNU.TimetablesFragment.OnGroupSelect;

public class TimetableActivity extends DrawerActivity implements OnChildSelect, OnGroupSelect {

    private FragmentManager manager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable);
        setDrawer();

        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);

        if (id != 0){
            String title = intent.getStringExtra("title");
            TimetablesFragment timetablesFragment = (TimetablesFragment) getFragmentManager().findFragmentById(R.id.frTimetable);
            timetablesFragment.openFolder(String.valueOf(id), title, null, null);
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        manager =  getFragmentManager();
        TimetablesFragment timetablesFragment = (TimetablesFragment)manager.findFragmentById(R.id.frTimetable);
        if (timetablesFragment.getParentId() == null ||
                timetablesFragment.getParentId().equals(timetablesFragment.getMainId())) {
            finish();
        } else {
            timetablesFragment.setTimetable(null);
            timetablesFragment.setRotated(false);
            timetablesFragment.openFolder(timetablesFragment.getParentId(), timetablesFragment.getParentTitle(),null, null);
        }

    }

    @Override
    public void onChildSelect(TimetableItem item) {
        //press on folder/file/link/text
        int type = item.getItemType();
        manager = getFragmentManager();
        TimetablesFragment timetablesFragment = (TimetablesFragment)manager.findFragmentById(R.id.frTimetable);
        switch (type){
            case TimetablesFragment.TYPE_FOLDER:
                int itemID = item.getId();
                String title = item.getCaption();
                timetablesFragment.setTimetable(null);
                timetablesFragment.openFolder(String.valueOf(itemID), title, timetablesFragment.getMainId(), timetablesFragment.getMainTitle());
                break;
            case TimetablesFragment.TYPE_FILE:
                //AlertDialog
                timetablesFragment.showDialog(item.getId(), item.getCaption());
                break;
            case TimetablesFragment.TYPE_LINK:
                timetablesFragment.showDialog(item.getCaption(), item.getDescription());
                break;
            case TimetablesFragment.TYPE_TEXT:
                timetablesFragment.showDialog(item.getCaption());
                break;
            default:
                Toast.makeText(this, item.getCaption(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onGroupSelect(ExpandableListView parent, int groupPosition, TimetableItem item) {
        int type = item.getItemType();
        manager = getFragmentManager();
        TimetablesFragment timetablesFragment = (TimetablesFragment)manager.findFragmentById(R.id.frTimetable);
        switch (type){
            case TimetablesFragment.TYPE_FOLDER:
                if (parent.isGroupExpanded(groupPosition)){
                    parent.collapseGroup(groupPosition);
                } else {
                    parent.expandGroup(groupPosition);
                }
                break;
            case TimetablesFragment.TYPE_FILE:
                //AlertDialog
                timetablesFragment.showDialog(item.getId(), item.getCaption());
                break;
            case TimetablesFragment.TYPE_LINK:
                timetablesFragment.showDialog(item.getCaption(), item.getDescription());
                break;
            case TimetablesFragment.TYPE_TEXT:
                timetablesFragment.showDialog(item.getCaption());
                break;
        }
    }
}
