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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.flexibles.ExpenseHeader;
import com.henriquenfaria.wisetrip.flexibles.ExpenseItem;
import com.henriquenfaria.wisetrip.listeners.OnExpenseInteractionListener;
import com.henriquenfaria.wisetrip.models.ExpenseHeaderModel;
import com.henriquenfaria.wisetrip.models.ExpenseModel;
import com.henriquenfaria.wisetrip.models.TripModel;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import timber.log.Timber;

public class ExpenseListFragment extends BaseFragment implements
        FlexibleAdapter.OnItemClickListener,
        FlexibleAdapter.OnUpdateListener {
    private static final String ARG_TRIP = "arg_trip";

    @BindView(R.id.expense_list_layout)
    protected FrameLayout mExpenseListLayout;

    @BindView(R.id.expense_list_recycler_view)
    protected RecyclerView mExpenseListRecyclerView;

    @BindView(R.id.empty_view)
    protected RelativeLayout mEmptyView;

    @BindView(R.id.empty_text)
    protected TextView mEmptyText;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mExpensesReference;
    private FirebaseUser mCurrentUser;
    private FlexibleAdapter<IFlexible> mExpenseAdapter;
    private ValueEventListener mValueEventListener;
    private ChildEventListener mChildEventListener;
    private TripModel mTrip;
    private OnExpenseInteractionListener mOnExpenseInteractionListener;

    // Create new Fragment instance with TripModel info
    public static ExpenseListFragment newInstance(TripModel trip) {
        ExpenseListFragment fragment = new ExpenseListFragment();
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

        mExpensesReference = mFirebaseDatabase.getReference()
                .child("expenses")
                .child(mCurrentUser.getUid())
                .child(mTrip.getId());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_expense_list, container, false);
        ButterKnife.bind(this, rootView);

        // TODO: If date is properly indexed, use:
        // https://github.com/firebase/FirebaseUI-Android/blob/master/database/README.md
       /* new FirebaseIndexRecyclerAdapter<mExpenseAdapter, TripFirebaseHolder>(mExpenseAdapter
       .class,
                R.layout.trip_item,
                TripFirebaseHolder.class,
                keyRef, // The Firebase location containing the list of keys to be found in dataRef.
                dataRef) //The Firebase location to watch for data changes. Each key key found at
                 keyRef's location represents a list item in the RecyclerView.
         */

        mEmptyText.setText(R.string.no_saved_expenses);

        mExpenseAdapter = new FlexibleAdapter<>(null, this);
        mExpenseAdapter
                .setDisplayHeadersAtStartUp(true)
                .setStickyHeaders(true)
                .setUnlinkAllItemsOnRemoveHeaders(true);

        mExpenseListRecyclerView.setLayoutManager(
                new SmoothScrollLinearLayoutManager(mFragmentActivity));
        mExpenseListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mExpenseListRecyclerView.setAdapter(mExpenseAdapter);


        //TODO: Must uncomment fastScroller logic
      /*  FastScroller fastScroller = getView().findViewById(R.id.fast_scroller);
        fastScroller.addOnScrollStateChangeListener((MainActivity) getActivity());
        mExpenseAdapter.setFastScroller(fastScroller);*/

        attachDatabaseReadListener();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnExpenseInteractionListener) {
            mOnExpenseInteractionListener = (OnExpenseInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnExpenseInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnExpenseInteractionListener = null;
    }

    @Override
    public boolean onItemClick(int position) {
        IFlexible flexibleItem = mExpenseAdapter.getItem(position);
        if (flexibleItem instanceof ExpenseItem) {
            ExpenseItem expenseItem = (ExpenseItem) flexibleItem;
            ExpenseModel expense = expenseItem.getModel();
            if (mOnExpenseInteractionListener != null) {
                mOnExpenseInteractionListener.onExpenseClicked(expense);
            }
            return false;
        }

        return false;
    }

    // TODO: Optimize with binary search?
    private ExpenseHeader getHeaderForExpense(ExpenseModel expense) {
        List<IHeader> headerList = mExpenseAdapter.getHeaderItems();
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
    }

    // TODO: Move attach/detach to onResume and on onPause.
    // Preserve listener instances (Serializable) to avoid getting items again on orientation change
    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildAdded");

                    ExpenseModel expense = dataSnapshot.getValue(ExpenseModel.class);
                    if (expense != null && !TextUtils.isEmpty(expense.getId())
                            && mExpenseAdapter != null) {
                        expenseAdded(expense);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildChanged");

                    ExpenseModel expense = dataSnapshot.getValue(ExpenseModel.class);
                    if (expense != null && !TextUtils.isEmpty(expense.getId())
                            && mExpenseAdapter != null) {
                        expenseChanged(expense);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Timber.d("onChildRemoved");

                    ExpenseModel expense = dataSnapshot.getValue(ExpenseModel.class);
                    if (expense != null && !TextUtils.isEmpty(expense.getId())
                            && mExpenseAdapter != null) {
                        expenseRemoved(expense);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildMoved");
                    //TODO: Implementation needed?
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Timber.d("onCancelled", databaseError.getMessage());
                }

            };
            mExpensesReference.addChildEventListener(mChildEventListener);
        }

        // To disable weird animations until all data is retrieved
        // MUST be added after mChildEventListener
        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Timber.d("onDataChange");

                    mExpenseListLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Timber.d("onCancelled", databaseError.getMessage());

                    mExpenseListLayout.setVisibility(View.VISIBLE);
                }
            };
            mExpensesReference.addListenerForSingleValueEvent(mValueEventListener);
        }
    }


    private void expenseAdded(ExpenseModel expense) {
        ExpenseHeader headerHolder = getHeaderForExpense(expense);

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
        mExpenseAdapter.addItemToSection(itemHolder, headerHolder, new ExpenseItemComparator());

    }


    private void expenseChanged(ExpenseModel expense) {
        ExpenseHeaderModel headerModel = new ExpenseHeaderModel();
        DateTime dateTime = new DateTime(expense.getDate());
        String formattedDateTime = dateTime.toString(DateTimeFormat.mediumDate());
        headerModel.setTitle(formattedDateTime);
        headerModel.setId(expense.getDate());
        ExpenseHeader expenseHeader = new ExpenseHeader(headerModel);
        ExpenseItem expenseItem = new ExpenseItem(expense, expenseHeader);

        ExpenseItem retrievedItem = (ExpenseItem) mExpenseAdapter
                .getItem(mExpenseAdapter.getGlobalPositionOf(expenseItem));
        if (retrievedItem != null) {
            if (retrievedItem.getModel().getDate().equals(expense.getDate())) {
                // No section change, just update the expense
                mExpenseAdapter.updateItem(expenseItem);
            } else {
                // Move it to a new Section
                ExpenseHeader destinationHeader = getHeaderForExpense(expense);
                expenseRemoved(expense);
                if (destinationHeader != null) {
                    expenseHeader = destinationHeader;
                    expenseItem = new ExpenseItem(expense, expenseHeader);
                }
                mExpenseAdapter.addItemToSection(expenseItem, expenseHeader,
                        new ExpenseItemComparator());
            }
        } else {
            mExpenseAdapter.updateItem(expenseItem);
        }
    }

    private void expenseRemoved(ExpenseModel expense) {
        ExpenseHeaderModel headerModel = new ExpenseHeaderModel();
        headerModel.setId(expense.getDate());
        ExpenseHeader headerHolder = new ExpenseHeader(headerModel);
        ExpenseItem itemHolder = new ExpenseItem(expense, headerHolder);
        int position = mExpenseAdapter.getGlobalPositionOf(itemHolder);
        if (position >= 0) {
            IHeader header = mExpenseAdapter.getSectionHeader(position);
            mExpenseAdapter.removeItem(position);

            // Remove empty section
            if (header != null && mExpenseAdapter.getSectionItems(header).size()
                    == 0) {
                mExpenseAdapter.removeItem(
                        mExpenseAdapter.getGlobalPositionOf(header));
            }
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mExpensesReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        if (mValueEventListener != null) {
            mExpensesReference.removeEventListener(mValueEventListener);
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

    private class ExpenseItemComparator implements Comparator<IFlexible> {

        @Override
        public int compare(IFlexible v1, IFlexible v2) {
            int result = 0;
            if (v1 instanceof ExpenseHeader && v2 instanceof ExpenseHeader) {
                result = ((ExpenseHeader) v2).getModel().getId().compareTo(((ExpenseHeader) v1)
                        .getModel().getId());
            } else if (v1 instanceof ExpenseItem && v2 instanceof ExpenseItem) {
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
            }
            return result;
        }
    }
}
