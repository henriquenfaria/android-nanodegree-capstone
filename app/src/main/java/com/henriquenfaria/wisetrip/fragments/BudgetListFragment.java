package com.henriquenfaria.wisetrip.fragments;

import android.content.Context;
import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.flexibles.BudgetItem;
import com.henriquenfaria.wisetrip.listeners.OnBudgetInteractionListener;
import com.henriquenfaria.wisetrip.models.BudgetModel;
import com.henriquenfaria.wisetrip.models.TripModel;

import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.IFlexible;
import timber.log.Timber;

public class BudgetListFragment extends BaseFragment implements
        FlexibleAdapter.OnItemClickListener,
        FlexibleAdapter.OnUpdateListener {
    private static final String ARG_TRIP = "arg_trip";

    @BindView(R.id.budget_list_layout)
    protected FrameLayout mBudgetListLayout;

    @BindView(R.id.budget_list_recycler_view)
    protected RecyclerView mBudgetListRecyclerView;

    @BindView(R.id.empty_view)
    protected RelativeLayout mEmptyListView;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBudgetReference;
    private FirebaseUser mCurrentUser;
    private FlexibleAdapter<IFlexible> mBudgetAdapter;
    private ValueEventListener mValueEventListener;
    private ChildEventListener mChildEventListener;
    private TripModel mTrip;
    private OnBudgetInteractionListener mOnBudgetInteractionListener;

    // Create new Fragment instance with TripModel info
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

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        //TODO: Need to order by child?
        mBudgetReference = mFirebaseDatabase.getReference()
                .child("budgets")
                .child(mCurrentUser.getUid())
                .child(mTrip.getId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_budget_list, container, false);
        ButterKnife.bind(this, rootView);

        // TODO: If date is properly indexed, use:
        // https://github.com/firebase/FirebaseUI-Android/blob/master/database/README.md
       /* new FirebaseIndexRecyclerAdapter<mBudgetAdapter, TripFirebaseHolder>(mBudgetAdapter
       .class,
                R.layout.trip_item,
                TripFirebaseHolder.class,
                keyRef, // The Firebase location containing the list of keys to be found in dataRef.
                dataRef) //The Firebase location to watch for data changes. Each key key found at
                 keyRef's location represents a list item in the RecyclerView.
         */


        mBudgetAdapter = new FlexibleAdapter<>(null, this);
        /*mBudgetAdapter
                .setDisplayHeadersAtStartUp(true)
                .setStickyHeaders(true)
                .setUnlinkAllItemsOnRemoveHeaders(true);*/

        mBudgetListRecyclerView.setLayoutManager(
                new SmoothScrollLinearLayoutManager(mFragmentActivity));
        mBudgetListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mBudgetListRecyclerView.setAdapter(mBudgetAdapter);


        //TODO: Must uncomment fastScroller logic
      /*  FastScroller fastScroller = getView().findViewById(R.id.fast_scroller);
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

    // TODO: Optimize with binary search?
    /*private ExpenseHeader getHeaderForExpense(ExpenseModel expense) {
        List<IHeader> headerList = mBudgetAdapter.getHeaderItems();
        if (!headerList.isEmpty()) {
            for (IHeader header : headerList) {
                if (header instanceof ExpenseHeader) {
                    if (((ExpenseHeader) header).getModel().getId()
                            .equals(expense.getDate())) {
                        return (ExpenseHeader) header;
                    }
                }
            }
        }

        return null;
    }*/

    // TODO: Move attach/detach to onResume and on onPause.
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
                        addBudget(budget);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildChanged");

                    BudgetModel budget = dataSnapshot.getValue(BudgetModel.class);
                    if (budget != null && !TextUtils.isEmpty(budget.getId())
                            && mBudgetAdapter != null) {
                        changeBudget(budget);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Timber.d("onChildRemoved");

                    BudgetModel budget = dataSnapshot.getValue(BudgetModel.class);
                    if (budget != null && !TextUtils.isEmpty(budget.getId())
                            && mBudgetAdapter != null) {
                        removeBudget(budget);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildMoved");
                    //TODO: Implementation needed?
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Timber.d("onCancelled");
                    //TODO: Implementation needed?
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
                    Timber.d("onCancelled");

                    mBudgetListLayout.setVisibility(View.VISIBLE);
                }
            };
            mBudgetReference.addListenerForSingleValueEvent(mValueEventListener);
        }
    }


    private void addBudget(BudgetModel budget) {
        // TODO: Implement
        /*ExpenseHeader headerHolder = getHeaderForExpense(expense);

        // Add new section
        if (headerHolder == null) {
            ExpenseHeaderModel headerModel = new ExpenseHeaderModel();
            DateTime dateTime = new DateTime(expense.getDate());
            String formattedDateTime = dateTime.toString(DateTimeFormat
                    .mediumDate());
            headerModel.setTitle(formattedDateTime);
            headerModel.setId(expense.getDate());
            headerHolder = new ExpenseHeader(headerModel);
        }
        ExpenseItem itemHolder = new ExpenseItem(expense, headerHolder);
        mBudgetAdapter.addItemToSection(itemHolder, headerHolder, new BudgetItemComparator());*/

    }


    private void changeBudget(BudgetModel budget) {
        // TODO: Implement
       /* ExpenseHeaderModel headerModel = new ExpenseHeaderModel();
        DateTime dateTime = new DateTime(expense.getDate());
        String formattedDateTime = dateTime.toString(DateTimeFormat.mediumDate());
        headerModel.setTitle(formattedDateTime);
        headerModel.setId(expense.getDate());
        ExpenseHeader expenseHeader = new ExpenseHeader(headerModel);
        ExpenseItem expenseItem = new ExpenseItem(expense, expenseHeader);

        ExpenseItem retrievedItem = (ExpenseItem) mBudgetAdapter
                .getItem(mBudgetAdapter.getGlobalPositionOf(expenseItem));
        if (retrievedItem != null) {
            if (retrievedItem.getModel().getDate().equals(expense.getDate())) {
                // No section change, just update the expense
                mBudgetAdapter.updateItem(expenseItem);
            } else {
                // Move it to a new Section
                ExpenseHeader destinationHeader = getHeaderForExpense(expense);
                removeBudget(expense);
                if (destinationHeader != null) {
                    expenseHeader = destinationHeader;
                    expenseItem = new ExpenseItem(expense, expenseHeader);
                }
                mBudgetAdapter.addItemToSection(expenseItem, expenseHeader,
                        new BudgetItemComparator());
            }
        } else {
            mBudgetAdapter.updateItem(expenseItem);
        }*/
    }

    private void removeBudget(BudgetModel budget) {
        // TODO: Implement
        /*ExpenseHeaderModel headerModel = new ExpenseHeaderModel();
        headerModel.setId(expense.getDate());
        ExpenseHeader headerHolder = new ExpenseHeader(headerModel);
        ExpenseItem itemHolder = new ExpenseItem(expense, headerHolder);
        int position = mBudgetAdapter.getGlobalPositionOf(itemHolder);
        if (position >= 0) {
            IHeader header = mBudgetAdapter.getSectionHeader(position);
            mBudgetAdapter.removeItem(position);

            // Remove empty section
            if (header != null && mBudgetAdapter.getSectionItems(header).size()
                    == 0) {
                mBudgetAdapter.removeItem(
                        mBudgetAdapter.getGlobalPositionOf(header));
            }
        }*/
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
            ViewCompat.animate(mEmptyListView).alpha(0);
        } else {
            ViewCompat.animate(mEmptyListView).alpha(1);
        }
    }

    private class BudgetItemComparator implements Comparator<IFlexible> {

        @Override
        public int compare(IFlexible v1, IFlexible v2) {
            int result = 0;
            if (v1 instanceof BudgetItem && v2 instanceof BudgetItem) {
               /* result = ((BudgetItem) v2).getHeader().getModel().getId().compareTo((
                        (BudgetItem) v1).getHeader().getModel().getId());*/

                // TODO: Add a modified timestamp for the expense and use it in the comparison
                // Current logic below it not ok for updated objects,
                // since they are put in the middle of section
                // Update timestamp only on add or on update where the expense date was changed
                //if (result == 0) {
                    result = ((BudgetItem) v1).getModel().getTitle().compareTo(((BudgetItem) v2)
                            .getModel().getTitle());
               // }
            }


            /*if (v1 instanceof ExpenseHeader && v2 instanceof ExpenseHeader) {
                result = ((ExpenseHeader) v2).getModel().getId().compareTo(((ExpenseHeader) v1)
                        .getModel().getId());
            if (v1 instanceof ExpenseItem && v2 instanceof ExpenseItem) {
                result = ((ExpenseItem) v2).getHeader().getModel().getId().compareTo((
                        (ExpenseItem) v1).getHeader().getModel().getId());

                // TODO: Add a modified timestamp for the expense and use it in the comparison
                // Current logic below it not ok for updated objects,
                // since they are put in the middle of section
                // Update timestamp only on add or on update where the expense date was changed
                if (result == 0) {
                    result = ((ExpenseItem) v2).getModel().getId().compareTo(((ExpenseItem) v1)
                            .getModel().getId());
                }
            } else if (v1 instanceof ExpenseItem && v2 instanceof ExpenseHeader) {

                result = ((ExpenseHeader) v2).getModel().getId().compareTo(((ExpenseItem) v1)
                        .getHeader().getModel().getId());
                if (result == 0) {
                    result--;
                }
            } else if (v1 instanceof ExpenseHeader && v2 instanceof ExpenseItem) {

                result = ((ExpenseItem) v2).getHeader().getModel().getId().compareTo((
                        (ExpenseHeader) v1).getModel().getId());
                if (result == 0) {
                    result--;
                }
            }*/
            return result;
        }
    }
}
