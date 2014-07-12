package ua.elitasoftware.UzhNU;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseBooleanArray;
import android.view.*;
import android.webkit.MimeTypeMap;
import android.widget.*;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.OnItemClickListener;
import ua.elitasoftware.UzhNU.System.ParcelableSparseBooleanArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class DownloadsFragment extends Fragment implements OnItemClickListener {

    public static final String ORDER_BY = "sort";
    public static final String FOLDERS_CHECKED_KEY = "foldersChecked";
    //sorting const
    public static final String SORT_BY_DATE = "date";
    public static final String SORT_BY_NAME = "name";
    public static final String SORT_BY_EXTENSION = "extension";
    private static final String SPARSE_BOOL_ARRAY = "selectedItems";
    private File[] files;
    private File currentFolder;
    private DownloadsAdapter adapter;
    private ListView listView;
    private ShareActionProvider shareActionProvider;
    private Intent shareIntent;
    private Bundle savedInstanceState;
    private int foldersChecked = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        if (savedInstanceState != null){
            this.foldersChecked = savedInstanceState.getInt(FOLDERS_CHECKED_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_downloads, container, false);
    }

    public void refresh() {
        openFolder(getCurrentFolder());
    }

    public void openDefaultFolder() {
        openFolder(getMainFolder());
    }

    public void openFolder(File folder) {
        setCurrentFolder(folder);
        files = folder.listFiles();
        //if folder is empty
        if (files == null || files.length == 0) {
//            Toast.makeText(getActivity(), getString(R.string.emptyFolder), Toast.LENGTH_SHORT).show();
            getActivity().finish();
        } else {
            //check Shared Preference for sortBy parameter
            SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
            String sortBy = preferences.getString(ORDER_BY, SORT_BY_NAME);
            //sort files by sortBy parameter

//            long startSort = System.currentTimeMillis();
            sortFiles(files, sortBy);
//            long finishSort = System.currentTimeMillis();
//            Toast.makeText(getActivity(), "Sort time: "+((finishSort-startSort))+" ms", Toast.LENGTH_SHORT).show();

            adapter = new DownloadsAdapter(getActivity().getApplicationContext(), files);
            if (savedInstanceState != null){
                adapter.setSelectedItemsId((SparseBooleanArray) savedInstanceState.getParcelable(SPARSE_BOOL_ARRAY));
            }
            listView = (ListView) getActivity().findViewById(R.id.lvDownloadList);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
            listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(multiChoiceModeListener);
        }
    }

    //TODO: Try sort in AsyncTask
    private void sortFiles(File[] files, final String sortBy) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                if (lhs.isDirectory() && rhs.isFile()) {
                    //return folder first
                    return -1;
                } else if (lhs.isFile() && rhs.isDirectory()) {
                    //return file first
                    return 1;
                } else if ((lhs.isDirectory() && rhs.isDirectory()) || (lhs.isFile() && rhs.isFile())) {
                    //if Files have same type, then sort
                    switch (sortBy) {
                        case SORT_BY_NAME:
                            return lhs.getName().compareTo(rhs.getName());
                        case SORT_BY_DATE:
                            //DESC sorting by date
                            return String.valueOf(rhs.lastModified()).compareTo(String.valueOf(lhs.lastModified()));
                        case SORT_BY_EXTENSION:
                            return extensionSort(lhs, rhs);
                    }
                }
                return 0;
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        SparseBooleanArray selectedItemsId = adapter.getSelectedItemsId();
        outState.putParcelable(SPARSE_BOOL_ARRAY, new ParcelableSparseBooleanArray(selectedItemsId));
        outState.putInt(FOLDERS_CHECKED_KEY, foldersChecked);
        super.onSaveInstanceState(outState);
    }

    private int extensionSort(File lhs, File rhs) {
        String ext1 = lhs.getName();
        String ext2 = rhs.getName();
        if (lhs.isDirectory()) {
            ext1 += ".";
        }
        if (rhs.isDirectory()) {
            ext2 += ".";
        }
        ext1 = ext1.substring(ext1.lastIndexOf("."));
        ext2 = ext2.substring(ext2.lastIndexOf("."));

        int extComparison = ext1.compareTo(ext2);
        if (extComparison != 0)
            return extComparison;
        else
            return lhs.getName().compareTo(rhs.getName());
    }

    /**
     * CAB
     */
    private MultiChoiceModeListener multiChoiceModeListener = new MultiChoiceModeListener() {

        private int num = 0;

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
            int num = adapter.selectedItemsCount();
            mode.setTitle(num + " обрано");

            shareActionProvider = (ShareActionProvider) menu.findItem(R.id.cabShare).getActionProvider();
            if (foldersChecked > 0){
                menu.findItem(R.id.cabShare).setVisible(false);
            }
            return true;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            if (checked){
                adapter.addSelection(position);
            } else {
                adapter.removeSelection(position);
            }
            int num = adapter.selectedItemsCount();
            mode.setTitle(num + getString(R.string.select));

            //folder checked
            if (files[position].isDirectory() && checked){
                foldersChecked++;
            //folder unchecked
            } else if (files[position].isDirectory() && !checked){
                foldersChecked--;
            }

            if (foldersChecked < 1 && num > 0){
                mode.getMenu().findItem(R.id.cabShare).setVisible(true);
                shareFiles(getSelectedFilesId(adapter.getSelectedItemsId()));
            } else {
                mode.getMenu().findItem(R.id.cabShare).setVisible(false);
            }
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            //get ids of selected items
            final int[] selectedItemsId = getSelectedFilesId(adapter.getSelectedItemsId());

            switch (item.getItemId()){
                case R.id.cabDelete:
                    DeleteFilesDialog dialog = new DeleteFilesDialog(getFragmentManager(), selectedItemsId, mode);
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelection();
        }
    };

    protected void delSelectedFiles(int[] selectedItemsId){
        deleteFilesById(selectedItemsId);
        if (!(getCurrentFolder().getPath()+"/").equals(getMainFolderPath()) && getCurrentFolder().list().length == 0){
            openFolder(getCurrentFolder().getParentFile());
        } else {
            refresh();
        }
    }

    private void shareFiles(int[] selectedItemsId) {
        ArrayList<Uri> filesUri = new ArrayList<>();
        for (int i = 0; i < selectedItemsId.length; i++) {
            File file = files[selectedItemsId[i]];
            filesUri.add(Uri.fromFile(file));
        }
        shareIntent = new Intent();
        int num = selectedItemsId.length;
        if (num < 2 && num > 0) {
            String fileName = files[selectedItemsId[0]].getName();
            String fileExtension = fileName.substring(fileName.lastIndexOf(".")+1);
            shareIntent.setType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension));
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, filesUri.get(0));
        } else {
            shareIntent.setType("*/*");
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesUri);
        }
        shareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OnItemClick listener = (OnItemClick) getActivity();
        listener.onItemClick((File) adapter.getItem(position));
    }

    private int[] getSelectedFilesId(SparseBooleanArray selectedItems){
        int[] selectedItemsId = new int[selectedItems.size()];
        for (int i = 0; i < selectedItems.size(); i++){
            selectedItemsId[i] = selectedItems.keyAt(i);
        }
        return selectedItemsId;
    }

    public File getCurrentFolder() {
        return currentFolder;
    }

    public void setCurrentFolder(File currentFolder) {
        this.currentFolder = currentFolder;
    }

    public File getMainFolder() {
        return new File(getMainFolderPath());
    }

    private String getMainFolderPath() {
        String folderName = getResources().getString(R.string.folderName);
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + folderName;
    }

    public interface OnItemClick {
        void onItemClick(File item);
    }

    private void deleteFilesById(int[] ids){
        File file;
        for (int id : ids){
            file = files[id];
            if (file.isDirectory()){
                ((DownloadActivity)getActivity()).removeDirectory(file);
            } else if (file.isFile()) {
                file.delete();
            }
        }
    }
}