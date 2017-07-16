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
import com.henriquenfaria.wisetrip.models.BudgetModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.CountryPickerListener;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class BudgetFactoryFragment extends BaseFragment implements
        AlertDialogFragment.OnAlertListener,
        CountryPickerListener {

    private static final String ARG_BUDGET = "arg_budget";
    private static final String ARG_TRIP = "arg_trip";

    private static final String TAG_COUNTRY_PICKER_FRAGMENT = "tag_country_picker_fragment";
    private static final String TAG_ALERT_DIALOG_FRAGMENT = "tag_alert_dialog_fragment";
    private static final String SAVE_TRIP = "save_trip";
    private static final String SAVE_BUDGET = "save_budget";
    private static final String SAVE_IS_EDIT_MODE = "save_is_edit_mode";

    @BindView(R.id.title_edit_text)
    protected EditText mBudgetTitleEditText;
    @BindView(R.id.currency_text)
    protected TextView mCurrencyTextView;
    @BindView(R.id.amount_edit_text)
    protected EditText mAmountEditText;
    @BindView(R.id.currency_icon)
    protected ImageView mCurrencyIcon;

    private TripModel mTrip;
    private BudgetModel mBudget;
    private boolean mIsEditMode;
    private CountryPicker mCountryPicker;
    private AlertDialogFragment mAlertDialogFragment;

    private OnBudgetFactoryListener mOnBudgetFactoryListener;

    private View.OnClickListener mOnCurrencyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            mCountryPicker = CountryPicker.newInstance(getString(R.string.select_country));
            mCountryPicker.setListener(BudgetFactoryFragment.this);
            mCountryPicker.show(getFragmentManager(), TAG_COUNTRY_PICKER_FRAGMENT);
        }
    };

    private TextWatcher mBudgetTitleTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null) {
                mBudget.setTitle(s.toString());
            }
        }
    };

    public BudgetFactoryFragment() {
        // Required empty public constructor
    }

    // Create new Fragment instance with TripModel info
    public static BudgetFactoryFragment newInstance(TripModel trip, BudgetModel budget) {
        BudgetFactoryFragment fragment = new BudgetFactoryFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRIP, trip);
        args.putParcelable(ARG_BUDGET, budget);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_TRIP, mTrip);
        outState.putParcelable(SAVE_BUDGET, mBudget);
        outState.putBoolean(SAVE_IS_EDIT_MODE, mIsEditMode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTrip = getArguments().getParcelable(ARG_TRIP);
            mBudget = getArguments().getParcelable(ARG_BUDGET);

            if (mBudget == null) {
                mBudget = new BudgetModel();
                //mBudget.setDate(DateTime.now().withTimeAtStartOfDay().getMillis());
                String country = Utils.getStringFromSharedPrefs(mFragmentActivity,
                        Constants.Preference.PREFERENCE_DEFAULT_COUNTRY);
                String currency = Utils.getStringFromSharedPrefs(mFragmentActivity,
                        Constants.Preference.PREFERENCE_DEFAULT_CURRENCY);
                mBudget.setCountry(country);
                mBudget.setCurrency(currency);
            }

            if (!TextUtils.isEmpty(mBudget.getId())) {
                mIsEditMode = true;
            }
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
            saveBudget();
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
            mOnBudgetFactoryListener = (OnBudgetFactoryListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnBudgetFactoryListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {

        // Restore instances
        if (savedInstanceState != null) {
            mTrip = savedInstanceState.getParcelable(SAVE_TRIP);
            mBudget = savedInstanceState.getParcelable(SAVE_BUDGET);
            mIsEditMode = savedInstanceState.getBoolean(SAVE_IS_EDIT_MODE);
        }

        View rootView = inflater.inflate(R.layout.fragment_budget_factory, container, false);
        ButterKnife.bind(this, rootView);
        mOnBudgetFactoryListener.changeActionBarTitle(getString(R.string.create_new_budget));

        mBudgetTitleEditText.addTextChangedListener(mBudgetTitleTextWatcher);

        mAmountEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void afterTextChanged(Editable arg0) {
                String str = mAmountEditText.getText().toString();

                if (TextUtils.isEmpty(str)) {
                    mBudget.setTotalAmount(0d);
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
                    mBudget.setTotalAmount(Double.parseDouble(str2));
                } catch (NumberFormatException e) {
                    Timber.e("NumberFormatException while parsing budget amount");
                    e.printStackTrace();
                    mBudget.setTotalAmount(0d);
                }
            }
        });

        mCurrencyTextView.setOnClickListener(mOnCurrencyClickListener);

        populateFormFields();

        return rootView;
    }

    private void populateFormFields() {
        mBudgetTitleEditText.setText(mBudget.getTitle());
        Country country = new Country();
        country.setCode(mBudget.getCountry());
        country.loadFlagByCode(mFragmentActivity);
        mCurrencyIcon.setImageResource(country.getFlag());
        mCurrencyTextView.setText(mBudget.getCurrency());
        if (mBudget.getTotalAmount() != null) {
            // Hardcoding value to use only points
            mAmountEditText.setText(String.format(Locale.US, "%.2f", mBudget.getTotalAmount()));
        }
    }

    private boolean isValidFormFields() {
        boolean isValid = true;

        //TODO: Is title mandatory?
        if (TextUtils.isEmpty(mBudget.getTitle())) {
            mBudgetTitleEditText.setError(getString(R.string.mandatory_field));
            isValid = false;
        }

        if (mBudget.getTotalAmount() == null || mBudget.getTotalAmount() <= 0d) {
            mAmountEditText.setError(getString(R.string.mandatory_field));
            isValid = false;
        }

        return isValid;
    }


    private void saveBudget() {
        if (isValidFormFields()) {
            mOnBudgetFactoryListener.saveBudget(mTrip, mBudget, mIsEditMode);
        }
    }

    private void deleteBudget() {
        mOnBudgetFactoryListener.deleteBudget(mTrip, mBudget);
    }

    private void createDeleteTripConfirmationDialog() {
        mAlertDialogFragment = new AlertDialogFragment();
        mAlertDialogFragment.setTitle(R.string.title_delete_trip);
        mAlertDialogFragment.setMessage(R.string.message_delete_trip);
        mAlertDialogFragment.setOnAlertListener(this);
        mAlertDialogFragment.show(getFragmentManager(), TAG_ALERT_DIALOG_FRAGMENT);
    }

    @Override
    public void positiveAlertButtonClicked() {
        deleteBudget();
    }

    @Override
    public void onSelectCountry(String name, String country, String dialCode, int
            flagDrawableResID) {
        if (mCountryPicker != null && !TextUtils.isEmpty(country)) {
            String currency = Utils.getCurrencySymbol(country);
            if (!TextUtils.isEmpty(currency)) {
                Utils.saveStringToSharedPrefs(mFragmentActivity,
                        Constants.Preference.PREFERENCE_DEFAULT_COUNTRY, country);
                Utils.saveStringToSharedPrefs(mFragmentActivity,
                        Constants.Preference.PREFERENCE_DEFAULT_CURRENCY, currency);
                mBudget.setCountry(country);
                mBudget.setCurrency(currency);
                if (flagDrawableResID > 0) {
                    mCurrencyIcon.setImageResource(flagDrawableResID);
                }
                mCurrencyTextView.setText(currency);
            }

            mCountryPicker.dismiss();
        }
    }

    public interface OnBudgetFactoryListener {
        void changeActionBarTitle(String newTitle);

        void saveBudget(TripModel trip, BudgetModel budget, boolean isEditMode);

        void deleteBudget(TripModel trip, BudgetModel budget);
    }
}
