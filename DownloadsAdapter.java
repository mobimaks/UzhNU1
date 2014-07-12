package ua.elitasoftware.UzhNU;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;

public class DownloadsAdapter extends BaseAdapter {

    private final int TYPE_FOLDER = 0;
    private final int TYPE_WORD = 1;
    private final int TYPE_EXCEL = 2;
    private final int TYPE_POINT = 3;
    private final int TYPE_PDF = 4;
    private final int TYPE_ZIP = 5;
    private final int TYPE_TXT = 6;
    private final int TYPE_IMAGE = 7;
    private final int TYPE_OTHER = 8;

    private Context c;
    private File[] files;
    private LayoutInflater inflater;
    private SparseBooleanArray selectedItemsId;

    public DownloadsAdapter(Context context, File[] files) {
        this.c = context;
        this.files = files;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedItemsId = new SparseBooleanArray();
    }

    public void addSelection(int position){
        selectedItemsId.put(position, true);
        notifyDataSetChanged();
    }

    public void removeSelection(int position){
        selectedItemsId.delete(position);
        notifyDataSetChanged();
    }

    public void clearSelection(){
        selectedItemsId.clear();
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedItemsId() {
        return selectedItemsId;
    }

    public void setSelectedItemsId(SparseBooleanArray selectedItemsId) {
        this.selectedItemsId = selectedItemsId;
    }

    public int selectedItemsCount(){
        return selectedItemsId.size();
    }

    @Override
    public int getCount() {
        return files.length;
    }

    @Override
    public Object getItem(int position) {
        return files[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.downloads_item, parent, false);
            holder.ivItemIcon = (ImageView) convertView.findViewById(R.id.ivItemIcon);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvItemName);
            holder.tvSize = (TextView) convertView.findViewById(R.id.tvItemSize);
            holder.tvDate = (TextView) convertView.findViewById(R.id.tvItemDate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (selectedItemsId.get(position)){
            convertView.setBackgroundColor(c.getResources().getColor(R.color.dark2_gray));
        } else {
            convertView.setBackgroundColor(c.getResources().getColor(android.R.color.transparent));
        }


        File file = (File) getItem(position);
        if (file.isDirectory()) {
            holder.ivItemIcon.setImageResource(R.drawable.format_folder);
            holder.tvName.setText(file.getName());
            holder.tvSize.setText(file.listFiles().length + " елементів");
        } else {
            String fileName = file.getName();
            holder.ivItemIcon.setImageDrawable(getFileIcon(fileName));
            holder.tvName.setText(fileName);
            //TODO: add MB, GB
            holder.tvSize.setText(file.length() / 1024 + " КБ");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("E dd MMM kk:mm");
        holder.tvDate.setText(dateFormat.format(file.lastModified()));

        return convertView;
    }

    private Integer getFileType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (extension.equals("doc") || extension.equals("docx")) {
            return TYPE_WORD;
        } else if (extension.equals("xls") || extension.equals("xlsx")) {
            return TYPE_EXCEL;
        } else if (extension.equals("pdf")) {
            return TYPE_PDF;
        } else if (extension.equals("ppt") || extension.equals("pptx")) {
            return TYPE_POINT;
        } else if (extension.equals("zip") || extension.equals("rar")) {
            return TYPE_ZIP;
        } else if (extension.equals("txt")) {
            return TYPE_TXT;
        } else if (extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png") || extension.equals("gif") || extension.equals("bmp")) {
            return TYPE_IMAGE;
        } else return TYPE_OTHER;
    }

    private Drawable getFileIcon(String fileName) {
        Integer fileType = getFileType(fileName);
        switch (fileType) {
            case TYPE_WORD:
                return c.getResources().getDrawable(R.drawable.format_word);
            case TYPE_EXCEL:
                return c.getResources().getDrawable(R.drawable.format_excel);
            case TYPE_PDF:
                return c.getResources().getDrawable(R.drawable.format_pdf);
            case TYPE_POINT:
                return c.getResources().getDrawable(R.drawable.format_ppt);
            case TYPE_ZIP:
                return c.getResources().getDrawable(R.drawable.format_zip);
            case TYPE_TXT:
                return c.getResources().getDrawable(R.drawable.format_text);
            case TYPE_IMAGE:
                return c.getResources().getDrawable(R.drawable.format_picture);
            default:
                return c.getResources().getDrawable(R.drawable.format_unknown);
        }

    }

    static class ViewHolder {
        public ImageView ivItemIcon;
        public TextView tvName;
        public TextView tvDate;
        public TextView tvSize;
    }
}
