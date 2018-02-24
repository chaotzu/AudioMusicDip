package org.netzd.audiomusicdip;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Alumno12 on 23/02/18.
 */

public class DialogWarning extends android.support.v4.app.DialogFragment {

    private static final String ARG_TITLE="Titulo";
    private static final String ARG_MESSAGE="Mensaje";
    private static final String ARG_CANCELABLE="isCancelable";
    private static final String ARG_TITLE_BUTTON_LEFT="TituloIzquierdo";
    private static final String ARG_TITLE_BUTTON_RIGHT="TituloDerecho";

    private TextView messageTextView = null;

    private OnDialogWarningListener onDialogWarningListener = null;

    public static DialogWarning newInstance(String title, String message, boolean isCancelable){
        DialogWarning dialogWarning = new DialogWarning();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putBoolean(ARG_CANCELABLE, isCancelable);
        dialogWarning.setArguments(args);
        return dialogWarning;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(getArguments().getBoolean(ARG_CANCELABLE));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = null;
        String message = null;
        boolean isCancelable = false;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        if(getArguments()!=null){
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_warning,null);
            title = getArguments().getString(ARG_TITLE);
            message = getArguments().getString(ARG_MESSAGE);

            messageTextView = dialogView.findViewById(R.id.messageDialogTextView);
            messageTextView.setText(message);

            dialogBuilder.setTitle(title);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(onDialogWarningListener != null){
                        onDialogWarningListener.onAccept(getDialog());
                    }else{
                        dismiss();
                    }
                }
            });

            dialogBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(onDialogWarningListener != null){
                        onDialogWarningListener.onCancel(getDialog());
                    }else{
                        dismiss();
                    }
                }
            });

            setCancelable(isCancelable);
        }
        return dialogBuilder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if(onDialogWarningListener!=null)
            onDialogWarningListener.onCancel(getDialog());
    }

    public void setOnDialogWarningListener(OnDialogWarningListener onDialogWarningListener) {
        this.onDialogWarningListener = onDialogWarningListener;
    }
}
