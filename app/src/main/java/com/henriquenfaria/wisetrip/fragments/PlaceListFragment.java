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
import com.henriquenfaria.wisetrip.data.FirebaseDbContract;
import com.henriquenfaria.wisetrip.flexibles.PlaceHeader;
import com.henriquenfaria.wisetrip.flexibles.PlaceItem;
import com.henriquenfaria.wisetrip.listeners.OnPlaceInteractionListener;
import com.henriquenfaria.wisetrip.models.HeaderModel;
import com.henriquenfaria.wisetrip.models.PlaceModel;
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

public class PlaceListFragment extends BaseFragment implements
        FlexibleAdapter.OnItemClickListener,
        FlexibleAdapter.OnUpdateListener {

    private static final String ARG_TRIP = "arg_trip";

    @BindView(R.id.place_list_layout)
    protected FrameLayout mPlaceListLayout;

    @BindView(R.id.place_list_recycler_view)
    protected RecyclerView mPlaceListRecyclerView;

    @BindView(R.id.empty_view)
    protected RelativeLayout mEmptyView;

    @BindView(R.id.empty_text)
    protected TextView mEmptyText;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPlacesReference;
    private FirebaseUser mCurrentUser;
    private FlexibleAdapter<IFlexible> mPlacesAdapter;
    private ValueEventListener mValueEventListener;
    private ChildEventListener mChildEventListener;
    private TripModel mTrip;
    private OnPlaceInteractionListener mOnPlacesInteractionListener;

    // Create new Fragment instance with PlaceModel info
    public static PlaceListFragment newInstance(TripModel trip) {
        PlaceListFragment fragment = new PlaceListFragment();
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

        mPlacesReference = mFirebaseDatabase.getReference()
                .child(FirebaseDbContract.Places.PATH_PLACES)
                .child(mCurrentUser.getUid())
                .child(mTrip.getId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_place_list, container, false);
        ButterKnife.bind(this, rootView);

        // TODO: If date is properly indexed, use:
        // https://github.com/firebase/FirebaseUI-Android/blob/master/database/README.md
       /* new FirebaseIndexRecyclerAdapter<mPlacesAdapter, TripFirebaseHolder>(mPlacesAdapter
       .class,
                R.layout.trip_item,
                TripFirebaseHolder.class,
                keyRef, // The Firebase location containing the list of keys to be found in dataRef.
                dataRef) //The Firebase location to watch for data changes. Each key key found at
                 keyRef's location represents a list item in the RecyclerView.
         */

        mEmptyText.setText(R.string.no_saved_places);

        mPlacesAdapter = new FlexibleAdapter<>(null, this);
        mPlacesAdapter
                .setDisplayHeadersAtStartUp(true)
                .setStickyHeaders(true)
                .setUnlinkAllItemsOnRemoveHeaders(true);

        mPlaceListRecyclerView.setLayoutManager(
                new SmoothScrollLinearLayoutManager(mFragmentActivity));
        mPlaceListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPlaceListRecyclerView.setAdapter(mPlacesAdapter);


        //TODO: Must uncomment fastScroller logic
      /*  FastScroller fastScroller = getView().findViewById(R.id.fast_scroller);
        fastScroller.addOnScrollStateChangeListener((MainActivity) getActivity());
        mPlacesAdapter.setFastScroller(fastScroller);*/

        attachDatabaseReadListener();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnPlaceInteractionListener) {
            mOnPlacesInteractionListener = (OnPlaceInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlaceInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnPlacesInteractionListener = null;
    }

    @Override
    public boolean onItemClick(int position) {
        IFlexible flexibleItem = mPlacesAdapter.getItem(position);
        if (flexibleItem instanceof PlaceItem) {
            PlaceItem placeItem = (PlaceItem) flexibleItem;
            PlaceModel place = placeItem.getModel();
            if (mOnPlacesInteractionListener != null) {
                mOnPlacesInteractionListener.onPlaceClicked(place);
            }
            return false;
        }

        return false;
    }

    // TODO: Optimize with binary search?
    private PlaceHeader getHeaderForPlace(PlaceModel places) {
        List<IHeader> headerList = mPlacesAdapter.getHeaderItems();
        if (!headerList.isEmpty()) {
            for (IHeader header : headerList) {
                if (header instanceof PlaceHeader) {
                    if (((PlaceHeader) header).getModel().getId()
                            .equals(places.getDate())) {
                        return (PlaceHeader) header;
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

                    PlaceModel place = dataSnapshot.getValue(PlaceModel.class);
                    if (place != null && !TextUtils.isEmpty(place.getId())
                            && mPlacesAdapter != null) {
                        placeAdded(place);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildChanged");

                    PlaceModel place = dataSnapshot.getValue(PlaceModel.class);
                    if (place != null && !TextUtils.isEmpty(place.getId())
                            && mPlacesAdapter != null) {
                        placeChanged(place);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Timber.d("onChildRemoved");

                    PlaceModel place = dataSnapshot.getValue(PlaceModel.class);
                    if (place != null && !TextUtils.isEmpty(place.getId())
                            && mPlacesAdapter != null) {
                        placeRemoved(place);
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
            mPlacesReference.addChildEventListener(mChildEventListener);
        }

        // To disable weird animations until all data is retrieved
        // MUST be added after mChildEventListener
        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Timber.d("onDataChange");

                    mPlaceListLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Timber.d("onCancelled", databaseError.getMessage());

                    mPlaceListLayout.setVisibility(View.VISIBLE);
                }
            };
            mPlacesReference.addListenerForSingleValueEvent(mValueEventListener);
        }
    }

    private void placeAdded(PlaceModel place) {
        PlaceHeader headerHolder = getHeaderForPlace(place);

        // Add new section
        if (headerHolder == null) {
            HeaderModel header = new HeaderModel();
            DateTime dateTime = new DateTime(place.getDate());
            String formattedDateTime = dateTime.toString(DateTimeFormat
                    .mediumDate());
            header.setTitle(formattedDateTime);
            header.setId(place.getDate());
            headerHolder = new PlaceHeader(header);
        }
        PlaceItem itemHolder = new PlaceItem(place, headerHolder);
        mPlacesAdapter.addItemToSection(itemHolder, headerHolder, new PlaceItemComparator());
    }

    private void placeChanged(PlaceModel place) {
        HeaderModel header = new HeaderModel();
        DateTime dateTime = new DateTime(place.getDate());
        String formattedDateTime = dateTime.toString(DateTimeFormat.mediumDate());
        header.setTitle(formattedDateTime);
        header.setId(place.getDate());
        PlaceHeader placeHeader = new PlaceHeader(header);
        PlaceItem placeItem = new PlaceItem(place, placeHeader);

        PlaceItem retrievedItem = (PlaceItem) mPlacesAdapter
                .getItem(mPlacesAdapter.getGlobalPositionOf(placeItem));
        if (retrievedItem != null) {
            if (retrievedItem.getModel().getDate().equals(place.getDate())) {
                // No section change, just update the place
                mPlacesAdapter.updateItem(placeItem);
            } else {
                // Move it to a new Section
                PlaceHeader destinationHeader = getHeaderForPlace(place);
                placeRemoved(place);
                if (destinationHeader != null) {
                    placeHeader = destinationHeader;
                    placeItem = new PlaceItem(place, placeHeader);
                }
                mPlacesAdapter.addItemToSection(placeItem, placeHeader,
                        new PlaceItemComparator());
            }
        } else {
            mPlacesAdapter.updateItem(placeItem);
        }
    }

    private void placeRemoved(PlaceModel place) {
        HeaderModel listHeaderModel = new HeaderModel();
        listHeaderModel.setId(place.getDate());
        PlaceHeader headerHolder = new PlaceHeader(listHeaderModel);
        PlaceItem itemHolder = new PlaceItem(place, headerHolder);
        int position = mPlacesAdapter.getGlobalPositionOf(itemHolder);
        if (position >= 0) {
            IHeader header = mPlacesAdapter.getSectionHeader(position);
            mPlacesAdapter.removeItem(position);

            // Remove empty section
            if (header != null && mPlacesAdapter.getSectionItems(header).size() == 0) {
                mPlacesAdapter.removeItem(
                        mPlacesAdapter.getGlobalPositionOf(header));
            }
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mPlacesReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        if (mValueEventListener != null) {
            mPlacesReference.removeEventListener(mValueEventListener);
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

    private class PlaceItemComparator implements Comparator<IFlexible> {

        @Override
        public int compare(IFlexible v1, IFlexible v2) {
            int result = 0;
            if (v1 instanceof PlaceHeader && v2 instanceof PlaceHeader) {
                result = ((PlaceHeader) v2).getModel().getId().compareTo(((PlaceHeader) v1)
                        .getModel().getId());
            } else if (v1 instanceof PlaceItem && v2 instanceof PlaceItem) {
                result = ((PlaceItem) v2).getHeader().getModel().getId().compareTo((
                        (PlaceItem) v1).getHeader().getModel().getId());

                // TODO: Add a modified timestamp for the place and use it in the comparison
                // Current logic below it not ok for updated objects,
                // since they are put in the middle of section
                // Update timestamp only on add or on update where the place date was changed
                if (result == 0) {
                    result = ((PlaceItem) v2).getModel().getId().compareTo(((PlaceItem) v1)
                            .getModel().getId());
                }
            } else if (v1 instanceof PlaceItem && v2 instanceof PlaceHeader) {

                result = ((PlaceHeader) v2).getModel().getId().compareTo(((PlaceItem) v1)
                        .getHeader().getModel().getId());
                if (result == 0) {
                    result--;
                }
            } else if (v1 instanceof PlaceHeader && v2 instanceof PlaceItem) {

                result = ((PlaceItem) v2).getHeader().getModel().getId().compareTo((
                        (PlaceHeader) v1).getModel().getId());
                if (result == 0) {
                    result--;
                }
            }
            return result;
        }
    }
}
