package com.henriquenfaria.wisetrip.activities;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.fragments.ExpenseFactoryFragment;
import com.henriquenfaria.wisetrip.models.ExpenseModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;

public class ExpenseFactoryActivity extends AppCompatActivity
        implements ExpenseFactoryFragment.OnExpenseFactoryListener {

    private static final String TAG_EXPENSE_FACTORY_FRAGMENT = "tag_expense_factory_fragment";
    private TripModel mTrip;
    private ExpenseModel mExpense;
    private ExpenseFactoryFragment mExpenseFactoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_factory);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent == null || !intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                Toast.makeText(this, R.string.expense_loading_error, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            mTrip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);

            if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                // ExpenseModel already exists
                mExpense = intent.getParcelableExtra(Constants.Extra.EXTRA_EXPENSE);
                mExpenseFactoryFragment = ExpenseFactoryFragment.newInstance(mTrip, mExpense);

            } else {
                // New expense
                mExpenseFactoryFragment = ExpenseFactoryFragment.newInstance(mTrip, null);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.expense_factory_fragment_container, mExpenseFactoryFragment,
                            TAG_EXPENSE_FACTORY_FRAGMENT).commit();
        } else {
            // Fragment already exists, just get it using its TAG
            mExpenseFactoryFragment = (ExpenseFactoryFragment) getSupportFragmentManager()
                    .findFragmentByTag(TAG_EXPENSE_FACTORY_FRAGMENT);
        }
    }


    @Override
    public void changeActionBarTitle(String newTitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(newTitle);
        }
    }

    @Override
    public void saveExpense(TripModel trip, ExpenseModel expense, boolean isEditMode) {
        if (expense != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Constants.Extra.EXTRA_EXPENSE, (Parcelable) expense);
            setResult(isEditMode ? Constants.Result.RESULT_EXPENSE_CHANGED
                    : Constants.Result.RESULT_EXPENSE_ADDED, resultIntent);
        } else {
            setResult(Constants.Result.RESULT_EXPENSE_ERROR);
        }

        finish();
    }

    @Override
    public void deleteExpense(TripModel trip, ExpenseModel expense) {
        if (expense != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Constants.Extra.EXTRA_EXPENSE, (Parcelable) expense);
            setResult(Constants.Result.RESULT_EXPENSE_REMOVED, resultIntent);
        } else {
            setResult(Constants.Result.RESULT_EXPENSE_ERROR);
        }

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // To animate transition like back button press
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
