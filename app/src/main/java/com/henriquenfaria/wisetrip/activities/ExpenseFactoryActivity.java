package com.henriquenfaria.wisetrip.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.fragments.ExpenseFactoryFragment;
import com.henriquenfaria.wisetrip.models.ExpenseModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;

public class ExpenseFactoryActivity extends AppCompatActivity
        implements ExpenseFactoryFragment.OnExpenseFactoryListener {

    private static final String TAG_EXPENSE_FACTORY_FRAGMENT = "tag_expense_factory_fragment";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mExpensesReference;
    private FirebaseUser mCurrentUser;
    private TripModel mTrip;
    private ExpenseModel mExpense;
    private ExpenseFactoryFragment mExpenseFactoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_factory);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize Firebase instances
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mExpensesReference = mFirebaseDatabase.getReference()
                .child("expenses")
                .child(mCurrentUser.getUid());

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent == null || !intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                Toast.makeText(this, R.string.expense_loading_expense, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            mTrip =  intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);

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
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(newTitle);
        }
    }

    @Override
    public void saveExpense(TripModel trip, ExpenseModel expense, boolean isEditMode) {
        if (isEditMode) {
            // Update existing ExpenseModel
            if (expense != null && !TextUtils.isEmpty(expense.getId())) {
                DatabaseReference databaseReference = mExpensesReference.child(trip.getId())
                        .child(expense.getId());
                databaseReference.setValue(expense);
                Toast.makeText(this, getString(R.string.expense_updated_success),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.expense_updated_error),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Creating ExpenseModel
            DatabaseReference databaseReference = mExpensesReference.child(trip.getId()).push();
            expense.setId(databaseReference.getKey());
            databaseReference.setValue(expense);
            Toast.makeText(this, getString(R.string.expense_created_success), Toast.LENGTH_SHORT)
                    .show();
        }

        finish();
    }

    @Override
    public void deleteExpense(TripModel trip, ExpenseModel expense) {
        if (expense != null && !TextUtils.isEmpty(expense.getId())) {
            // Remove ExpenseModel
            mExpensesReference.child(trip.getId()).child(expense.getId()).removeValue();
            Toast.makeText(this, getString(R.string.expense_deleted_success), Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(this, getString(R.string.expense_deleted_error), Toast.LENGTH_SHORT)
                    .show();
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
