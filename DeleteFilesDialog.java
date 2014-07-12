package ua.elitasoftware.UzhNU;

import android.app.*;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.View;
import android.widget.AbsListView;

/**
 * Created by mobimaks on 30.06.2014.
 */
public class DeleteFilesDialog extends DialogFragment implements OnClickListener{

    private int[] selectedItemsId;
    private ActionMode mode;

    public interface DeleteFilesDialogResult{
        void onDeleteFilesDialogResult(boolean dialogOk, int[] selectedItemsId, ActionMode mode);
    }

    public DeleteFilesDialog(FragmentManager manager, int[] selectedItemsId, ActionMode mode){
        this.selectedItemsId = selectedItemsId;
        this.mode = mode;
        this.show(manager, "delete");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getQuantityString(R.plurals.deletingFilesDialog, selectedItemsId.length))
               .setNeutralButton(getString(R.string.dialogBtnNo), this)
               .setPositiveButton(getString(R.string.dialogBtnYes), this);
        setRetainInstance(true);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        DeleteFilesDialogResult listener = (DeleteFilesDialogResult) getActivity();
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                listener.onDeleteFilesDialogResult(true, selectedItemsId, mode);
                break;
            default:
                listener.onDeleteFilesDialogResult(false, selectedItemsId, mode);
                break;
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
