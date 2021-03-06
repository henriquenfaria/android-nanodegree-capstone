package com.henriquenfaria.wisetrip.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.data.FirebaseDbContract;
import com.henriquenfaria.wisetrip.flexibles.BudgetItem;
import com.henriquenfaria.wisetrip.listeners.OnBudgetInteractionListener;
import com.henriquenfaria.wisetrip.models.BudgetModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.utils.NotificationUtils;
import com.henriquenfaria.wisetrip.utils.Utils;

import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.IFlexible;
import timber.log.Timber;

/**
 * Fragment that displays a budget list
 */
public class BudgetListFragment extends FirebaseBaseFragment implements
        FlexibleAdapter.OnItemClickListener,
        FlexibleAdapter.OnUpdateListener {
    private static final String ARG_TRIP = "arg_trip";

    @BindView(R.id.budget_list_layout)
    protected FrameLayout mBudgetListLayout;

    @BindView(R.id.budget_list_recycler_view)
    protected RecyclerView mBudgetListRecyclerView;

    @BindView(R.id.empty_view)
    protected RelativeLayout mEmptyView;

    @BindView(R.id.empty_text)
    protected TextView mEmptyText;

    private DatabaseReference mBudgetReference;
    private FlexibleAdapter<IFlexible> mBudgetAdapter;
    private ValueEventListener mValueEventListener;
    private ChildEventListener mChildEventListener;
    private TripModel mTrip;
    private OnBudgetInteractionListener mOnBudgetInteractionListener;

    // Create new Fragment instance with BudgetModel info
    public static BudgetListFragment newInstance(TripModel trip) {
        BudgetListFragment fragment = new BudgetListFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRIP, trip);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTrip = getArguments().getParcelable(ARG_TRIP);
        }
        mBudgetReference = mRootReference
                .child(FirebaseDbContract.Budgets.PATH_BUDGETS)
                .child(mCurrentUser.getUid())
                .child(mTrip.getId());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_budget_list, container, false);
        ButterKnife.bind(this, rootView);

        // TODO: Use it when date is indexed:
        // https://github.com/firebase/FirebaseUI-Android/blob/master/database/README.md
       /* new FirebaseIndexRecyclerAdapter<mBudgetAdapter, TripFirebaseHolder>(mBudgetAdapter
       .class,
                R.layout.trip_item,
                TripFirebaseHolder.class,
                keyRef, // The Firebase location containing the list of keys to be found in dataRef.
                dataRef) //The Firebase location to watch for data changes. Each key key found at
                 keyRef's location represents a list item in the RecyclerView.
         */

        mEmptyText.setText(R.string.no_saved_budgets);

        mBudgetAdapter = new FlexibleAdapter<>(null, this);
        /*mBudgetAdapter
                .setDisplayHeadersAtStartUp(true)
                .setStickyHeaders(true)
                .setUnlinkAllItemsOnRemoveHeaders(true);*/

        mBudgetListRecyclerView.setLayoutManager(
                new SmoothScrollLinearLayoutManager(mFragmentActivity));
        mBudgetListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mBudgetListRecyclerView.setAdapter(mBudgetAdapter);


        //TODO: Implement fastScroller logic later
       /*FastScroller fastScroller = getView().findViewById(R.id.fast_scroller);
        fastScroller.addOnScrollStateChangeListener((MainActivity) getActivity());
        mBudgetAdapter.setFastScroller(fastScroller);*/

        attachDatabaseReadListener();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnBudgetInteractionListener) {
            mOnBudgetInteractionListener = (OnBudgetInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBudgetInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnBudgetInteractionListener = null;
    }

    @Override
    public boolean onItemClick(int position) {
        IFlexible flexibleItem = mBudgetAdapter.getItem(position);
        if (flexibleItem instanceof BudgetItem) {
            BudgetItem budgetItem = (BudgetItem) flexibleItem;
            BudgetModel budget = budgetItem.getModel();
            if (mOnBudgetInteractionListener != null) {
                mOnBudgetInteractionListener.onBudgetClicked(budget);
            }
            return false;
        }

        return false;
    }

    // Preserve listener instances (Serializable) to avoid getting items again on orientation change
    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildAdded");

                    BudgetModel budget = dataSnapshot.getValue(BudgetModel.class);
                    if (budget != null && !TextUtils.isEmpty(budget.getId())
                            && mBudgetAdapter != null) {
                        budgetAdded(budget);

                        Utils.saveBooleanToSharedPrefs(mFragmentActivity, budget.getId(), false,
                                true);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildChanged");

                    BudgetModel budget = dataSnapshot.getValue(BudgetModel.class);
                    if (budget != null && !TextUtils.isEmpty(budget.getId())
                            && mBudgetAdapter != null) {
                        budgetChanged(budget);

                        if (budget.isBudgetExceeded()
                                && !Utils.getBooleanFromSharedPrefs(mFragmentActivity, budget
                                .getId(), false)) {
                            NotificationUtils.notifyBudgetLimitExceeded(mFragmentActivity,
                                    budget, mTrip);
                            Utils.saveBooleanToSharedPrefs(mFragmentActivity, budget.getId(),
                                    true, true);
                        }
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Timber.d("onChildRemoved");

                    BudgetModel budget = dataSnapshot.getValue(BudgetModel.class);
                    if (budget != null && !TextUtils.isEmpty(budget.getId())
                            && mBudgetAdapter != null) {
                        budgetRemoved(budget);

                        Utils.deleteSharedPrefs(mFragmentActivity, budget.getId(), true);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildMoved");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Timber.d("onCancelled", databaseError.getMessage());
                }

            };
            mBudgetReference.addChildEventListener(mChildEventListener);
        }

        // To disable weird animations until all data is retrieved
        // MUST be added after mChildEventListener
        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Timber.d("onDataChange");

                    mBudgetListLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Timber.d("onCancelled", databaseError.getMessage());

                    mBudgetListLayout.setVisibility(View.VISIBLE);
                }
            };
            mBudgetReference.addListenerForSingleValueEvent(mValueEventListener);
        }
    }

    private void budgetAdded(BudgetModel budget) {
        BudgetItem budgetItem = new BudgetItem(budget);
        mBudgetAdapter.addItem(mBudgetAdapter
                .calculatePositionFor(budgetItem, new BudgetItemComparator()), budgetItem);
    }


    private void budgetChanged(BudgetModel budget) {
        BudgetItem budgetItem = new BudgetItem(budget);
        mBudgetAdapter.updateItem(budgetItem);
        mBudgetAdapter.moveItem(mBudgetAdapter.getGlobalPositionOf(budgetItem), mBudgetAdapter
                .calculatePositionFor(budgetItem, new BudgetItemComparator()));
    }

    private void budgetRemoved(BudgetModel budget) {
        BudgetItem budgetItem = new BudgetItem(budget);
        mBudgetAdapter.removeItem(mBudgetAdapter.getGlobalPositionOf(budgetItem));
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mBudgetReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        if (mValueEventListener != null) {
            mBudgetReference.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        detachDatabaseReadListener();
    }

    @Override
    public void onUpdateEmptyView(int size) {
        if (size > 0) {
            ViewCompat.animate(mEmptyView).alpha(0);
        } else {
            ViewCompat.animate(mEmptyView).alpha(1);
        }
    }

    private class BudgetItemComparator implements Comparator<IFlexible> {

        @Override
        public int compare(IFlexible v1, IFlexible v2) {
            int result = 0;
            if (v1 instanceof BudgetItem && v2 instanceof BudgetItem) {
                result = ((BudgetItem) v1).getModel().getTitle().compareTo(((BudgetItem) v2)
                        .getModel().getTitle());
            }

            return result;
        }
    }
}
