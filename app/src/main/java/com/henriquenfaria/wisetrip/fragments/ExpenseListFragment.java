package com.henriquenfaria.wisetrip.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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

public class ExpenseListFragment extends BaseFragment implements FlexibleAdapter
        .OnItemClickListener {
    private static final String ARG_TRIP = "arg_trip";

    // TODO: Issue regarding empty layout for Recycler... Maybe caused by StickyHeaders lib
    // Reverted back to the normal RecyclerView for now
    @BindView(R.id.expense_list_recycler_view)
    protected RecyclerView mExpenseListRecyclerView;

    @BindView(R.id.empty_expense_list_text)
    protected TextView mEmptyExpenseListText;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private Query mExpensesReference;
    private FirebaseUser mCurrentUser;
    private FlexibleAdapter<IFlexible> mExpenseAdapter;
    private ChildEventListener mExpensesEventListener;
    private TripModel mTrip;

    private OnExpenseInteractionListener mListener;

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
        //TODO: Need to order by child?
        mExpensesReference = mFirebaseDatabase.getReference()
                .child("expenses")
                .child(mCurrentUser.getUid())
                .child(mTrip.getId())
                .orderByChild("date");


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


        mExpenseAdapter = new FlexibleAdapter<>(null, this);
        mExpenseAdapter
                .setDisplayHeadersAtStartUp(true)
                .setStickyHeaders(true)
                .setUnlinkAllItemsOnRemoveHeaders(true);

        mExpenseListRecyclerView.setLayoutManager(
                new SmoothScrollLinearLayoutManager(mFragmentActivity));
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
            mListener = (OnExpenseInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnExpenseInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onItemClick(int position) {
        IFlexible flexibleItem = mExpenseAdapter.getItem(position);
        if (flexibleItem instanceof ExpenseItem) {
            ExpenseItem expenseItem = (ExpenseItem) flexibleItem;
            ExpenseModel expense = expenseItem.getModel();
            if (mListener != null) {
                mListener.onExpenseClicked(expense);
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

    private void attachDatabaseReadListener() {
        if (mExpensesEventListener == null) {
            mExpensesEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildAdded");

                    ExpenseModel expense = dataSnapshot.getValue(ExpenseModel.class);
                    if (expense != null && !TextUtils.isEmpty(expense.getId())) {
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
                        mExpenseAdapter.addItemToSection(itemHolder, headerHolder, new
                                ExpenseItemComparator());
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildChanged");
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Timber.d("onChildRemoved");

                    ExpenseModel expense = dataSnapshot.getValue(ExpenseModel.class);
                    if (expense != null && !TextUtils.isEmpty(expense.getId())) {
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
                                mExpenseAdapter.removeItem(mExpenseAdapter.getGlobalPositionOf
                                        (header));
                            }
                        }
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

            mExpensesReference.addChildEventListener(mExpensesEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mExpensesEventListener != null) {
            mExpensesReference.removeEventListener(mExpensesEventListener);
            mExpensesEventListener = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        detachDatabaseReadListener();
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
