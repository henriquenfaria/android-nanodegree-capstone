package com.henriquenfaria.wisetrip.activities;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.data.TempSimpleRecyclerAdapter;
import com.henriquenfaria.wisetrip.data.ViewPagerAdapter;
import com.henriquenfaria.wisetrip.models.TempVersionModel;
import com.henriquenfaria.wisetrip.models.Trip;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/* Activity to display all related data of a specific Trip */
public class TripDetailsActivity extends AppCompatActivity {

    private Trip mTrip;

    // private TabLayout.OnTabSelectedListener mTabLayoutListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTrip = getIntent().getParcelableExtra(Constants.Extra.EXTRA_TRIP);
        if (mTrip == null) {
            Toast.makeText(this, R.string.could_not_load_trip_details, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_trip_details);
        ButterKnife.bind(this);



      /*  mTabLayoutListener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:

                        break;
                    case 1:

                        break;
                    case 2:

                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        };*/

        setTripBackdropPhoto(mTrip.getId());
        setupToolbar();
        setupViewPager();

    }


    private void setupToolbar() {
        Toolbar toolbar = ButterKnife.findById(TripDetailsActivity.this, R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(mTrip.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void setupViewPager() {
        ViewPager viewPager = ButterKnife.findById(TripDetailsActivity.this, R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        //TODO: Temp code
        adapter.addFragment(new TempDummyFragment(android.R.color.background_light), "Expenses");
        adapter.addFragment(new TempDummyFragment(android.R.color.background_light), "Budgets");
        adapter.addFragment(new TempDummyFragment(android.R.color.background_light), "Places");

        viewPager.setAdapter(adapter);

        TabLayout tabLayout = ButterKnife.findById(TripDetailsActivity.this, R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setTripBackdropPhoto(String tripId) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directoryFile = cw.getDir(Constants.Global.DESTINATION_PHOTO_DIR,
                Context.MODE_PRIVATE);
        final File photoFile = new File(directoryFile, tripId);

        ImageView tripPhotoBackdrop = ButterKnife.findById(this, R.id.trip_backdrop);
        Picasso.with(this)
                .load(photoFile)
                .networkPolicy(
                        NetworkPolicy.NO_CACHE,
                        NetworkPolicy.NO_STORE,
                        NetworkPolicy.OFFLINE)
                .placeholder(R.color.colorAccent)
                .error(R.drawable.trip_photo_default)
                .into(tripPhotoBackdrop);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mTabLayout.removeOnTabSelectedListener(mTabLayoutListener);
    }


    //TODO: Temp dummy Fragment code
    public static class TempDummyFragment extends Fragment {
        int color;
        TempSimpleRecyclerAdapter adapter;

        public TempDummyFragment() {
        }

        @SuppressLint("ValidFragment")
        public TempDummyFragment(int color) {
            this.color = color;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
                savedInstanceState) {
            View view = inflater.inflate(R.layout.temp_dummy_fragment, container, false);

            final FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.dummyfrag_bg);
            frameLayout.setBackgroundColor(color);

            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id
                    .dummyfrag_scrollableview);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()
                    .getBaseContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setHasFixedSize(true);

            List<String> list = new ArrayList<>();
            for (int i = 0; i < TempVersionModel.data.length; i++) {
                list.add(TempVersionModel.data[i]);
            }

            adapter = new TempSimpleRecyclerAdapter(list);
            recyclerView.setAdapter(adapter);

            return view;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // To animate transition like back button press
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
