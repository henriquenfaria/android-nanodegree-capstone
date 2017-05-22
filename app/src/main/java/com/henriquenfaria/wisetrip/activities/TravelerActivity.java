package com.henriquenfaria.wisetrip.activities;

import android.content.Intent;
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
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.data.TravelerAdapter;
import com.henriquenfaria.wisetrip.models.Traveler;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.henriquenfaria.wisetrip.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;


public class TravelerActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        TravelerAdapter.OnTravelerAdapter {

    private static final String TAG = TravelerActivity.class.getSimpleName();
    private static final int ID_TASK_CONTACTS = 1;
    private static final String SAVE_TRAVELER_KEY = "save_traveler_key";


    @BindView(R.id.traveler_recycler_view)
    RecyclerView mTravelerRecyclerView;

    private TravelerAdapter mTravelerAdapter;
    private LinearLayoutManager mLayoutManager;
    private SparseArray<Traveler> mTravelerSparseArray;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traveler);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            mTravelerSparseArray = savedInstanceState.getSparseParcelableArray(SAVE_TRAVELER_KEY);
        } else {
            mTravelerSparseArray = new SparseArray<>();
        }

        mTravelerAdapter = new TravelerAdapter(null);
        mTravelerRecyclerView.setHasFixedSize(true);
        mTravelerRecyclerView.setAdapter(mTravelerAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mTravelerRecyclerView.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mTravelerRecyclerView.getContext(),
                        mLayoutManager.getOrientation());
        mTravelerRecyclerView.addItemDecoration(dividerItemDecoration);

        setTitleCount();
        getSupportLoaderManager().initLoader(ID_TASK_CONTACTS, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_traveler_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                Intent data = new Intent();
                if (mTravelerSparseArray != null) {
                    data.putParcelableArrayListExtra(Constants.Extras.EXTRA_TRAVELER, Utils
                            .sparseArrayAsArrayList(mTravelerSparseArray));
                }
                setResult(RESULT_OK, data);
                finish();
                return true;
            case android.R.id.home:
                // To animate transition like back button press
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mTravelerAdapter != null) {
            outState.putSparseParcelableArray(SAVE_TRAVELER_KEY, mTravelerSparseArray);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projectionFields = new String[]{ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
        CursorLoader cursorLoader = new CursorLoader(TravelerActivity.this,
                ContactsContract.Contacts.CONTENT_URI,
                projectionFields,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        );
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

    @Override
    public SparseArray<Traveler> getTravelerSparseArray() {
        return mTravelerSparseArray;
    }

    @Override
    public void setTravelerSparseArray(SparseArray<Traveler> travelerSparseArray) {
        mTravelerSparseArray = travelerSparseArray;
        setTitleCount();
    }

    private void setTitleCount() {
        int count = mTravelerSparseArray != null ? mTravelerSparseArray.size() : 0;
        String travelerSelected = getResources().getQuantityString(R.plurals.traveler_selected,
                count, count);
        setTitle(travelerSelected);
    }


}
