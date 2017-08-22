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
import com.henriquenfaria.wisetrip.data.FirebaseDbContract;
import com.henriquenfaria.wisetrip.fragments.ExpenseFactoryFragment;
import com.henriquenfaria.wisetrip.models.BudgetModel;
import com.henriquenfaria.wisetrip.models.ExpenseModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import timber.log.Timber;

public class ExpenseFactoryActivity extends AppCompatActivity
        implements ExpenseFactoryFragment.OnExpenseFactoryListener {

    private static final String TAG_EXPENSE_FACTORY_FRAGMENT = "tag_expense_factory_fragment";

    private static final int RESULT_EXPENSE_ADDED = 1;
    private static final int RESULT_EXPENSE_REMOVED = 2;
    private static final int RESULT_EXPENSE_CHANGED = 3;

    private ExpenseFactoryFragment mExpenseFactoryFragment;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRootReference;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_factory);

        // Initialize Firebase instances
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
                Toast.makeText(this, R.string.expense_loading_error, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            TripModel trip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);

            if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                // ExpenseModel already exists
                ExpenseModel expense = intent.getParcelableExtra(Constants.Extra.EXTRA_EXPENSE);
                mExpenseFactoryFragment = ExpenseFactoryFragment.newInstance(trip, expense);

            } else {
                // New expense
                mExpenseFactoryFragment = ExpenseFactoryFragment.newInstance(trip, null);
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
            final DatabaseReference expenseReference
                    = mRootReference.child(FirebaseDbContract.Expenses.PATH_EXPENSES)
                    .child(mCurrentUser.getUid());
            final DatabaseReference budgetReference
                    = mRootReference.child(FirebaseDbContract.Budgets.PATH_BUDGETS)
                    .child(mCurrentUser.getUid());

            if (isEditMode && !TextUtils.isEmpty(expense.getId())) {
                DatabaseReference databaseReference = expenseReference.child(trip.getId())
                        .child(expense.getId());
                databaseReference.setValue(expense);

                // Update expense for applicable budgets
                updateExpenseForCurrentBudgets(RESULT_EXPENSE_CHANGED,
                        trip, budgetReference, expense);
            } else if (!isEditMode) {
                // Add expense
                DatabaseReference databaseReference = expenseReference.child(trip.getId()).push();
                expense.setId(databaseReference.getKey());
                databaseReference.setValue(expense);

                // Add expense for applicable budgets
                updateExpenseForCurrentBudgets(RESULT_EXPENSE_ADDED,
                        trip, budgetReference, expense);
            } else {
                Toast.makeText(this, getString(R.string.expense_updated_error), Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            Toast.makeText(this, getString(R.string.expense_updated_error), Toast.LENGTH_SHORT)
                    .show();
        }
        finish();
    }

    // Iterates through all budgets and add/remove/update the expense if applicable
    private void updateExpenseForCurrentBudgets(final int result, final TripModel trip,
                                                final DatabaseReference budgetReference,
                                                final ExpenseModel expense) {
        budgetReference.child(trip.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Timber.d("onDataChange");
                if (dataSnapshot != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        final BudgetModel budget = snapshot.getValue(BudgetModel.class);
                        final boolean isBudgetExpense = Utils.isBudgetExpense(budget, expense);
                        if (budget != null) {
                            if (result == RESULT_EXPENSE_REMOVED
                                    && isBudgetExpense) {
                                budget.getExpenses().remove(expense.getId());
                            } else if (result == RESULT_EXPENSE_ADDED
                                    && isBudgetExpense) {
                                budget.getExpenses().put(expense.getId(), expense.getAmount());
                            } else if (result == RESULT_EXPENSE_CHANGED) {
                                if (isBudgetExpense) {
                                    budget.getExpenses().put(expense.getId(), expense.getAmount());
                                } else {
                                    budget.getExpenses().remove(expense.getId());
                                }
                            }
                            budget.updateExpensesAmount();
                            budgetReference.child(trip.getId()).child(budget.getId()).setValue
                                    (budget);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.d("onCancelled", databaseError.getMessage());
            }
        });
    }

    @Override
    public void deleteExpense(TripModel trip, ExpenseModel expense) {
        if (expense != null && !TextUtils.isEmpty(expense.getId())) {
            final DatabaseReference expenseReference
                    = mRootReference.child(FirebaseDbContract.Expenses.PATH_EXPENSES)
                    .child(mCurrentUser.getUid());

            final DatabaseReference budgetReference
                    = mRootReference.child(FirebaseDbContract.Budgets.PATH_BUDGETS)
                    .child(mCurrentUser.getUid());

            expenseReference.child(trip.getId()).child(expense.getId()).removeValue();

            // Remove expense from applicable budgets
            updateExpenseForCurrentBudgets(RESULT_EXPENSE_REMOVED, trip, budgetReference,
                    expense);
        } else {
            Toast.makeText(this, getString(R.string.expense_updated_error), Toast.LENGTH_SHORT)
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
