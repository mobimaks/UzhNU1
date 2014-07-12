package ua.elitasoftware.UzhNU;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class TimetableItem implements Parcelable {

    @SuppressWarnings("unused")
    public static final Creator<TimetableItem> CREATOR = new Creator<TimetableItem>() {
        @Override
        public TimetableItem createFromParcel(Parcel in) {
            return new TimetableItem(in);
        }

        @Override
        public TimetableItem[] newArray(int size) {
            return new TimetableItem[size];
        }
    };
    private int id;
    private int itemType;
    private String caption;
    private String description;
    private String postDate;
    private Integer hits;
    private ArrayList<TimetableItem> items;

    public TimetableItem() {
    }

    public TimetableItem(int id, int itemType, String caption, String description, String postDate, Integer hits, ArrayList<TimetableItem> items) {
        this.id = id;
        this.itemType = itemType;
        this.caption = caption;
        this.description = description;
        this.postDate = postDate;
        this.hits = hits;
        this.items = items;
    }

    public TimetableItem(int id, int itemType, String caption, String description, String postDate, Integer hits) {
        this(id, itemType, caption, description, postDate, hits, null);
    }

    protected TimetableItem(Parcel in) {
        id = in.readInt();
        itemType = in.readInt();
        caption = in.readString();
        description = in.readString();
        postDate = in.readString();
        hits = in.readByte() == 0x00 ? null : in.readInt();
        if (in.readByte() == 0x01) {
            items = new ArrayList<TimetableItem>();
            in.readList(items, TimetableItem.class.getClassLoader());
        } else {
            items = null;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public Integer getHits() {
        return hits;
    }

    public void setHits(Integer hits) {
        this.hits = hits;
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
        dest.writeInt(id);
        dest.writeInt(itemType);
        dest.writeString(caption);
        dest.writeString(description);
        dest.writeString(postDate);
        if (hits == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(hits);
        }
        if (items == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(items);
        }
    }
}
