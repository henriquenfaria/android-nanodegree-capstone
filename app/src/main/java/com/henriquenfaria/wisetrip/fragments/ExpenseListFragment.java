package com.henriquenfaria.wisetrip.fragments;

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
import com.henriquenfaria.wisetrip.adapters.ExpenseAdapter;
import com.henriquenfaria.wisetrip.models.ExpenseModel;
import com.henriquenfaria.wisetrip.models.TripModel;

import org.zakariya.stickyheaders.StickyHeaderLayoutManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ExpenseListFragment extends BaseFragment {
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
    private ExpenseAdapter mExpenseAdapter;
    private ChildEventListener mExpensesEventListener;
    private TripModel mTrip;

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

        mExpenseListRecyclerView.setLayoutManager(new StickyHeaderLayoutManager());

        // mExpenseListRecyclerView.setEmptyView(mEmptyExpenseListText);
        // mExpenseListRecyclerView.setHasFixedSize(false);


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


       // TODO: Implement adapter using FlexibleAdapter
       // mExpenseAdapter = new ExpenseAdapter(mTrip);
       // mExpenseListRecyclerView.setAdapter(mExpenseAdapter);

        attachDatabaseReadListener();

        return rootView;
    }



    private void attachDatabaseReadListener() {
        if (mExpensesEventListener == null) {
            mExpensesEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildAdded");

                    ExpenseModel expense = dataSnapshot.getValue(ExpenseModel.class);
                    if (expense != null && !TextUtils.isEmpty(expense.getId())) {
                        //mExpenseAdapter.addExpense(expense);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildChanged");
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Timber.d("onChildRemoved");
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
}
