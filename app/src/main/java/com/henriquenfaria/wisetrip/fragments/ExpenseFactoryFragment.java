package com.henriquenfaria.wisetrip.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.models.ExpenseModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.CountryPickerListener;

import org.joda.time.DateTime;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ExpenseFactoryFragment extends BaseFragment implements
        DatePickerDialogFragment.OnDateSetListener,
        AlertDialogFragment.OnAlertListener,
        CountryPickerListener {

    private static final String ARG_EXPENSE = "arg_expense";
    private static final String ARG_TRIP = "arg_trip";

    private static final String TAG_DATE_PICKER_FRAGMENT = "tag_date_picker_fragment";
    private static final String TAG_COUNTRY_PICKER_FRAGMENT = "tag_country_picker_fragment";
    private static final String TAG_ALERT_DIALOG_FRAGMENT = "tag_alert_dialog_fragment";
    private static final String SAVE_TRIP = "save_trip";
    private static final String SAVE_EXPENSE = "save_expense";
    private static final String SAVE_IS_EDIT_MODE = "save_is_edit_mode";

    @BindView(R.id.title_edit_text)
    protected EditText mExpenseTitleEditText;
    @BindView(R.id.date_text)
    protected TextView mDateTextView;
    @BindView(R.id.currency_text)
    protected TextView mCurrencyTextView;
    @BindView(R.id.amount_edit_text)
    protected EditText mAmountEditText;
    @BindView(R.id.currency_icon)
    protected ImageView mCurrencyIcon;

    private TripModel mTrip;
    private ExpenseModel mExpense;
    private boolean mIsEditMode;
    private CountryPicker mCountryPicker;
    private DatePickerDialogFragment mDatePickerFragment;
    private AlertDialogFragment mAlertDialogFragment;

    private OnExpenseFactoryListener mListener;

    private View.OnClickListener mOnCurrencyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            mCountryPicker = CountryPicker.newInstance(getString(R.string.select_country));
            mCountryPicker.setListener(ExpenseFactoryFragment.this);
            mCountryPicker.show(getFragmentManager(), TAG_COUNTRY_PICKER_FRAGMENT);
        }
    };

    private View.OnClickListener mOnDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            mDatePickerFragment = new DatePickerDialogFragment();
            mDatePickerFragment.setOnDateSetListener(ExpenseFactoryFragment.this);
            mDatePickerFragment.setCurrentDate(mExpense.getDate());
            mDatePickerFragment.setTargetViewId(v.getId());
            mDatePickerFragment.show(getFragmentManager(), TAG_DATE_PICKER_FRAGMENT);
        }
    };

    private TextWatcher mExpenseTitleTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null) {
                mExpense.setTitle(s.toString());
            }
        }
    };

    public ExpenseFactoryFragment() {
        // Required empty public constructor
    }

    // Create new Fragment instance with TripModel info
    public static ExpenseFactoryFragment newInstance(TripModel trip, ExpenseModel expense) {
        ExpenseFactoryFragment fragment = new ExpenseFactoryFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRIP,  trip);
        args.putParcelable(ARG_EXPENSE, expense);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_TRIP, mTrip);
        outState.putParcelable(SAVE_EXPENSE, mExpense);
        outState.putBoolean(SAVE_IS_EDIT_MODE, mIsEditMode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTrip = getArguments().getParcelable(ARG_TRIP);
            mExpense = getArguments().getParcelable(ARG_EXPENSE);

            if (mExpense == null) {
                mExpense = new ExpenseModel();
                mExpense.setDate(DateTime.now().withTimeAtStartOfDay().getMillis());
                String country = Utils.getStringFromSharedPrefs(mFragmentActivity,
                        Constants.Preference.PREFERENCE_DEFAULT_COUNTRY);
                mExpense.setCountry(country);
            }

            if (!TextUtils.isEmpty(mExpense.getId())) {
                mIsEditMode = true;
            }
        }

        mDatePickerFragment = (DatePickerDialogFragment)
                getFragmentManager().findFragmentByTag(TAG_DATE_PICKER_FRAGMENT);
        if (mDatePickerFragment != null) {
            mDatePickerFragment.setOnDateSetListener(this);
        }

        mAlertDialogFragment = (AlertDialogFragment)
                getFragmentManager().findFragmentByTag(TAG_ALERT_DIALOG_FRAGMENT);
        if (mAlertDialogFragment != null) {
            mAlertDialogFragment.setOnAlertListener(this);
        }

        mCountryPicker = (CountryPicker)
                getFragmentManager().findFragmentByTag(TAG_COUNTRY_PICKER_FRAGMENT);
        if (mCountryPicker != null) {
            mCountryPicker.setListener(this);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_trip_factory_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mIsEditMode) {
            menu.findItem(R.id.action_delete).setVisible(true);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveExpense();
            return true;
        } else if (id == R.id.action_delete) {
            createDeleteTripConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnExpenseFactoryListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnExpenseFactoryListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {

        // Restore instances
        if (savedInstanceState != null) {
            mTrip = savedInstanceState.getParcelable(SAVE_TRIP);
            mExpense = savedInstanceState.getParcelable(SAVE_EXPENSE);
            mIsEditMode = savedInstanceState.getBoolean(SAVE_IS_EDIT_MODE);
        }

        View rootView = inflater.inflate(R.layout.fragment_expense_factory, container, false);
        ButterKnife.bind(this, rootView);
        mListener.changeActionBarTitle(getString(R.string.create_new_expense));

        mExpenseTitleEditText.addTextChangedListener(mExpenseTitleTextWatcher);
        mDateTextView.setOnClickListener(mOnDateClickListener);

        mAmountEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void afterTextChanged(Editable arg0) {
                String str = mAmountEditText.getText().toString();

                if (TextUtils.isEmpty(str)){
                    mExpense.setAmount(0d);
                    return;
                }

                String str2 = Utils.getRestrictedDecimal(str,
                        Constants.General.MAX_DIGITS_BEFORE_POINT,
                        Constants.General.MAX_DECIMAL_DIGITS);

                if (!str2.equals(str)) {
                    mAmountEditText.setText(str2);
                    int pos = mAmountEditText.getText().length();
                    mAmountEditText.setSelection(pos);
                }

                try {
                    mExpense.setAmount(Double.parseDouble(str2));
                } catch (NumberFormatException e) {
                    Timber.e("NumberFormatException while parsing expense amount");
                    e.printStackTrace();
                    mExpense.setAmount(0d);
                }
            }
        });

        mCurrencyTextView.setOnClickListener(mOnCurrencyClickListener);

        populateFormFields();

        return rootView;
    }

    private void populateFormFields() {
        mExpenseTitleEditText.setText(mExpense.getTitle());
        mDateTextView.setText(Utils.getFormattedExpenseDateText(mExpense.getDate()));
        Country country = new Country();
        country.setCode(mExpense.getCountry());
        country.loadFlagByCode(mFragmentActivity);
        mCurrencyIcon.setImageResource(country.getFlag());
        mCurrencyTextView.setText(Utils.getCurrencySymbol(mExpense.getCountry()));
        mAmountEditText.setText(String.valueOf(mExpense.getAmount()));
    }

    private boolean isValidFormFields() {
        boolean isValid = true;

        //TODO: Is title mandatory?
        if (TextUtils.isEmpty(mExpense.getTitle())) {
            mExpenseTitleEditText.setError(getString(R.string.mandatory_field));
            isValid = false;
        }

        if (mExpense.getAmount() == null || mExpense.getAmount() <= 0d) {
            mAmountEditText.setError(getString(R.string.mandatory_field));
            isValid = false;
        }

        if (mExpense.getDate() <= 0) {
            mDateTextView.setError(getString(R.string.mandatory_field));
            isValid = false;
        }

        return isValid;
    }


    private void saveExpense() {
        if (isValidFormFields()) {
            mListener.saveExpense(mTrip, mExpense, mIsEditMode);
        }
    }

    private void deleteExpense() {
        mListener.deleteExpense(mTrip, mExpense);
    }

    private void createDeleteTripConfirmationDialog() {
        mAlertDialogFragment = new AlertDialogFragment();
        mAlertDialogFragment.setTitle(R.string.title_delete_trip);
        mAlertDialogFragment.setMessage(R.string.message_delete_trip);
        mAlertDialogFragment.setOnAlertListener(this);
        mAlertDialogFragment.show(getFragmentManager(), TAG_ALERT_DIALOG_FRAGMENT);
    }


    @Override
    public void onDateSet(int targetViewId, long dateMillis) {
        if (mDateTextView.getId() == targetViewId) {
            mExpense.setDate(new DateTime(dateMillis).withTimeAtStartOfDay().getMillis());
            mDateTextView.setText(Utils.getFormattedExpenseDateText(dateMillis));
            mDateTextView.setError(null);
        }
    }

    @Override
    public void positiveAlertButtonClicked() {
        deleteExpense();
    }

    @Override
    public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {
        if (mCountryPicker != null && !TextUtils.isEmpty(code)) {
            String currency = Utils.getCurrencySymbol(code);
            if (!TextUtils.isEmpty(currency)) {
                Utils.saveStringToSharedPrefs(mFragmentActivity,
                        Constants.Preference.PREFERENCE_DEFAULT_COUNTRY, code);
                mExpense.setCountry(code);
                if (flagDrawableResID > 0) {
                    mCurrencyIcon.setImageResource(flagDrawableResID);
                }
                mCurrencyTextView.setText(Utils.getCurrencySymbol(code));
            }

            mCountryPicker.dismiss();
        }
    }

    public interface OnExpenseFactoryListener {
        void changeActionBarTitle(String newTitle);

        void saveExpense(TripModel trip, ExpenseModel expense, boolean isEditMode);

        void deleteExpense(TripModel trip, ExpenseModel expense);
    }

}
