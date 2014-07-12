package ua.elitasoftware.UzhNU;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimetablesAdapter extends BaseExpandableListAdapter {

    //Item types const
    private final int TYPE_FOLDER = 0;
    private final int TYPE_FILE = 1;
    private final int TYPE_LINK = 2;
    private final int TYPE_TEXT = 3;

    private Context c;
    private ArrayList<TimetableItem> timetableItems;
    private LayoutInflater inflater;

    public TimetablesAdapter(Context context, ArrayList<TimetableItem> timetableItems) {
        this.c = context;
        this.timetableItems = timetableItems;
        this.inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return timetableItems.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        try {
            return timetableItems.get(groupPosition).getItems().size();
        } catch (NullPointerException e){
            return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return timetableItems.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return timetableItems.get(groupPosition).getItems().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getGroupTypeCount() {
        return 4;
    }

    @Override
    public int getChildTypeCount() {
        return 4;
    }

    @Override
    public int getGroupType(int groupPosition) {
        TimetableItem item = (TimetableItem) getGroup(groupPosition);
        return item.getItemType();
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        TimetableItem item = (TimetableItem) getChild(groupPosition, childPosition);
        return item.getItemType();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TimetableItem item = (TimetableItem) getGroup(groupPosition);
        int type = getGroupType(groupPosition);
        ViewHolder holder = null;
        if (convertView == null){
            holder = new ViewHolder();
            switch (type){
                case TYPE_FOLDER:
                    convertView = inflateFolder(convertView, holder, parent);
                    break;
                case TYPE_FILE:
                    convertView = inflateFile(convertView, holder, parent);
                    break;
                case TYPE_LINK:
                    convertView = inflateLink(convertView, holder, parent);
                    break;
                case TYPE_TEXT:
                    convertView = inflateInfo(convertView, holder, parent);
                    break;
            }
        }

        ViewHolder tag = (ViewHolder)convertView.getTag();
        switch (type){
            case TYPE_FOLDER:
                fillFolder(item, isExpanded, tag, groupPosition);
                break;
            case TYPE_FILE:
                fillFile(item, isExpanded, tag);
                break;
            case TYPE_LINK:
                fillLink(item, isExpanded, tag);
                break;
            case TYPE_TEXT:
                fillText(item, isExpanded, tag);
                break;
        }
        return convertView;
    }

    /**
     * Fill groups items
     * @param item
     * @param isExpanded
     * @param holder
     * @param groupPosition
     */
    private void fillFolder(TimetableItem item, boolean isExpanded, ViewHolder holder, int groupPosition) {
        if (getChildrenCount(groupPosition) == 0) {
            holder.ivFolderIcon.setImageResource(R.drawable.empty);
        } else if (isExpanded){
            holder.ivFolderIcon.setImageResource(R.drawable.open);
        } else if (!isExpanded){
            holder.ivFolderIcon.setImageResource(R.drawable.closed);
        }
        holder.tvFolderName.setText(item.getCaption());
    }

    private void fillFile(TimetableItem item, boolean isExpanded, ViewHolder holder) {
        holder.tvFileName.setText(item.getCaption());
        String fileDate = item.getPostDate();
        highlightDate(holder.tvFileDate, fileDate);
        holder.tvFileDate.setText(fileDate);
//        holder.pbFileDownload.setProgress(66);
        int downloads = (item.getHits());
        holder.tvFileDownload.setText(String.valueOf(downloads));
    }

    private void highlightDate(TextView tvFileDate, String fileDate) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        int interval = Integer.parseInt(preferences.getString(SettingsFragment.DATE_KEY, "7"));
        DateFormat fileDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm");
        Date filePostDate;
        try {
            filePostDate = fileDateFormat.parse(fileDate);
        } catch (ParseException e) {
            filePostDate = new Date(System.currentTimeMillis());
            e.printStackTrace();
        }
        Calendar filePostCalendar = new GregorianCalendar();
        filePostCalendar.setTime(filePostDate);

        Calendar today = Calendar.getInstance();
        today.add(Calendar.DAY_OF_YEAR, -interval);

        boolean isAfter = filePostCalendar.after(today);

        if (isAfter) {
            tvFileDate.setTextColor(c.getResources().getColor(android.R.color.holo_green_dark));
            tvFileDate.setTypeface(null, Typeface.BOLD);
        } else {
            tvFileDate.setTextColor(c.getResources().getColor(android.R.color.darker_gray));
            tvFileDate.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void fillLink(TimetableItem item, boolean isExpanded, ViewHolder holder) {
        holder.tvLinkName.setText(item.getCaption());
    }

    private void fillText(TimetableItem item, boolean isExpanded, ViewHolder holder) {
        holder.tvInfoName.setText(item.getCaption());
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TimetableItem item = (TimetableItem) getChild(groupPosition, childPosition);
        int type = getChildType(groupPosition, childPosition);
        ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            switch (type){
                case TYPE_FOLDER:
                    convertView = inflateFolder(convertView, holder, parent);
                    break;
                case TYPE_FILE:
                    convertView = inflateFile(convertView, holder, parent);
                    break;
                case TYPE_LINK:
                    convertView = inflateLink(convertView, holder, parent);
                    break;
                case TYPE_TEXT:
                    convertView = inflateInfo(convertView, holder, parent);
                    break;
            }
        }
        ViewHolder tag = (ViewHolder) convertView.getTag();
        switch (type){
            case TYPE_FOLDER:
                fillFolder(item, tag);
                break;
            case TYPE_FILE:
                fillFile(item, tag);
                break;
            case TYPE_LINK:
                fillLink(item, tag);
                break;
            case TYPE_TEXT:
                fillText(item, tag);
                break;
        }
        convertView.setPadding(50, 0, 0, 0);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
            convertView.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.selector));
        } else {
            convertView.setBackground(c.getResources().getDrawable(R.drawable.selector));
        }
        return convertView;
    }

    /**
     * Fill child items
     * @param item
     * @param holder
     */
    private void fillFolder(TimetableItem item, ViewHolder holder) {
        holder.ivFolderIcon.setImageResource(R.drawable.empty);
        holder.tvFolderName.setText(item.getCaption());
    }

    private void fillFile(TimetableItem item, ViewHolder holder) {
        holder.tvFileName.setText(item.getCaption());
        String fileDate = item.getPostDate();
        highlightDate(holder.tvFileDate, fileDate);
        holder.tvFileDate.setText(fileDate);
        holder.tvFileDownload.setText(String.valueOf(item.getHits()));
    }

    private void fillLink(TimetableItem item, ViewHolder holder) {
        holder.tvLinkName.setText(item.getCaption());
    }

    private void fillText(TimetableItem item, ViewHolder holder) {
        holder.tvInfoName.setText(item.getCaption());
    }


    /**
     * Inflating different views for list's items
     */
    private View inflateFolder(View convertView, ViewHolder holder, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.folder_item, parent, false);
        holder.ivFolderIcon = (ImageView) convertView.findViewById(R.id.ivFolderIcon);
        holder.tvFolderName = (TextView) convertView.findViewById(R.id.tvFolderName);
        convertView.setTag(holder);
        return convertView;
    }

    private View inflateFile(View convertView, ViewHolder holder, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.file_item, parent, false);
        holder.tvFileName = (TextView) convertView.findViewById(R.id.tvFileName);
        holder.tvFileDate = (TextView) convertView.findViewById(R.id.tvFileDate);
//        holder.pbFileDownload = (ProgressBar)convertView.findViewById(R.id.pbFileDownload);
        holder.tvFileDownload = (TextView)convertView.findViewById(R.id.tvFileDownload);
        convertView.setTag(holder);
        return convertView;
    }

    private View inflateLink(View convertView, ViewHolder holder, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.link_item, parent, false);
        holder.tvLinkName = (TextView)convertView.findViewById(R.id.tvLinkName);
        convertView.setTag(holder);
        return convertView;
    }

    private View inflateInfo(View convertView, ViewHolder holder, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.info_item, parent, false);
        holder.tvInfoName = (TextView)convertView.findViewById(R.id.tvInfoName);
        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    /**
     * Class with views id's
     */
    private static class ViewHolder {
        public ImageView ivFolderIcon;
        public TextView tvFolderName;

        public TextView tvFileName;
        public TextView tvFileDate;
        public TextView tvFileDownload;

        public TextView tvLinkName;

        public TextView tvInfoName;
    }
}
