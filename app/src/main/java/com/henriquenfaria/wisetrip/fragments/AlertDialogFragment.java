package com.henriquenfaria.wisetrip.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.henriquenfaria.wisetrip.R;

// DialogFragment implementation for OK/Cancel dialog
public class AlertDialogFragment extends DialogFragment {

    private static final String SAVE_TITLE = "save_title";
    private static final String SAVE_MESSAGE = "save_message";

    private int mTitle;
    private int mMessage;
    private OnAlertListener mOnAlertListener;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_TITLE, mTitle);
        outState.putInt(SAVE_MESSAGE, mMessage);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getInt(SAVE_TITLE);
            mMessage = savedInstanceState.getInt(SAVE_MESSAGE);
        }

        return new AlertDialog.Builder(getActivity())
                //.setIcon(R.drawable.alert_dialog_icon)
                .setTitle(mTitle)
                .setMessage(mMessage)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (mOnAlertListener != null) {
                                    mOnAlertListener.positiveAlertButtonClicked();
                                }
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismiss();

                            }
                        }
                )
                .create();
    }

    public void setTitle(int title) {
        mTitle = title;
    }

    public void setMessage(int message) {
        mMessage = message;
    }

    public void setOnAlertListener(OnAlertListener listener) {
        mOnAlertListener = listener;
    }

    public interface OnAlertListener {
        void positiveAlertButtonClicked();
    }
}