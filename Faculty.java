package ua.elitasoftware.UzhNU;

import android.os.Parcel;
import android.os.Parcelable;

public class Faculty implements Parcelable {

    public static Creator<Faculty> CREATOR = new Creator<Faculty>() {
        public Faculty createFromParcel(Parcel source) {
            return new Faculty(source);
        }

        public Faculty[] newArray(int size) {
            return new Faculty[size];
        }
    };
    private Integer id;
    private String name;

    public Faculty(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    private Faculty(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.name = in.readString();
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.name);
    }
}
