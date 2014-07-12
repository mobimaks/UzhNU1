package ua.elitasoftware.UzhNU;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;

public class DownloadDialog extends DialogFragment implements OnClickListener {

    private final String DIALOG_TAG = "download";
    private int type;
    private int id;
    private String caption;
    private String description;

    public DownloadDialog(FragmentManager fragmentManager, int id, String caption, int type) {
        this.type = type;
        this.id = id;
        this.caption = caption;
        this.show(fragmentManager, "download");
    }

    public DownloadDialog(FragmentManager fragmentManager, String caption, String description, int type) {
        this.type = type;
        this.caption = caption;
        this.description = description;
        this.show(fragmentManager, DIALOG_TAG);
    }

    public DownloadDialog(FragmentManager fragmentManager, String caption, int type) {
        this.type = type;
        this.caption = caption;
        this.show(fragmentManager, DIALOG_TAG);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        switch (type) {
            case TimetablesFragment.TYPE_FILE:
                builder.setMessage(String.format(getString(R.string.dialogDownload), caption))
                       .setIcon(R.drawable.file)
                       .setNegativeButton(getString(R.string.dialogBtnNo), null)
                       .setPositiveButton(getString(R.string.dialogBtnYes), this);
                break;
            case TimetablesFragment.TYPE_LINK:
                String goTo = String.format(getString(R.string.dialogGoTo), caption);
                builder.setMessage(Html.fromHtml(goTo +
                        "<a href=\"" + description + "\">" + description + "</a>"))
                        .setIcon(R.drawable.link)
                        .setNegativeButton(getString(R.string.dialogBtnNo), null)
                        .setPositiveButton(getString(R.string.dialogBtnYes), this);
                break;
            case TimetablesFragment.TYPE_TEXT:
                builder.setMessage(caption)
                        .setIcon(R.drawable.info)
                        .setNeutralButton(getString(R.string.dialogBtnOk), this);
                break;
            case MainActivity.TYPE_APP_UPD:
                builder.setMessage(caption)
                       .setIcon(R.drawable.ic_launcher)
                       .setNegativeButton(getString(R.string.dialogBtnNo), null)
                       .setPositiveButton(getString(R.string.dialogBtnYes), this);
                break;

        }
        setRetainInstance(true);
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (type == TimetablesFragment.TYPE_FILE) {
                downloadFile(TimetablesFragment.GET_FILE + id, caption, null);
            } else if (type == TimetablesFragment.TYPE_LINK) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(description));
                startActivity(intent);
            } else if (type == MainActivity.TYPE_APP_UPD) {
                downloadFile(getString(R.string.appUpdUrl), getString(R.string.app_name) + "_", "apk");
            }
        }
        dismiss();
    }

    //start download service
    private void downloadFile(String url, String caption, String extension) {
        Intent intent = new Intent(getActivity(), DownloadService.class);
        intent.putExtra(TimetablesFragment.FILE, url);
        intent.putExtra(TimetablesFragment.FILE_NAME, caption);
        intent.putExtra(TimetablesFragment.FILE_EXTENSION, extension);
        getActivity().startService(intent);
    }
}
