package com.henriquenfaria.wisetrip.activities;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.adapters.ViewPagerAdapter;
import com.henriquenfaria.wisetrip.fragments.BudgetListFragment;
import com.henriquenfaria.wisetrip.fragments.ExpenseListFragment;
import com.henriquenfaria.wisetrip.fragments.PlaceListFragment;
import com.henriquenfaria.wisetrip.listeners.OnBudgetInteractionListener;
import com.henriquenfaria.wisetrip.listeners.OnExpenseInteractionListener;
import com.henriquenfaria.wisetrip.listeners.OnPlaceInteractionListener;
import com.henriquenfaria.wisetrip.models.AttributionModel;
import com.henriquenfaria.wisetrip.models.BudgetModel;
import com.henriquenfaria.wisetrip.models.ExpenseModel;
import com.henriquenfaria.wisetrip.models.PlaceModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Features;
import com.henriquenfaria.wisetrip.utils.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


/* Activity to display all related data of a specific TripModel */
public class TripDetailsActivity extends AppCompatActivity implements
        OnExpenseInteractionListener,
        OnBudgetInteractionListener,
        OnPlaceInteractionListener {

    private static final String SAVE_IS_SHARED_ELEMENT_TRANSITION =
            "save_is_shared_element_transition";
    public static final int TAB_EXPENSES_POSITION = 0;
    public static final int TAB_BUDGETS_POSITION = 1;
    public static final int TAB_PLACES_POSITION = 2;

    @BindView(R.id.attribution_container)
    protected LinearLayout mAttributionContainer;
    @BindView(R.id.trip_photo)
    protected ImageView mTripPhoto;
    @BindView(R.id.trip_photo_protection)
    protected View mTripPhotoProtection;
    @BindView(R.id.attribution_prefix)
    protected TextView mAttributionPrefix;
    @BindView(R.id.trip_title)
    protected TextView mTripTitle;
    @BindView(R.id.attribution_content)
    protected TextView mAttributionContent;
    @BindView(R.id.fab)
    protected FloatingActionButton mFab;
    @BindView(R.id.tablayout)

    protected TabLayout mTabLayout;
    private TripModel mTrip;
    private boolean mIsSharedElementTransition;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRootReference;
    private FirebaseUser mCurrentUser;
    private int mDefaultTabIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase instances
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRootReference = mFirebaseDatabase.getReference();

        if (getIntent() != null) {
            mTrip = getIntent().getParcelableExtra(Constants.Extra.EXTRA_TRIP);
            mDefaultTabIndex = getIntent().getIntExtra(Constants.Extra.EXTRA_TRIP_DETAILS_TAB_INDEX,
                    TAB_EXPENSES_POSITION);
        }


        if (mTrip == null) {
            Toast.makeText(this, R.string.could_not_load_trip_details, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_trip_details);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            mIsSharedElementTransition = savedInstanceState.getBoolean
                    (SAVE_IS_SHARED_ELEMENT_TRANSITION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        } else {
            supportPostponeEnterTransition();
        }


        setTripBackdropPhoto(mTrip.getId());
        setupAppBarLayout();
        setupToolbar();
        setupFab();
        setupViewPager(mDefaultTabIndex);
        setTransitionNames();
    }

    private void setupToolbar() {
        Toolbar toolbar = ButterKnife.findById(TripDetailsActivity.this, R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView title = ButterKnife.findById(toolbar, R.id.trip_title);
        title.setText(mTrip.getTitle());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(null);
        }
    }

    private void setupFab() {
        mFab.setImageResource(getFabImageResource());
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mTabLayout.getSelectedTabPosition()) {
                    case TAB_EXPENSES_POSITION:
                        startExpenseFactory(null);
                        break;
                    case TAB_BUDGETS_POSITION:
                        startBudgetFactory(null);
                        break;
                    case TAB_PLACES_POSITION:
                        startPlaceFactory(null);
                        break;
                }
            }
        });
    }

    private void setupAppBarLayout() {
        AppBarLayout appbarLayout = ButterKnife.findById(TripDetailsActivity.this, R.id
                .appbarlayout);

        if (Features.TRIP_LIST_SHARED_ELEMENT_TRANSITION_ENABLED) {
            appbarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, State state) {
                    switch (state) {
                        case COLLAPSED:
                        case IDLE:
                            mIsSharedElementTransition = false;
                            break;
                        case EXPANDED:
                            mIsSharedElementTransition = true;
                            break;
                    }

                }
            });
        }

    }

    private void setTransitionNames() {
        String tripId = mTrip.getId();
        ViewCompat.setTransitionName(mTripPhoto,
                Constants.Transition.PREFIX_TRIP_PHOTO + tripId);
        ViewCompat.setTransitionName(mTripPhotoProtection,
                Constants.Transition.PREFIX_TRIP_PHOTO_PROTECTION + tripId);
        ViewCompat.setTransitionName(mAttributionContainer,
                Constants.Transition.PREFIX_TRIP_ATTRIBUTION + tripId);
        ViewCompat.setTransitionName(mTripTitle,
                Constants.Transition.PREFIX_TRIP_TITLE + tripId);

    }

    private int getFabImageResource() {
        switch (mTabLayout.getSelectedTabPosition()) {
            case TAB_EXPENSES_POSITION:
                return R.drawable.ic_fab_expense;
            case TAB_BUDGETS_POSITION:
                return R.drawable.ic_fab_budget;
            case TAB_PLACES_POSITION:
                return R.drawable.ic_fab_place;
            default:
                return R.drawable.ic_fab_plus;
        }
    }

    private void animateFab() {
        if (!mFab.isShown()) {
            mFab.setImageResource(getFabImageResource());
            mFab.show();
        } else {
            mFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    super.onHidden(fab);
                    mFab.setImageResource(getFabImageResource());
                    mFab.show();
                }
            });
        }
    }


    private void setupViewPager(int defaultTabIndex) {
        ViewPager viewPager = ButterKnife.findById(TripDetailsActivity.this, R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(ExpenseListFragment.newInstance(mTrip), getString(R.string.expenses));
        adapter.addFragment(BudgetListFragment.newInstance(mTrip), getString(R.string.budgets));
        adapter.addFragment(PlaceListFragment.newInstance(mTrip), getString(R.string.places));

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setCurrentItem(defaultTabIndex);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                animateFab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        mTabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SAVE_IS_SHARED_ELEMENT_TRANSITION, mIsSharedElementTransition);
        super.onSaveInstanceState(outState);
    }

    private void setTripBackdropPhoto(String tripId) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directoryFile = cw.getDir(Constants.General.DESTINATION_PHOTO_DIR,
                Context.MODE_PRIVATE);
        final File photoFile = new File(directoryFile, tripId);

        ImageView tripPhotoBackdrop = ButterKnife.findById(this, R.id.trip_photo);
        Picasso.with(this)
                .load(photoFile)
                .networkPolicy(
                        NetworkPolicy.NO_CACHE,
                        NetworkPolicy.NO_STORE,
                        NetworkPolicy.OFFLINE)
                .noFade()
                .noPlaceholder()
                .error(R.drawable.trip_photo_default)
                .into(tripPhotoBackdrop, new Callback() {
                    @Override
                    public void onSuccess() {
                        displayPhotoAttribution(mTrip.getId(), true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startPostponedEnterTransition();
                        } else {
                            supportStartPostponedEnterTransition();
                        }
                    }

                    @Override
                    public void onError() {
                        displayPhotoAttribution(mTrip.getId(), false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startPostponedEnterTransition();
                        } else {
                            supportStartPostponedEnterTransition();
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // To animate transition like back button press
                if (mIsSharedElementTransition) {
                    supportFinishAfterTransition();
                } else {
                    finish();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mIsSharedElementTransition) {
            super.onBackPressed();
        } else {
            finish();
        }
    }

    private void displayPhotoAttribution(String tripId, boolean shouldDisplay) {
        if (mAttributionContainer != null) {
            if (shouldDisplay) {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference attributionsReference = firebaseDatabase
                        .getReference()
                        .child("attributions")
                        .child(currentUser.getUid())
                        .child(tripId);

                attributionsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Timber.d("onDataChange");
                        if (mAttributionContainer != null) {
                            AttributionModel attribution = dataSnapshot.getValue(AttributionModel
                                    .class);
                            if (attribution != null && !TextUtils.isEmpty(attribution.getText())) {
                                Spanned result;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    result = Html.fromHtml(attribution.getText(),
                                            Html.FROM_HTML_MODE_LEGACY);
                                } else {
                                    result = Html.fromHtml(attribution.getText());
                                }

                                mAttributionContent.setText(result);
                                mAttributionContent.setMovementMethod(
                                        LinkMovementMethod.getInstance());
                                mAttributionPrefix.setVisibility(View.VISIBLE);
                                mAttributionContent.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Timber.d("onCancelled", databaseError.getMessage());
                    }
                });
            } else {
                mAttributionPrefix.setVisibility(View.GONE);
                mAttributionContent.setVisibility(View.GONE);
            }
        }
    }

    public void startExpenseFactory(ExpenseModel expense) {
        Intent intent = new Intent(TripDetailsActivity.this,
                ExpenseFactoryActivity.class);
        intent.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) mTrip);
        if (expense != null) {
            intent.putExtra(Constants.Extra.EXTRA_EXPENSE, (Parcelable) expense);
        }
        startActivityForResult(intent, Constants.Request.REQUEST_EXPENSE_FACTORY);
    }

    public void startBudgetFactory(BudgetModel budget) {
        Intent intent = new Intent(TripDetailsActivity.this,
                BudgetFactoryActivity.class);
        intent.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) mTrip);
        if (budget != null) {
            intent.putExtra(Constants.Extra.EXTRA_BUDGET, (Parcelable) budget);
        }
        startActivityForResult(intent, Constants.Request.REQUEST_BUDGET_FACTORY);
    }

    public void startPlaceFactory(PlaceModel place) {
        Intent intent = new Intent(TripDetailsActivity.this,
                PlaceFactoryActivity.class);
        intent.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) mTrip);
        if (place != null) {
            intent.putExtra(Constants.Extra.EXTRA_PLACE, (Parcelable) place);
        }
        startActivityForResult(intent, Constants.Request.REQUEST_PLACE_FACTORY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == Constants.Request.REQUEST_EXPENSE_FACTORY) {
                handleRequestExpenseFactory(resultCode, data);
            } else if (requestCode == Constants.Request.REQUEST_BUDGET_FACTORY) {
                handleRequestBudgetFactory(resultCode, data);
            } else if (requestCode == Constants.Request.REQUEST_PLACE_FACTORY) {
                handleRequestPlaceFactory(resultCode, data);
            }
        }
    }

    private void handleRequestExpenseFactory(int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        final ExpenseModel expense = data.getParcelableExtra(Constants.Extra.EXTRA_EXPENSE);
        final DatabaseReference expenseReference
                = mRootReference.child("expenses").child(mCurrentUser.getUid());

        final DatabaseReference budgetReference
                = mRootReference.child("budgets").child(mCurrentUser.getUid());

        if (resultCode == Constants.Result.RESULT_EXPENSE_ADDED && expense != null) {
            // Add expense
            DatabaseReference databaseReference = expenseReference.child(mTrip.getId()).push();
            expense.setId(databaseReference.getKey());
            databaseReference.setValue(expense);

            // Add expense for applicable budgets
            updateExpenseForCurrentBudgets(Constants.Result.RESULT_EXPENSE_ADDED,
                    budgetReference, expense);

        } else if (resultCode == Constants.Result.RESULT_EXPENSE_CHANGED
                && expense != null && !TextUtils.isEmpty(expense.getId())) {
            DatabaseReference databaseReference = expenseReference.child(mTrip.getId())
                    .child(expense.getId());
            databaseReference.setValue(expense);

            // Update expense for applicable budgets
            updateExpenseForCurrentBudgets(Constants.Result.RESULT_EXPENSE_CHANGED,
                    budgetReference, expense);

        } else if (resultCode == Constants.Result.RESULT_EXPENSE_REMOVED
                && expense != null && !TextUtils.isEmpty(expense.getId())) {
            expenseReference.child(mTrip.getId()).child(expense.getId()).removeValue();

            // Remove expense from applicable budgets
            updateExpenseForCurrentBudgets(Constants.Result.RESULT_EXPENSE_REMOVED,
                    budgetReference, expense);

        } else if (resultCode == Constants.Result.RESULT_EXPENSE_ERROR) {
            Toast.makeText(this, getString(R.string.expense_updated_error), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    // Iterates through all budgets and add/remove/update the expense if applicable
    private void updateExpenseForCurrentBudgets(final int result, final DatabaseReference
            budgetReference,
                                                final ExpenseModel expense) {
        budgetReference.child(mTrip.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Timber.d("onDataChange");
                if (dataSnapshot != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        final BudgetModel budget = snapshot.getValue(BudgetModel.class);
                        final boolean isBudgetExpense = Utils.isBudgetExpense(budget, expense);
                        if (budget != null) {
                            if (result == Constants.Result.RESULT_EXPENSE_REMOVED
                                    && isBudgetExpense) {
                                budget.getExpenses().remove(expense.getId());
                            } else if (result == Constants.Result.RESULT_EXPENSE_ADDED
                                    && isBudgetExpense) {
                                budget.getExpenses().put(expense.getId(), expense.getAmount());
                            } else if (result == Constants.Result.RESULT_EXPENSE_CHANGED) {
                                if (isBudgetExpense) {
                                    budget.getExpenses().put(expense.getId(), expense.getAmount());
                                } else {
                                    budget.getExpenses().remove(expense.getId());
                                }
                            }
                            budget.updateExpensesAmount();
                            budgetReference.child(mTrip.getId()).child(budget.getId()).setValue
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

    private void handleRequestBudgetFactory(int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        final BudgetModel budget = data.getParcelableExtra(Constants.Extra.EXTRA_BUDGET);
        final DatabaseReference budgetReference
                = mRootReference.child("budgets").child(mCurrentUser.getUid());

        final DatabaseReference expenseReference
                = mRootReference.child("expenses").child(mCurrentUser.getUid());

        if (resultCode == Constants.Result.RESULT_BUDGET_ADDED && budget != null) {
            DatabaseReference databaseReference = budgetReference.child(mTrip.getId()).push();
            budget.setId(databaseReference.getKey());
            databaseReference.setValue(budget);

            // Update budget by updating it with applicable expenses
            updateBudgetForCurrentExpenses(Constants.Result.RESULT_BUDGET_CHANGED,
                    expenseReference, budgetReference, budget);

        } else if (resultCode == Constants.Result.RESULT_BUDGET_CHANGED
                && budget != null && !TextUtils.isEmpty(budget.getId())) {
            DatabaseReference databaseReference = budgetReference.child(mTrip.getId())
                    .child(budget.getId());
            databaseReference.setValue(budget);

            // Update budget by adding applicable expenses
            updateBudgetForCurrentExpenses(Constants.Result.RESULT_BUDGET_CHANGED,
                    expenseReference, budgetReference, budget);

        } else if (resultCode == Constants.Result.RESULT_BUDGET_REMOVED
                && budget != null && !TextUtils.isEmpty(budget.getId())) {
            budgetReference.child(mTrip.getId()).child(budget.getId()).removeValue();

            // No need to update expenses here. Just delete the budget.

        } else if (resultCode == Constants.Result.RESULT_BUDGET_ERROR) {
            Toast.makeText(this, getString(R.string.budget_updated_error), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void handleRequestPlaceFactory(int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        final PlaceModel place = data.getParcelableExtra(Constants.Extra.EXTRA_PLACE);
        final DatabaseReference placeReference
                = mRootReference.child("places").child(mCurrentUser.getUid());

        if (resultCode == Constants.Result.RESULT_PLACE_ADDED && place != null) {
            DatabaseReference databaseReference = placeReference.child(mTrip.getId()).push();
            place.setId(databaseReference.getKey());
            databaseReference.setValue(place);

        } else if (resultCode == Constants.Result.RESULT_PLACE_CHANGED
                && place != null && !TextUtils.isEmpty(place.getId())) {
            DatabaseReference databaseReference = placeReference.child(mTrip.getId())
                    .child(place.getId());
            databaseReference.setValue(place);

        } else if (resultCode == Constants.Result.RESULT_PLACE_REMOVED
                && place != null && !TextUtils.isEmpty(place.getId())) {
            placeReference.child(mTrip.getId()).child(place.getId()).removeValue();

        } else if (resultCode == Constants.Result.RESULT_PLACE_ERROR) {
            Toast.makeText(this, getString(R.string.place_updated_error), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    // Iterates through all expenses and add them to the created/updated budget if applicable
    private void updateBudgetForCurrentExpenses(final int result, final DatabaseReference
            expenseReference,
                                                final DatabaseReference budgetReference, final
                                                BudgetModel budget) {
        expenseReference.child(mTrip.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Timber.d("onDataChange");
                if (dataSnapshot != null) {

                    boolean budgetChangedProcessed = false;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        final ExpenseModel expense = snapshot.getValue(ExpenseModel.class);
                        final boolean isBudgetExpense = Utils.isBudgetExpense(budget, expense);
                        if (expense != null) {
                            if (result == Constants.Result.RESULT_BUDGET_CHANGED
                                    && !budgetChangedProcessed) {
                                if (!isBudgetExpense) {
                                    budget.getExpenses().clear();
                                }
                                budgetChangedProcessed = true;
                            }

                            if (isBudgetExpense) {
                                budget.getExpenses().put(expense.getId(), expense.getAmount());
                            }

                            /*if (result == Constants.Result.RESULT_BUDGET_ADDED
                                    && isBudgetExpense) {
                                budget.getExpenses().put(expense.getId(), expense.getAmount());
                            } else if (result == Constants.Result.RESULT_BUDGET_CHANGED) {
                                if (isBudgetExpense) {
                                    budget.getExpenses().put(expense.getId(), expense.getAmount());
                                } else {
                                    budget.getExpenses().remove(expense.getId());
                                }
                            }*/

                        }
                    }

                    budget.updateExpensesAmount();
                    budgetReference.child(mTrip.getId()).child(budget.getId()).setValue(budget);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.d("onCancelled", databaseError.getMessage());
            }
        });
    }

    @Override
    public void onExpenseClicked(ExpenseModel expense) {
        startExpenseFactory(expense);
    }

    @Override
    public void onBudgetClicked(BudgetModel budget) {
        startBudgetFactory(budget);
    }

    @Override
    public void onPlaceClicked(PlaceModel place) {
        startPlaceFactory(place);
    }

    private static abstract class AppBarStateChangeListener implements
            AppBarLayout.OnOffsetChangedListener {

        private State mCurrentState = State.IDLE;

        @Override
        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            if (i == 0) {
                if (mCurrentState != State.EXPANDED) {
                    onStateChanged(appBarLayout, State.EXPANDED);
                }
                mCurrentState = State.EXPANDED;
            } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                if (mCurrentState != State.COLLAPSED) {
                    onStateChanged(appBarLayout, State.COLLAPSED);
                }
                mCurrentState = State.COLLAPSED;
            } else {
                if (mCurrentState != State.IDLE) {
                    onStateChanged(appBarLayout, State.IDLE);
                }
                mCurrentState = State.IDLE;
            }
        }

        public abstract void onStateChanged(AppBarLayout appBarLayout, State state);

        public enum State {
            EXPANDED,
            COLLAPSED,
            IDLE
        }
    }
}
