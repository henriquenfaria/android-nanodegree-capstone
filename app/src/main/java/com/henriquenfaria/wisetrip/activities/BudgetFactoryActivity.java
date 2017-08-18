package com.henriquenfaria.wisetrip.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.fragments.BudgetFactoryFragment;
import com.henriquenfaria.wisetrip.models.BudgetModel;
import com.henriquenfaria.wisetrip.models.ExpenseModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import timber.log.Timber;

public class BudgetFactoryActivity extends AppCompatActivity
        implements BudgetFactoryFragment.OnBudgetFactoryListener {

    private static final String TAG_BUDGET_FACTORY_FRAGMENT = "tag_budget_factory_fragment";

    private BudgetFactoryFragment mBudgetFactoryFragment;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRootReference;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_factory);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRootReference = mFirebaseDatabase.getReference();

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

            TripModel trip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);

            if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                // BudgetModel already exists
                BudgetModel budget = intent.getParcelableExtra(Constants.Extra.EXTRA_BUDGET);
                mBudgetFactoryFragment = BudgetFactoryFragment.newInstance(trip, budget);

            } else {
                // New budget
                mBudgetFactoryFragment = BudgetFactoryFragment.newInstance(trip, null);
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
            final DatabaseReference budgetReference
                    = mRootReference.child("budgets").child(mCurrentUser.getUid());

            final DatabaseReference expenseReference
                    = mRootReference.child("expenses").child(mCurrentUser.getUid());
            if (isEditMode && !TextUtils.isEmpty(budget.getId())) {
                DatabaseReference databaseReference = budgetReference.child(trip.getId())
                        .child(budget.getId());
                databaseReference.setValue(budget);

                // Update budget by adding applicable expenses
                updateBudgetForCurrentExpenses(expenseReference, trip, budgetReference, budget);

            } else if (!isEditMode){
                DatabaseReference databaseReference = budgetReference.child(trip.getId()).push();
                budget.setId(databaseReference.getKey());
                databaseReference.setValue(budget);

                // Update budget by updating it with applicable expenses
                updateBudgetForCurrentExpenses(expenseReference, trip, budgetReference, budget);
            } else {
                Toast.makeText(this, getString(R.string.budget_updated_error), Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            Toast.makeText(this, getString(R.string.budget_updated_error), Toast.LENGTH_SHORT)
                    .show();
        }
        finish();
    }

    // Iterates through all expenses and add them to the created/updated budget if applicable
    private void updateBudgetForCurrentExpenses(final DatabaseReference expenseReference,
                                                final TripModel trip,
                                                final DatabaseReference budgetReference, final
                                                BudgetModel budget) {
        expenseReference.child(trip.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Timber.d("onDataChange");
                if (dataSnapshot != null) {

                    boolean budgetChangedProcessed = false;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        final ExpenseModel expense = snapshot.getValue(ExpenseModel.class);
                        final boolean isBudgetExpense = Utils.isBudgetExpense(budget, expense);
                        if (expense != null) {
                            if (!budgetChangedProcessed) {
                                if (!isBudgetExpense) {
                                    budget.getExpenses().clear();
                                }
                                budgetChangedProcessed = true;
                            }

                            if (isBudgetExpense) {
                                budget.getExpenses().put(expense.getId(), expense.getAmount());
                            }
                        }
                    }

                    budget.updateExpensesAmount();
                    budgetReference.child(trip.getId()).child(budget.getId()).setValue(budget);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.d("onCancelled", databaseError.getMessage());
            }
        });
    }

    @Override
    public void deleteBudget(TripModel trip, BudgetModel budget) {
        if (budget != null && !TextUtils.isEmpty(budget.getId())) {
            final DatabaseReference budgetReference
                    = mRootReference.child("budgets").child(mCurrentUser.getUid());

            final DatabaseReference expenseReference
                    = mRootReference.child("expenses").child(mCurrentUser.getUid());
            budgetReference.child(trip.getId()).child(budget.getId()).removeValue();

            // No need to update expenses here. Just delete the budget.
        } else {
            Toast.makeText(this, getString(R.string.budget_updated_error), Toast.LENGTH_SHORT)
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
