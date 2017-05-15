package com.henriquenfaria.wisetrip.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;


public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private static final String SAVE_TARGET_VIEW_ID = "save_target_view_id";

    private OnDateSetListener mListener;
    private int mTargetViewId;

    public void setOnDateSetListener(OnDateSetListener listener) {
        mListener = listener;
    }

    public void setTargetViewId(int targetViewId) {
        mTargetViewId = targetViewId;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_TARGET_VIEW_ID, mTargetViewId);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mTargetViewId = savedInstanceState.getInt(SAVE_TARGET_VIEW_ID);
        }

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        if (mListener != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
            mListener.onDateSet(mTargetViewId, calendar.getTimeInMillis(), df.format(calendar
                    .getTime()));
        }
    }

    public interface OnDateSetListener {
        void onDateSet(int targetViewId, long dateMillis, String dateText);
    }
}
