package com.henriquenfaria.wisetrip.activities;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.fragments.BudgetFactoryFragment;
import com.henriquenfaria.wisetrip.models.BudgetModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;

public class BudgetFactoryActivity extends AppCompatActivity
        implements BudgetFactoryFragment.OnBudgetFactoryListener {

    private static final String TAG_BUDGET_FACTORY_FRAGMENT = "tag_budget_factory_fragment";
    private TripModel mTrip;
    private BudgetModel mBudget;
    private BudgetFactoryFragment mBudgetFactoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_factory);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent == null || !intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                Toast.makeText(this, R.string.budget_loading_error, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            mTrip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);

            if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                // BudgetModel already exists
                mBudget = intent.getParcelableExtra(Constants.Extra.EXTRA_BUDGET);
                mBudgetFactoryFragment = BudgetFactoryFragment.newInstance(mTrip, mBudget);

            } else {
                // New budget
                mBudgetFactoryFragment = BudgetFactoryFragment.newInstance(mTrip, null);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.budget_factory_fragment_container, mBudgetFactoryFragment,
                            TAG_BUDGET_FACTORY_FRAGMENT).commit();
        } else {
            // Fragment already exists, just get it using its TAG
            mBudgetFactoryFragment = (BudgetFactoryFragment) getSupportFragmentManager()
                    .findFragmentByTag(TAG_BUDGET_FACTORY_FRAGMENT);
        }
    }


    @Override
    public void changeActionBarTitle(String newTitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(newTitle);
        }
    }

    @Override
    public void saveBudget(TripModel trip, BudgetModel budget, boolean isEditMode) {
        if (budget != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Constants.Extra.EXTRA_BUDGET, (Parcelable) budget);
            setResult(isEditMode ? Constants.Result.RESULT_BUDGET_CHANGED
                    : Constants.Result.RESULT_BUDGET_ADDED, resultIntent);
        } else {
            setResult(Constants.Result.RESULT_BUDGET_ERROR);
        }

        finish();
    }

    @Override
    public void deleteBudget(TripModel trip, BudgetModel budget) {
        if (budget != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Constants.Extra.EXTRA_BUDGET, (Parcelable) budget);
            setResult(Constants.Result.RESULT_BUDGET_REMOVED, resultIntent);
        } else {
            setResult(Constants.Result.RESULT_BUDGET_ERROR);
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
