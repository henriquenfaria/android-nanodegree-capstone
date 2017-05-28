package com.henriquenfaria.wisetrip.activities;

import android.annotation.SuppressLint;
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
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.data.TravelerAdapter;
import com.henriquenfaria.wisetrip.models.Traveler;
import com.henriquenfaria.wisetrip.utils.Constants;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TravelerActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        TravelerAdapter.OnTravelerAdapter,
        SearchView.OnQueryTextListener {

    private static final String TAG = TravelerActivity.class.getSimpleName();
    private static final int ID_TASK_CONTACTS = 1;
    private static final String SAVE_TRAVELER_KEY = "save_traveler_key";

    @BindView(R.id.traveler_search_view)
    SearchView mTravelerSearchView;

    @BindView(R.id.traveler_recycler_view)
    RecyclerView mTravelerRecyclerView;

    private TravelerAdapter mTravelerAdapter;
    private LinearLayoutManager mLayoutManager;
    private HashMap<Long, Traveler> mTravelerHashMap;
    private String mSearchViewFilter;


    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traveler);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            // noinspection unchecked
            mTravelerHashMap = (HashMap<Long, Traveler>) savedInstanceState.getSerializable
                    (SAVE_TRAVELER_KEY);
        } else {

            mTravelerHashMap = new HashMap<>();
        }

        mTravelerSearchView.setOnQueryTextListener(this);
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
                if (mTravelerHashMap != null) {
                    data.putExtra(Constants.Extras.EXTRA_TRAVELER, mTravelerHashMap);
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
            outState.putSerializable(SAVE_TRAVELER_KEY, mTravelerHashMap);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String[] projectionFields = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};


        /*String[] projectionFields = new String[]{ContactsContract.Contacts._ID,
                ContactsContract.CommonDataKinds.Contactables.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Contactables.PHOTO_THUMBNAIL_URI};*/

        String selection = null;
        if (!TextUtils.isEmpty(mSearchViewFilter)) {
            selection = ContactsContract.CommonDataKinds.Contactables.DISPLAY_NAME + " LIKE  '%"
                    + mSearchViewFilter
                    + "%' ";
        }


       /* ContactsContract.Contacts._ID
        CursorLoader cursorLoader = new CursorLoader(TravelerActivity.this,
                ContactsContract.Contacts.CONTENT_URI,
                projectionFields,
                selection,
                null,
                ContactsContract.CommonDataKinds.Contactables.DISPLAY_NAME + " ASC"*/

        CursorLoader cursorLoader = new CursorLoader(TravelerActivity.this,
                ContactsContract.Contacts.CONTENT_URI,
                projectionFields,
                selection,
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
    public HashMap<Long, Traveler> getTravelerHashMap() {
        return mTravelerHashMap;
    }

    @Override
    public void setTravelerHashMap(HashMap<Long, Traveler> travelerHashMap) {
        mTravelerHashMap = travelerHashMap;
        setTitleCount();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mSearchViewFilter = !TextUtils.isEmpty(newText) ? newText : null;
        restartLoader();
        return true;
    }

    public void restartLoader() {
        if (getSupportLoaderManager().getLoader(ID_TASK_CONTACTS) != null) {
            getSupportLoaderManager().restartLoader(ID_TASK_CONTACTS, null, this);
        } else {
            getSupportLoaderManager().initLoader(ID_TASK_CONTACTS, null, this);
        }
    }

    private void setTitleCount() {
        int count = mTravelerHashMap != null ? mTravelerHashMap.size() : 0;
        String travelerSelected = getResources().getQuantityString(R.plurals.traveler_selected,
                count, count);
        setTitle(travelerSelected);
    }
}
