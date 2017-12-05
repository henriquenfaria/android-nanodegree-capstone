package com.henriquenfaria.wisetrip.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.data.FirebaseDbContract;
import com.henriquenfaria.wisetrip.flexibles.TripHeader;
import com.henriquenfaria.wisetrip.flexibles.TripItem;
import com.henriquenfaria.wisetrip.models.DestinationModel;
import com.henriquenfaria.wisetrip.models.HeaderModel;
import com.henriquenfaria.wisetrip.models.TripModel;
import com.henriquenfaria.wisetrip.services.PlacePhotoIntentService;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;
import com.henriquenfaria.wisetrip.views.SizeAwareRecyclerView;

import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;
import timber.log.Timber;

/**
 * Fragment that displays a budget list
 */
public class TripListFragment extends FirebaseBaseFragment implements
        FlexibleAdapter.OnUpdateListener {


    @BindView(R.id.trip_list_recycler_view)
    protected SizeAwareRecyclerView mTripListRecyclerView;

    @BindView(R.id.empty_trip_list_text)
    protected TextView mEmptyTripListText;

    private DatabaseReference mTripsReference;
    private ChildEventListener mTripsEventListener;
    private PlacePhotoReceiver mPlacePhotoReceiver;
    private FlexibleAdapter<IFlexible> mTripAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTripsReference = mRootReference
                .child(FirebaseDbContract.Trips.PATH_TRIPS)
                .child(mCurrentUser.getUid());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_trip_list, container, false);
        ButterKnife.bind(this, rootView);

        // TODO: If date is properly indexed, use:
        // https://github.com/firebase/FirebaseUI-Android/blob/master/database/README.md
       /* new FirebaseIndexRecyclerAdapter<mTripAdapter, TripFirebaseHolder>(mTripAdapter.class,
                R.layout.trip_item,
                TripFirebaseHolder.class,
                keyRef, // The Firebase location containing the list of keys to be found in dataRef.
                dataRef) //The Firebase location to watch for data changes. Each key key found at
                 keyRef's location represents a list item in the RecyclerView.
         */

        mTripListRecyclerView.setLayoutManager(new LinearLayoutManager(mFragmentActivity));
        mTripListRecyclerView.setHasFixedSize(false);

        mTripAdapter = new FlexibleAdapter<>(null, this);
        mTripAdapter
                .setDisplayHeadersAtStartUp(true)
                .setUnlinkAllItemsOnRemoveHeaders(true);

        mTripListRecyclerView.setEmptyView(mEmptyTripListText);
        mTripListRecyclerView.setAdapter(mTripAdapter);
        mPlacePhotoReceiver = new PlacePhotoReceiver();

        // TODO: Move to onCreate()?
        attachDatabaseReadListener();

        return rootView;
    }

    private void attachDatabaseReadListener() {
        if (mTripsEventListener == null) {
            mTripsEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildAdded");

                    TripModel trip = dataSnapshot.getValue(TripModel.class);
                    if (trip != null && !TextUtils.isEmpty(trip.getId())
                            && mTripAdapter != null) {
                        tripAdded(trip);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildChanged");

                    TripModel trip = dataSnapshot.getValue(TripModel.class);
                    if (trip != null && !TextUtils.isEmpty(trip.getId())
                            && mTripAdapter != null) {
                        tripChanged(trip);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Timber.d("onChildRemoved");

                    TripModel trip = dataSnapshot.getValue(TripModel.class);
                    if (trip != null && !TextUtils.isEmpty(trip.getId())
                            && mTripAdapter != null) {
                        tripRemoved(trip);
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

            mTripsReference.addChildEventListener(mTripsEventListener);
        }
    }

    private void tripAdded(TripModel trip) {
        TripHeader headerHolder = getHeaderForTrip(trip);

        // Add new section
        if (headerHolder == null) {
            HeaderModel header = new HeaderModel();
            long now = DateTime.now().getMillis();
            header.setTitle(trip.getStateName(mFragmentActivity, now));
            header.setId((long) trip.getState(now).ordinal());
            headerHolder = new TripHeader(header);
        }
        TripItem itemHolder = new TripItem(trip, headerHolder);
        mTripAdapter.addItemToSection(itemHolder, headerHolder, new TripListFragment
                .TripItemComparator());

        Intent placePhotoIntentService = new Intent(mFragmentActivity,
                PlacePhotoIntentService.class);
        placePhotoIntentService.setAction(Constants.Action.ACTION_ADD_PHOTO);
        List<DestinationModel> destinations = trip.getDestinations();
        if (destinations.size() > 0) {
            placePhotoIntentService.putExtra(Constants.Extra.EXTRA_TRIP,
                    (Parcelable) trip);
            mFragmentActivity.startService(placePhotoIntentService);
        }
    }

    private void tripChanged(TripModel trip) {
        HeaderModel header = new HeaderModel();
        long now = DateTime.now().getMillis();
        header.setTitle(trip.getStateName(mFragmentActivity, now));
        header.setId((long) trip.getState(now).ordinal());
        TripHeader tripHeader = new TripHeader(header);
        TripItem tripItem = new TripItem(trip, tripHeader);

        TripItem retrievedItem = (TripItem) mTripAdapter
                .getItem(mTripAdapter.getGlobalPositionOf(tripItem));

        if (retrievedItem != null) {
            if (retrievedItem.getModel().getStartDate().equals(trip.getStartDate())
                    && retrievedItem.getModel().getEndDate().equals(trip.getEndDate())) {
                // No section change, just update the trip
                mTripAdapter.updateItem(tripItem);
            } else {
                // Move it to a new Section
                TripHeader destinationHeader = getHeaderForTrip(trip);
                tripRemoved(trip);
                if (destinationHeader != null) {
                    tripHeader = destinationHeader;
                    tripItem = new TripItem(trip, tripHeader);
                }
                mTripAdapter.addItemToSection(tripItem, tripHeader,
                        new TripItemComparator());
            }
        } else {
            mTripAdapter.updateItem(tripItem);
        }

        Intent placePhotoIntentService = new Intent(mFragmentActivity,
                PlacePhotoIntentService.class);
        placePhotoIntentService.setAction(Constants.Action.ACTION_CHANGE_PHOTO);
        placePhotoIntentService.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable)
                trip);
        mFragmentActivity.startService(placePhotoIntentService);

        Utils.updateAppWidgets(mFragmentActivity, trip.getId(), false);
    }

    private void tripRemoved(TripModel trip) {
        HeaderModel listHeaderModel = new HeaderModel();
        long now = DateTime.now().getMillis();
        listHeaderModel.setId((long) trip.getState(now).ordinal());
        TripHeader headerHolder = new TripHeader(listHeaderModel);
        TripItem itemHolder = new TripItem(trip, headerHolder);
        int position = mTripAdapter.getGlobalPositionOf(itemHolder);
        if (position >= 0) {
            IHeader header = mTripAdapter.getSectionHeader(position);
            mTripAdapter.removeItem(position);

            // Remove empty section
            if (header != null && mTripAdapter.getSectionItems(header).size() == 0) {
                mTripAdapter.removeItem(
                        mTripAdapter.getGlobalPositionOf(header));
            }
        }

        Intent placePhotoIntentService = new Intent(mFragmentActivity,
                PlacePhotoIntentService.class);
        placePhotoIntentService.setAction(Constants.Action.ACTION_REMOVE_PHOTO);
        placePhotoIntentService.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable)
                trip);
        mFragmentActivity.startService(placePhotoIntentService);

        Utils.updateAppWidgets(mFragmentActivity, trip.getId(), true);
    }

    // TODO: Optimize with binary search?
    private TripHeader getHeaderForTrip(TripModel trip) {
        List<IHeader> headerList = mTripAdapter.getHeaderItems();
        if (!headerList.isEmpty()) {
            for (IHeader header : headerList) {
                if (header instanceof TripHeader) {
                    //TODO: Is this comparison correct? Compering Long to int. If if works it is ok!
                    if (((TripHeader) header).getModel().getId()
                            == trip.getState(DateTime.now().getMillis()).ordinal()) {
                        return (TripHeader) header;
                    }
                }
            }
        }

        return null;
    }

    private void detachDatabaseReadListener() {
        if (mTripsEventListener != null) {
            mTripsReference.removeEventListener(mTripsEventListener);
            mTripsEventListener = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        detachDatabaseReadListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlacePhotoReceiver != null) {
            LocalBroadcastManager.getInstance(mFragmentActivity)
                    .registerReceiver(mPlacePhotoReceiver,
                            new IntentFilter(Constants.Action.ACTION_UPDATE_TRIP_LIST));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlacePhotoReceiver != null) {
            LocalBroadcastManager.getInstance(mFragmentActivity)
                    .unregisterReceiver(mPlacePhotoReceiver);
        }
    }

    @Override
    public void onUpdateEmptyView(int size) {
        if (size > 0) {
            ViewCompat.animate(mEmptyTripListText).alpha(0);
        } else {
            ViewCompat.animate(mEmptyTripListText).alpha(1);
        }
    }

    private class PlacePhotoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Constants.Action.ACTION_UPDATE_TRIP_LIST)) {
                if (intent.hasExtra(Constants.Extra.EXTRA_TRIP)) {
                    TripModel trip = intent.getParcelableExtra(Constants.Extra.EXTRA_TRIP);
                    HeaderModel header = new HeaderModel();
                    long now = DateTime.now().getMillis();
                    header.setId((long) trip.getState(now).ordinal());
                    TripHeader tripHeader = new TripHeader(header);
                    TripItem tripItem = new TripItem(trip, tripHeader);
                    mTripAdapter.updateItem(tripItem, false);
                }
            }
        }
    }

    private class TripItemComparator implements Comparator<IFlexible> {

        @Override
        public int compare(IFlexible v1, IFlexible v2) {
            int result = 0;
            if (v1 instanceof TripHeader && v2 instanceof TripHeader) {
                result = ((TripHeader) v2).getModel().getId().compareTo(((TripHeader) v1)
                        .getModel().getId());
            } else if (v1 instanceof TripItem && v2 instanceof TripItem) {
                result = ((TripItem) v2).getHeader().getModel().getId().compareTo((
                        (TripItem) v1).getHeader().getModel().getId());

                if (result == 0) {
                    result = ((TripItem) v2).getModel().getStartDate().compareTo(((TripItem) v1)
                            .getModel().getStartDate());
                }
            } else if (v1 instanceof TripItem && v2 instanceof TripHeader) {
                result = ((TripHeader) v2).getModel().getId().compareTo(((TripItem) v1)
                        .getHeader().getModel().getId());
                if (result == 0) {
                    result--;
                }
            } else if (v1 instanceof TripHeader && v2 instanceof TripItem) {
                result = ((TripItem) v2).getHeader().getModel().getId().compareTo((
                        (TripHeader) v1).getModel().getId());
                if (result == 0) {
                    result--;
                }
            }
            return result;
        }
    }
}
