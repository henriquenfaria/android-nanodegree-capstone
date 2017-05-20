package com.henriquenfaria.wisetrip.activities;


import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.data.TravelerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;


public class TravelerActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.traveler_recycler_view)
    RecyclerView mTravelerRecyclerView;

    private TravelerAdapter mTravelerAdapter;
    private LinearLayoutManager mLayoutManager;
    private static final int ID_TASK_CONTACTS = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traveler);

        ButterKnife.bind(this);

        mTravelerAdapter = new TravelerAdapter(null);
        mTravelerRecyclerView.setHasFixedSize(true);
        mTravelerRecyclerView.setAdapter(mTravelerAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mTravelerRecyclerView.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mTravelerRecyclerView.getContext(),
                        mLayoutManager.getOrientation());
        mTravelerRecyclerView.addItemDecoration(dividerItemDecoration);


        getSupportLoaderManager().initLoader(ID_TASK_CONTACTS, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define the columns to retrieve
        String[] projectionFields = new String[]{ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
        // Construct the loader
        CursorLoader cursorLoader = new CursorLoader(TravelerActivity.this,
                ContactsContract.Contacts.CONTENT_URI, // URI
                projectionFields, // projection fields
                null, // the selection criteria
                null, // the selection args
                null // the sort order
        );
        // Return the loader for use
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mTravelerAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTravelerAdapter.swapCursor(null);

    }
}
