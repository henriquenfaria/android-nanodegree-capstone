package com.henriquenfaria.wisetrip.activities;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.data.TempSimpleRecyclerAdapter;
import com.henriquenfaria.wisetrip.data.ViewPagerAdapter;
import com.henriquenfaria.wisetrip.models.Attribution;
import com.henriquenfaria.wisetrip.models.TempVersionModel;
import com.henriquenfaria.wisetrip.models.Trip;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static butterknife.ButterKnife.findById;


/* Activity to display all related data of a specific Trip */
public class TripDetailsActivity extends AppCompatActivity {

    private Trip mTrip;

    @BindView(R.id.attribution_container)
    protected LinearLayout mAttributionContainer;
    @BindView(R.id.attribution_prefix)
    protected TextView mAttributionPrefix;
    @BindView(R.id.attribution_content)
    protected TextView mAttributionContent;


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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        } else {
            supportPostponeEnterTransition();
        }

        setTripBackdropPhoto(mTrip.getId());
        setupAppBarLayout();
        setupToolbar();
        setupViewPager();

    }


    private void setupToolbar() {
        Toolbar toolbar = findById(TripDetailsActivity.this, R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView title = ButterKnife.findById(toolbar, R.id.toolbar_title);
        title.setText(mTrip.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
    }

    private void setupAppBarLayout() {
        AppBarLayout appbarLayout = ButterKnife.findById(TripDetailsActivity.this, R.id
                .appbarlayout);
        appbarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    switch (state) {
                        case COLLAPSED:
                        case IDLE:
                            ButterKnife.findById(TripDetailsActivity.this,
                                    R.id.trip_photo_protection).setTransitionName("");
                            ButterKnife.findById(TripDetailsActivity.this,
                                    R.id.trip_photo_backdrop).setTransitionName("");
                            ButterKnife.findById(TripDetailsActivity.this,
                                    R.id.attribution_container).setTransitionName("");
                            break;
                        case EXPANDED:
                            // TODO: Do not use hardcoded transition names
                            ButterKnife.findById(TripDetailsActivity.this,
                                    R.id.trip_photo_protection)
                                    .setTransitionName("trip_photo_protection");
                            ButterKnife.findById(TripDetailsActivity.this,
                                    R.id.trip_photo_backdrop).setTransitionName("trip_photo");
                            ButterKnife.findById(TripDetailsActivity.this,
                                    R.id.attribution_container)
                                    .setTransitionName("attribution_container");
                            break;
                    }
                }
            }
        });
    }

    private void setupViewPager() {
        ViewPager viewPager = findById(TripDetailsActivity.this, R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        //TODO: Temp code
        adapter.addFragment(new TempDummyFragment(android.R.color.background_light), "Expenses");
        adapter.addFragment(new TempDummyFragment(android.R.color.background_light), "Budgets");
        adapter.addFragment(new TempDummyFragment(android.R.color.background_light), "Places");

        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findById(TripDetailsActivity.this, R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setTripBackdropPhoto(String tripId) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directoryFile = cw.getDir(Constants.Global.DESTINATION_PHOTO_DIR,
                Context.MODE_PRIVATE);
        final File photoFile = new File(directoryFile, tripId);

        ImageView tripPhotoBackdrop = findById(this, R.id.trip_photo_backdrop);
        Picasso.with(this)
                .load(photoFile)
                .networkPolicy(
                        NetworkPolicy.NO_CACHE,
                        NetworkPolicy.NO_STORE,
                        NetworkPolicy.OFFLINE)
                .noFade()
                .noPlaceholder()
                .error(R.drawable.trip_photo_default)
                .into(tripPhotoBackdrop, new Callback() {
                    @Override
                    public void onSuccess() {
                        displayPhotoAttribution(mTrip.getId(), true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startPostponedEnterTransition();
                        } else {
                            supportStartPostponedEnterTransition();
                        }
                    }

                    @Override
                    public void onError() {
                        displayPhotoAttribution(mTrip.getId(), false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startPostponedEnterTransition();
                        } else {
                            supportStartPostponedEnterTransition();
                        }
                    }
                });
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                } else {
                    finish();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static abstract class AppBarStateChangeListener implements AppBarLayout
            .OnOffsetChangedListener {

        public enum State {
            EXPANDED,
            COLLAPSED,
            IDLE
        }

        private State mCurrentState = State.IDLE;

        @Override
        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            if (i == 0) {
                if (mCurrentState != State.EXPANDED) {
                    onStateChanged(appBarLayout, State.EXPANDED);
                }
                mCurrentState = State.EXPANDED;
            } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                if (mCurrentState != State.COLLAPSED) {
                    onStateChanged(appBarLayout, State.COLLAPSED);
                }
                mCurrentState = State.COLLAPSED;
            } else {
                if (mCurrentState != State.IDLE) {
                    onStateChanged(appBarLayout, State.IDLE);
                }
                mCurrentState = State.IDLE;
            }
        }

        public abstract void onStateChanged(AppBarLayout appBarLayout, State state);
    }

    private void displayPhotoAttribution(String tripId, boolean shouldDisplay) {


        if (mAttributionContainer != null) {
            if (shouldDisplay) {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference attributionsReference = firebaseDatabase
                        .getReference()
                        .child("attributions")
                        .child(currentUser.getUid())
                        .child(tripId);

                attributionsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Timber.d("onDataChange");
                        if (mAttributionContainer != null) {
                            Attribution attribution = dataSnapshot.getValue(Attribution.class);
                            if (attribution != null && !TextUtils.isEmpty(attribution.getText())) {
                                Spanned result;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    result = Html.fromHtml(attribution.getText(),
                                            Html.FROM_HTML_MODE_LEGACY);
                                } else {
                                    result = Html.fromHtml(attribution.getText());
                                }

                                mAttributionContent.setText(result);
                                mAttributionContent.setMovementMethod(
                                        LinkMovementMethod.getInstance());
                                mAttributionPrefix.setVisibility(View.VISIBLE);
                                mAttributionContent.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Timber.d("onCancelled");
                        //TODO: Implementation needed?
                    }
                });
            } else {
                mAttributionPrefix.setVisibility(View.GONE);
                mAttributionContent.setVisibility(View.GONE);
            }
        }
    }
}
