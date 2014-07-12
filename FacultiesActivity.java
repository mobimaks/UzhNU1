package ua.elitasoftware.UzhNU;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import ua.elitasoftware.UzhNU.FacultiesFragment.OnSelectedFaculty;

public class FacultiesActivity extends DrawerActivity  implements OnSelectedFaculty{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setDrawer();
    }

    @Override
    public void onSelectFaculty(AdapterView<?> parent, View view, int position, Faculty faculty) {
        FragmentManager fragmentManager = getFragmentManager();
        TimetablesFragment timetablesFragment = (TimetablesFragment) fragmentManager.findFragmentById(R.id.frTimetable);
        if (timetablesFragment == null){
            openFaculty(faculty.getId(), faculty.getName());
        }
    }
}
