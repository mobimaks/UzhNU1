package ua.elitasoftware.UzhNU;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FacultiesAdapter extends BaseAdapter {

    private Context c;
    private LayoutInflater inflater;
    private ArrayList<Faculty> data;

    public FacultiesAdapter(Context c, ArrayList<Faculty> data) {
        this.c = c;
        this.data = data;
        //ініціалізуєм LayoutInflater
        this.inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Faculty faculty = data.get(position);
        View view = convertView;
        if (view == null)
            view = inflater.inflate(R.layout.faculty_item, parent, false);

        ((TextView)view.findViewById(R.id.tvFacultyName)).setText(faculty.getName());
        return view;
    }
}





























