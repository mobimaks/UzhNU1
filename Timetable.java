package ua.elitasoftware.UzhNU;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Timetable implements Parcelable {
    @SuppressWarnings("unused")
    public static final Creator<Timetable> CREATOR = new Creator<Timetable>() {
        @Override
        public Timetable createFromParcel(Parcel in) {
            return new Timetable(in);
        }

        @Override
        public Timetable[] newArray(int size) {
            return new Timetable[size];
        }
    };
    private String id;
    private String caption;
    private String data;
    private String parent_id;
    private ArrayList<TimetableItem> items;

    public Timetable() {
    }

    public Timetable(String id, String caption, String data, String parent_id, ArrayList<TimetableItem> items) {
        this.id = id;
        this.caption = caption;
        this.data = data;
        this.parent_id = parent_id;
        this.items = items;
    }

    protected Timetable(Parcel in) {
        id = in.readString();
        caption = in.readString();
        data = in.readString();
        parent_id = in.readString();
        if (in.readByte() == 0x01) {
            items = new ArrayList<TimetableItem>();
            in.readList(items, TimetableItem.class.getClassLoader());
        } else {
            items = null;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public ArrayList<TimetableItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<TimetableItem> items) {
        this.items = items;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(caption);
        dest.writeString(data);
        dest.writeString(parent_id);
        if (items == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(items);
        }
    }
}